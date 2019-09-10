import React from "react";
import "./TeamPage.css";
import {Dialog} from "@blueprintjs/core";

export default class TeamPage extends React.Component {
    render() {
        return (
            <Dialog isOpen={this.props.open} title="About the Team" onClose={this.props.onClose}>
                <img/>
            </Dialog>
        )
    }
}