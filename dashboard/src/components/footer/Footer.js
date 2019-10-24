import React from "react";
import "./Footer.css";
import {Button, Popover} from "@blueprintjs/core";
import TeamPage from "./TeamPage";
import {Link, HashRouter as Router} from "react-router-dom";

export default class Footer extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            openTeam: false
        }
    }

    onTeamClick = (evt) => {
        evt.preventDefault();
        this.setState({
            openTeam: !this.state.openTeam
        });
    };

    render() {
        return (
            <div className="footer">
                <div>
                    <p className="footer-description">
                        A Project by the Department of Intelligent Systems Engineering at Indiana University,
                        Bloomington. In collaboration with NTE IndyCar, Intel and Amazon.
                    </p>
                </div>
                <div className="footer-buttons">
                    <Router>
                        <Link to="/team">THE TEAM</Link>
                        <Link to="/contact">CONTACT US</Link>
                    </Router>
                </div>
            </div>
        )
    }

}