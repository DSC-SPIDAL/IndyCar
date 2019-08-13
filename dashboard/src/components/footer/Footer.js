import React from "react";
import "./Footer.css";
import {Button, Popover} from "@blueprintjs/core";

export default class Footer extends React.Component {

    constructor(props) {
        super(props);
    }

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
                    <a href="#">THE TEAM</a>
                    <a href="#">CONTACT US</a>
                </div>
            </div>
        )
    }

}