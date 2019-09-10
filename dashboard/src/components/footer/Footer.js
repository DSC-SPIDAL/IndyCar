import React from "react";
import "./Footer.css";
import {Button, Popover} from "@blueprintjs/core";
import TeamPage from "./TeamPage";

export default class Footer extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            openTeam: false
        }
    }

    onTeamClick = () => {
        console.log("OPEN");
        this.setState({
            openTeam: !this.state.openTeam
        });
    };

    render() {
        return (
            <div className="footer">
                <div>
                    <p>
                        A Project by the Department of Intelligent Systems Engineering at Indiana University,
                        Bloomington. In collaboration with NTE IndyCar, Intel and Amazon.
                    </p>
                </div>
                <div className="footer-buttons">
                    <a href="javascript:;" onClick={this.onTeamClick}>THE TEAM</a>
                    <a href="javascript:;">CONTACT US</a>
                </div>
                <TeamPage open={this.state.openTeam} onClose={this.onTeamClick}/>
            </div>
        )
    }

}