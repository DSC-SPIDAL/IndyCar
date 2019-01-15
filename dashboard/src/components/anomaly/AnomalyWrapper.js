import React from "react";
import SpeedAnomalyComponent from "./SpeedAnomalyComponent";
import {Card} from "@blueprintjs/core";
import "./AnomalyWrapper.css";
import {ANOMALY_METRIC} from "./AnomalyConstants";

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
    }

    onCarChange = (event) => {
        this.setState({
            selectedCarNumber: event.target.value
        });
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
                        <label className="pt-label">
                            Metric
                            <div className="pt-select">
                                <select onChange={this.onMetricChange} value={this.state.selectedMetric}>
                                    {
                                        Object.keys(ANOMALY_METRIC).map(metric => {
                                            return (
                                                <option value={metric} key={metric}>
                                                    {ANOMALY_METRIC[metric].text}
                                                </option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </label>
                    </div>
                    <SpeedAnomalyComponent carNumber={this.state.selectedCarNumber}
                                           metric={this.state.selectedMetric}
                                           key={this.state.selectedCarNumber + this.state.selectedMetric}/>
                </Card>
            </div>
        );
    }
}