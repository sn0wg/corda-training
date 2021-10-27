import * as React from 'react';
import NavBar from '../../components/navbar/navbar';
import Home from '../home/home';
import Buy from '../buy/buy';
import Sell from '../sell/sell';
import {
    BrowserRouter as Router,
    Switch,
    Route
} from "react-router-dom";


class MainPage extends React.Component {
    render() {
        return (
            <Router>
                <div>
                    <NavBar />
                    <Switch>
                        <Route path="/buy">
                            <Buy />
                        </Route>
                        <Route path="/sell">
                            <Sell />
                        </Route>
                        <Route path="/">
                            <Home />
                        </Route>
                    </Switch>
                </div>
            </Router>
        );
    }
}

export default MainPage;