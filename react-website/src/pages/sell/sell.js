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
import ClearIcon from '@material-ui/icons/Clear';
import CheckIcon from '@material-ui/icons/Check';
const axios = require('axios').default;

class Sell extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            proposals: []
        };
    }

    componentDidMount() {
        this.getProposals();
    }

    getProposals() {
        axios.get('http://localhost:8080/proposal/for')
            .then(result => result.data)
            .then(result => this.setState({ proposals: result }))
            .then(result => console.log(this.state.proposals));
    }

    viewImage(imageHash) {
        window.open('http://localhost:8080/file/' + imageHash);
    }

    response(hash, response) {
        const params = new URLSearchParams();
        params.append('RESPONSE', response);
        let url = 'http://localhost:8080/proposal/' + hash + '/answer';
        axios.post(url, params).then(response => this.getProposals());
    }

    render() {
        return (
            <div className="wrapper">
                <h2> Propostas de venda </h2>
                <List dense={false}>
                    {this.state.proposals.map(item =>
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
                                <IconButton edge="end" aria-label="delete" disabled={item.status != 'CREATED'} onClick={() => this.response(item.linearId.id, 'REFUSE')}>
                                    <ClearIcon />
                                </IconButton>
                                <IconButton edge="end" aria-label="delete" disabled={item.status != 'CREATED'} onClick={() => this.response(item.linearId.id, 'ACCEPT')}>
                                    <CheckIcon />
                                </IconButton>
                            </ListItemSecondaryAction>
                        </ListItem>
                    )}
                </List>
            </div>
        );
    }
}

export default Sell;