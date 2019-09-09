import React from "react";
import "./FocusViewComponent.css";
import PlayerSelectionComponent from "../player/PlayerSelectionComponent";
import {connect} from "react-redux";
import VisorView from "../visor/VisorView";
import AnomalyWrapper from "../anomaly/AnomalyWrapper";
import LapTimesComponent from "../laps/LapTimesComponent";

class FocusViewComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="focusview-wrapper">
                <PlayerSelectionComponent/>
                <VisorView/>
                <AnomalyWrapper/>
                <LapTimesComponent/>
            </div>
        );
    }
}

const focusView = connect()(FocusViewComponent);

export default focusView;