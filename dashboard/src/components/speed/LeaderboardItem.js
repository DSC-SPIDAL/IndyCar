import React from "react";
import "./SpeedDataComponent.css";
import PropTypes from 'prop-types';
import "./LeaderboardItem.css";
import {connect} from "react-redux";
import {Spinner} from "@blueprintjs/core";

/**
 * @author Chathura Widanage
 */
class LeaderboardItem extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            driverName: "",
            lastLapTime: 0,
            fastestLapTime: 0,
            predictedRank: ""
        };
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        let driverName = prevState.driverName;
        let lastLapTime = prevState.lastLapTime;
        let fastestLapTime = prevState.fastestLapTime;
        let predictedRank = "-";

        if (nextProps.players[nextProps.carNumber]) {
            driverName = nextProps.players[nextProps.carNumber].driverName;
        }

        if (nextProps.lastLaps[nextProps.carNumber]) {
            lastLapTime = nextProps.lastLaps[nextProps.carNumber].time;
            fastestLapTime = nextProps.lastLaps[nextProps.carNumber].fastestLapTime;
        }

        // if (nextProps.ranks.predictions
        //     && typeof nextProps.ranks.predictions[nextProps.carNumber] !== 'undefined') {
        //     if (nextProps.ranks.predictions[nextProps.carNumber] <= 0) {
        //         predictedRank = 1;
        //     } else {
        //         predictedRank = nextProps.ranks.predictions[nextProps.carNumber];
        //     }
        // }

        if (nextProps.predictions
            && typeof nextProps.predictions[nextProps.carNumber] !== 'undefined') {
            if (isNaN(nextProps.predictions[nextProps.carNumber])) {
                predictedRank = 0;
            } else {
                predictedRank = parseInt(nextProps.predictions[nextProps.carNumber]);
            }
        }

        return {
            driverName, lastLapTime, fastestLapTime, predictedRank
        };
    }

    render() {

        let predColor = "black";

        if (this.state.predictedRank && this.state.predictedRank > 0) {
            predColor = "red";
        } else if (this.state.predictedRank && this.state.predictedRank < 0) {
            predColor = "green";
        }

        return (
            <tr>
                <td>
                    [{this.props.carNumber}]
                </td>
                <td>
                    {this.state.driverName}
                </td>
                <td>
                    {this.props.ranks.carToRank[this.props.carNumber]}
                </td>
                <td style={{color: predColor}}>
                    {isNaN(this.state.predictedRank) ? "-" : this.props.ranks.carToRank[this.props.carNumber] + this.state.predictedRank}
                </td>
                <td className="hidden-on-mobile">
                    {this.state.fastestLapTime}
                </td>
                <td className="hidden-on-mobile">
                    {this.state.lastLapTime}
                </td>
            </tr>
        );
    }
}

LeaderboardItem.propTypes = {
    carNumber: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

const LBItem = connect(state => {
    return {
        players: state.PlayerInfo.players || {},
        lastLaps: state.PlayerInfo.lastLaps || {},
        ranks: state.PlayerInfo.ranks || {},
        predictions: state.PlayerInfo.rankPredictions || {}
    }
})(LeaderboardItem);

export default LBItem;