import React from "react";
import "./SpeedDataComponent.css";
import PropTypes from 'prop-types';
import "./LeaderboardItem.css";
import {connect} from "react-redux";

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

        if (nextProps.ranks.predictions
            && typeof nextProps.ranks.predictions[nextProps.carNumber] !== 'undefined') {
            if (nextProps.ranks.predictions[nextProps.carNumber] <= 0) {
                predictedRank = 1;
            } else {
                predictedRank = nextProps.ranks.predictions[nextProps.carNumber];
            }
        }

        return {
            driverName, lastLapTime, fastestLapTime, predictedRank
        };
    }

    render() {
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
                <td style={{color: 'red'}}>
                    {this.state.predictedRank}
                </td>
                <td>
                    {this.state.fastestLapTime}
                </td>
                <td>
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
        ranks: state.PlayerInfo.ranks || {}
    }
})(LeaderboardItem);

export default LBItem;