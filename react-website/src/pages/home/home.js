import * as React from 'react';
import CustomCard from '../../components/card/card';
import './home.scss';
import Button from '@material-ui/core/Button';
import AddIcon from '@material-ui/icons/Add';
const axios = require('axios').default;

class Home extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: []
        };
    }

    onFileChange = event => {
        // Update the state
        const formData = new FormData();
        formData.append('file', event.target.files[0]);
        axios.post("http://localhost:8080/file", formData)
            .then(response => response.data)
            .then(response => {
                const params = new URLSearchParams();
                params.append('hash', response.fileHash);
                return axios.post('http://localhost:8080/item', params);
            }).then(response => this.getItems());
    };

    getItems() {
        axios.get('http://localhost:8080/item')
            .then(result => result.data)
            .then(result => this.setState({ items: result }));
    }

    componentDidMount() {
        this.getItems();
    }

    render() {
        return (
            <div>
                <div className="main-grid">
                    {
                        this.state.items.map(item => <CustomCard key={item.linearId.id} label={item.owner} imageUrl={'http://localhost:8080/file/' + item.linearId.externalId} hash={item.linearId.externalId} />)
                    }
                </div>
                <div className="float-button">
                    <input accept="image/*" className="hidden-input" id="contained-button-file" type="file" onChange={this.onFileChange} />
                    <label htmlFor="contained-button-file">
                        <Button variant="contained" color="primary" component="span" startIcon={<AddIcon />}>
                            Upload
                        </Button>
                    </label>
                </div>
            </div>
        );
    }
}

export default Home;