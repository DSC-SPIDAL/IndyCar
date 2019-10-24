import React from "react";
import IMG from "./maxresdefault-2.jpg";
import "./VisorView.css";
import {connect} from "react-redux";
import {Spinner} from "@blueprintjs/core";

class VisorView extends React.Component {
    render() {

        let pastLaps = Object.values(this.props.pastLaps).reverse().map((lap, index) => {
            return (
                <div className="visor-view-row" key={index}>
                    <div className="visor-view-col">
                        LAP {lap.completedLaps + 1}
                    </div>
                    <div className="visor-view-col">
                        {lap.time}
                    </div>
                </div>
            );
        });

        return (
            <div className="visor-view-wrapper">
                <div className="car-image">
                    <img src={IMG} alt="car image"/>
                    <div className="positions-wrapper">
                        <div className="current-position">
                            Current Position <span>{this.props.currentRank}</span>
                        </div>
                        <div className="predicted-position">
                            Predicted Position <span>{this.props.predictedRank}</span>
                        </div>
                    </div>
                </div>
                <div className="best-time-widget">
                    <div className="visor-view-row">
                        <div className="visor-view-col">
                            BEST TIME (s)
                        </div>
                        <div className="visor-view-col">
                            {this.props.lastLap.fastestLapTime || <Spinner small={true}/>}
                        </div>
                    </div>
                    <div className="visor-view-row">
                        <div className="visor-view-col">
                            CURRENT (s)
                        </div>
                        <div className="visor-view-col">
                            {this.props.lastLap.time || <Spinner small={true}/>}
                        </div>
                    </div>
                </div>

                <div className="lap-history-widget">
                    {pastLaps}
                </div>
            </div>
        )
    }
}

export default connect(state => {
    let currentRank = "-";
    let predictedRank = "-";
    let pastLaps = {};
    let lastLap = {};

    if (state.PlayerInfo.ranks && state.PlayerInfo.ranks.carToRank && state.AnomalyInfo.focusedPlayer) {
        currentRank = state.PlayerInfo.ranks.carToRank[state.AnomalyInfo.focusedPlayer];

        if (state.PlayerInfo.ranks.predictions) {
            predictedRank = state.PlayerInfo.ranks.predictions[state.AnomalyInfo.focusedPlayer];
        }

        if (state.PlayerInfo.lastLaps) {
            lastLap = state.PlayerInfo.lastLaps[state.AnomalyInfo.focusedPlayer] || {}
        }

        if (state.PlayerInfo.laps) {
            pastLaps = state.PlayerInfo.laps[state.AnomalyInfo.focusedPlayer] || {}
        }
    }


    return {
        currentRank, predictedRank, lastLap, pastLaps
    };
})(VisorView);