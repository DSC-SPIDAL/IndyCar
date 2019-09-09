import React from "react";
import SpeedAnomalyComponent from "./SpeedAnomalyComponent";
import {Card} from "@blueprintjs/core";
import "./AnomalyWrapper.css";
import {SocketService} from "../../services/SocketService";
import {connect} from "react-redux";
import {ACTION_ANOMALY_DATA_RECEIVED, ACTION_CLEAR_ANOMALY_DATA} from "../../reducers/AnomalyReducer";

class AnomalyWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.socket = new SocketService();
    }

    subscribeToPlayer = (newPlayer, oldPlayer) => {
        if (oldPlayer) {
            this.socket.send("EVENT_UNSUB", {
                roomName: "anomaly_" + oldPlayer
            });
            this.socket.unsubscribe("anomaly_" + oldPlayer, this.onReceiveAnomalyData);
        }

        this.socket.send("EVENT_SUB", {
            roomName: "anomaly_" + newPlayer
        });
        this.socket.subscribe("anomaly_" + newPlayer, this.onReceiveAnomalyData);
    };

    onReceiveAnomalyData = (anomalyData) => {
        this.props.dispatch({
            type: ACTION_ANOMALY_DATA_RECEIVED,
            data: anomalyData
        })
    };

    componentDidMount() {
        this.subscribeToPlayer(this.props.focusedPlayer);
        this.socket.subscribe("anomaly_" + this.props.focusedPlayer, this.onReceiveAnomalyData);
    }

    componentWillReceiveProps(nextProps, nextContext) {
        console.log(this.props, nextProps);
        if (nextProps.focusedPlayer !== this.props.focusedPlayer) {
            console.log("Changing subscription");
            this.subscribeToPlayer(nextProps.focusedPlayer, this.props.focusedPlayer);
        }
    }

    componentWillUnmount() {
        this.props.dispatch({
            type: ACTION_CLEAR_ANOMALY_DATA
        });
        this.socket.unsubscribe("anomaly_" + this.props.focusedPlayer, this.onReceiveAnomalyData);
    }

    render() {
        return (
            <div className="ic-section ic-anomaly-wrapper">
                <SpeedAnomalyComponent carNumber={this.props.focusedPlayer}
                                       metric={"SPEED"}
                                       rawDataColor="black"
                    // rawDataColor="#1565C0"
                                       hideX={true}
                                       key={this.props.focusedPlayer + "SPEED"}/>
                <SpeedAnomalyComponent carNumber={this.props.focusedPlayer}
                                       metric={"RPM"}
                                       hideX={true}
                                       rawDataColor="black"
                    // rawDataColor="#2E7D32"
                                       key={this.props.focusedPlayer + "RPM"}/>
                <SpeedAnomalyComponent carNumber={this.props.focusedPlayer}
                                       metric={"THROTTLE"}
                                       rawDataColor="black"
                    // rawDataColor="#673AB7"
                                       key={this.props.focusedPlayer + "THROTTLE"}/>
            </div>
        );
    }
}

const anomalyWrapper = connect(state => {
    return {
        focusedPlayer: state.AnomalyInfo.focusedPlayer
    }
})(AnomalyWrapper);

export default anomalyWrapper;