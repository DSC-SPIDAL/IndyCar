import React from "react";
import SpeedAnomalyComponent from "./SpeedAnomalyComponent";
import {Card} from "@blueprintjs/core";
import "./AnomalyWrapper.css";
import {ANOMALY_METRIC} from "./AnomalyConstants";
import {SocketService} from "../../services/SocketService";

export default class AnomalyWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carNumbers: [
                20, 21, 13, 98, 19, 6, 33, 24, 26, 7, 60, 27, 17, 15, 10,
                64, 25, 59, 32, 28, 4, 3, 18, 22, 12, 1, 9, 14, 23, 30, 29, 88, 66
            ].sort(),
            selectedCarNumber: 20,
            selectedMetric: ANOMALY_METRIC.RPM.id
        }
        this.socket = new SocketService();
    }

    subscribe = () => {
        this.socket.send("EVENT_SUB", {
            roomName: "anomaly_" + this.state.selectedCarNumber
        });
    };

    componentDidMount() {
        this.subscribe();
    }

    onCarChange = (event) => {
        this.socket.send("EVENT_UNSUB", {
            roomName: "anomaly_" + this.state.selectedCarNumber
        });
        this.setState({
            selectedCarNumber: event.target.value
        }, this.subscribe);
    };

    onMetricChange = (event) => {
        this.setState({
            selectedMetric: event.target.value
        });
    };

    render() {
        return (
            <div className="ic-section ic-anomaly-wrapper">
                <Card>
                    <h5>Anomaly Scores</h5>
                    <div className="ic-anomaly-selection">
                        <label className="pt-label">
                            Car
                            <div className="pt-select">
                                <select onChange={this.onCarChange} value={this.state.selectedCarNumber}>
                                    {
                                        this.state.carNumbers.map(carNum => {
                                            return <option value={carNum} key={carNum}>{carNum}</option>
                                        })
                                    }
                                </select>
                            </div>
                        </label>
                        {/*<label className="pt-label">*/}
                        {/*Metric*/}
                        {/*<div className="pt-select">*/}
                        {/*<select onChange={this.onMetricChange} value={this.state.selectedMetric}>*/}
                        {/*{*/}
                        {/*Object.keys(ANOMALY_METRIC).map(metric => {*/}
                        {/*return (*/}
                        {/*<option value={metric} key={metric}>*/}
                        {/*{ANOMALY_METRIC[metric].text}*/}
                        {/*</option>*/}
                        {/*);*/}
                        {/*})*/}
                        {/*}*/}
                        {/*</select>*/}
                        {/*</div>*/}
                        {/*</label>*/}
                    </div>

                    <SpeedAnomalyComponent carNumber={this.state.selectedCarNumber}
                                           metric={"SPEED"}
                                           rawDataColor="#1565C0"
                                           hideX={true}
                                           key={this.state.selectedCarNumber + "SPEED"}/>
                    <SpeedAnomalyComponent carNumber={this.state.selectedCarNumber}
                                           metric={"RPM"}
                                           hideX={true}
                                           rawDataColor="#2E7D32"
                                           key={this.state.selectedCarNumber + "RPM"}/>
                    <SpeedAnomalyComponent carNumber={this.state.selectedCarNumber}
                                           metric={"THROTTLE"}
                                           rawDataColor="#EF6C00"
                                           key={this.state.selectedCarNumber + "THROTTLE"}/>
                </Card>
            </div>
        );
    }
}