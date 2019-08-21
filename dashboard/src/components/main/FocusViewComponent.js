import React from "react";
import "./FocusViewComponent.css";
import PlayerSelectionComponent from "../player/PlayerSelectionComponent";
import {connect} from "react-redux";

class FocusViewComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="focusview-wrapper">
                <PlayerSelectionComponent/>
            </div>
        );
    }
}

const focusView = connect()(FocusViewComponent);

export default focusView;