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
            lastLapTime: 0
        };
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        let driverName = prevState.driverName;
        let lastLapTime = prevState.lastLapTime;

        if (nextProps.players[nextProps.carNumber]) {
            driverName = nextProps.players[nextProps.carNumber].driverName;
            console.log("DN", driverName);
        }

        if (nextProps.lastLaps[nextProps.carNumber]) {
            lastLapTime = nextProps.lastLaps[nextProps.carNumber].time;
        }

        return {
            driverName, lastLapTime
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
                    {this.props.rank}
                </td>
                <td>
                    -
                </td>
                <td>
                    -
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
        lastLaps: state.PlayerInfo.lastLaps
    }
})(LeaderboardItem);

export default LBItem;