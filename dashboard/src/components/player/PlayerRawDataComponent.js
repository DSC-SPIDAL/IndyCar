import React from "react";
import "./PlayerRawDataComponent.css";
import {SocketService} from "../../services/SocketService";
import SPEED_ICO from "./img/speedIcon.svg";
import RPM_ICO from "./img/RPMIcon.svg";
import THROTTLE_ICO from "./img/gearIcon.svg";

export class PlayerRawDataComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            speed: 0,
            rpm: 0,
            throttle: 0,
            speedAnomaly: "",
            rpmAnomaly: "",
            throttleAnomaly: "",
        };
        this.socket = new SocketService();
    }

    onReceiveChartData = (data) => {
        try {
            console.log(data);
            this.setState({
                speed: data.anomalies["SPEED"].rawData,
                rpm: data.anomalies["RPM"].rawData,
                throttle: data.anomalies["THROTTLE"].rawData,
                speedAnomaly: data.anomalies["SPEED"].anomaly > 0.5 ? "red"
                    : data.anomalies["SPEED"].anomaly > 0.3 ? "yellow" : "",
                rpmAnomaly: data.anomalies["RPM"].anomaly > 0.5 ? "red"
                    : data.anomalies["RPM"].anomaly > 0.3 ? "yellow" : "",
                throttleAnomaly: data.anomalies["THROTTLE"].anomaly > 0.5 ? "red"
                    : data.anomalies["THROTTLE"].anomaly > 0.3 ? "yellow" : ""
            });
        } catch (e) {
            console.log("Invalid data format ", e);
        }
    };

    componentWillMount() {
        this.socket.subscribe("anomaly_" + this.props.selectedCarNumber, this.onReceiveChartData);
    }

    componentWillUnmount() {
        this.socket.unsubscribe("anomaly_" + this.props.selectedCarNumber, this.onReceiveChartData);
    }

    componentWillReceiveProps(nextProps, nextContext) {
        if (nextProps.selectedCarNumber !== this.props.selectedCarNumber) {
            this.socket.unsubscribe("anomaly_" + this.props.selectedCarNumber, this.onReceiveChartData);
            this.socket.subscribe("anomaly_" + nextProps.selectedCarNumber, this.onReceiveChartData);
            this.setState({
                speed: 0,
                rpm: 0,
                throttle: 0,
                speedAnomaly: "",
                rpmAnomaly: "",
                throttleAnomaly: "",
            })
        }
    }

    render() {
        return (
            <div className="player-raw-data-wrapper">
                <div className="player-img">
                    <img src={`img/cars/car_${('' + this.props.selectedCarNumber).padStart(2, '0')}.png`}/>
                </div>
                <div className="player-raw-data-widget">
                    <div className="player-raw-data-metric">
                        <div>
                            <img src={SPEED_ICO}/>
                        </div>
                        <div>
                            <div className="player-raw-data-metric-name">
                                SPEED [mph]
                            </div>
                            <div className={"player-raw-data-metric-value metric-value-" + this.state.speedAnomaly}>
                                {this.state.speed}
                            </div>
                        </div>
                    </div>
                    <div className="player-raw-data-metric">
                        <div>
                            <img src={RPM_ICO}/>
                        </div>
                        <div>
                            <div className="player-raw-data-metric-name">
                                RPM
                            </div>
                            <div className={"player-raw-data-metric-value metric-value-" + this.state.rpmAnomaly}>
                                {this.state.rpm}
                            </div>
                        </div>
                    </div>
                    <div className="player-raw-data-metric">
                        <div>
                            <img src={THROTTLE_ICO}/>
                        </div>
                        <div>
                            <div className="player-raw-data-metric-name">
                                THROTTLE [%]
                            </div>
                            <div className={"player-raw-data-metric-value metric-value-" + this.state.throttleAnomaly}>
                                {this.state.throttle}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}