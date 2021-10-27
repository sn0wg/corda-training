import * as React from 'react';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardMedia from '@material-ui/core/CardMedia';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import {
    Link
} from "react-router-dom";

class CustomCard extends React.Component {

    onClickFunction = () => {
        window.open(this.props.imageUrl);
    }

    history = () => {
        window.open('http://localhost:8080/item/' + this.props.hash);
    }

    createCard() {
        return (
            <Card>
                <CardActionArea>
                    <CardMedia
                        component="img"
                        image={this.props.imageUrl}
                        height="200"
                        onClick={this.onClickFunction}
                    />
                </CardActionArea>
                <CardContent>
                    <Typography gutterBottom variant="h5" component="h2">
                        {this.props.label}
                    </Typography>
                </CardContent>
                <CardActions>
                    <Link to={'/buy?hash=' + this.props.hash}>
                        <Button size="large" color="primary">
                            Comprar
                        </Button>
                    </Link>
                    <Button size="large" color="primary"  onClick={this.history}>
                        Histórico
                    </Button>
                </CardActions>
            </Card>
        );
    }

    render() {
        return (
            this.createCard()
        );
    }
}

export default CustomCard;