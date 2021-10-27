import * as React from 'react';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import { Link, useLocation, BrowserRouter as Router } from "react-router-dom";
import './buy.scss';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import FolderIcon from '@material-ui/icons/Folder';
import DeleteIcon from '@material-ui/icons/Delete';
import PaymentIcon from '@material-ui/icons/Payment';
import VisibilityIcon from '@material-ui/icons/Visibility';
const axios = require('axios').default;

const useQuery = () => {
    return new URLSearchParams(useLocation().search);
}

function Buy() {
    let query = useQuery();
    return (
        <BuyClass hash={query.get('hash')} />
    )
}

class BuyClass extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            hash: props.hash,
            value: undefined,
            proposals: [],
            balance: 0
        };

        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;
        this.setState({
            [name]: value
        });
    }

    handleSubmit(event) {
        const params = new URLSearchParams();
        params.append('hash', this.state.hash);
        params.append('value', this.state.value);
        axios.post('http://localhost:8080/proposal', params).then(response => this.getProposals());
        event.preventDefault();
    }

    pay(hash) {
        let url = 'http://localhost:8080/proposal/' + hash + '/pay';
        axios.post(url).then(response => this.getProposals());
    }

    componentDidMount() {
        this.getProposals();
    }

    getProposals() {
        axios.get('http://localhost:8080/proposal/from')
            .then(result => result.data)
            .then(result => this.setState({ proposals: result }));

            axios.get('http://localhost:8080/wallet')
            .then(result => result.data)
            .then(result => this.setState({ balance: result }));
    }

    viewImage(imageHash) {
        window.open('http://localhost:8080/file/' + imageHash);
    }

    render() {
        return (
            <div className="wrapper">
                <form className="form-grid" onSubmit={this.handleSubmit}>
                    <TextField id="outlined-basic" label="Hash" variant="outlined" name="hash" onChange={this.handleInputChange} value={this.state.hash} />
                    <TextField id="outlined-basic" label="Valor" variant="outlined" type="number" name="value" onChange={this.handleInputChange} value={this.state.value} />
                    <Button variant="contained" color="primary" type="submit">
                        Enviar
                    </Button>
                </form>
                <h2> Saldo: {this.state.balance} </h2>
                <h2> Propostas de compra </h2>
                <List dense={false}>
                    {this.state.proposals.map( item =>
                        <ListItem>
                            <ListItemText
                                primary={item.itemID.externalId}
                            />
                            <ListItemText
                                primary={item.value}
                            />
                            <ListItemText
                                primary={item.status}
                            />
                            <ListItemSecondaryAction>
                                <IconButton edge="end" aria-label="delete" onClick={() => this.viewImage(item.itemID.externalId)}>
                                    <VisibilityIcon />
                                </IconButton>
                                <IconButton edge="end" aria-label="delete" disabled={item.status != 'ACCEPTED'} onClick={() => this.pay(item.linearId.id)}>
                                    <PaymentIcon />
                                </IconButton>
                            </ListItemSecondaryAction>
                        </ListItem>
                    )}
                </List>
            </div>
        );
    }
}

export default Buy;