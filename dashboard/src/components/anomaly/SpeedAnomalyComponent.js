import React from "react";
import {Bar} from "react-chartjs-2";
import "chartjs-plugin-datalabels"
import SPEED_ICO from "./img/speedIcon.svg";
import RPM_ICO from "./img/RPMIcon.svg";
import GEAR_ICO from "./img/gearIcon.svg";
import Gauage from "../../util/gauage/Gauage";
import "./SpeedAnomalyComponent.css";
import {connect} from "react-redux";

const METRIC_UPPER_BOUNDS = {
    SPEED: 240,
    RPM: 12600,
    THROTTLE: 115
};

class SpeedAnomalyComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    getIcon = () => {
        switch (this.props.metric) {
            case "SPEED":
                return SPEED_ICO;
            case "RPM":
                return RPM_ICO;
            case "THROTTLE":
                return GEAR_ICO;
        }
    };

    render() {
        return (
            <div className="anomaly-widgets-wrapper">
                <div className="raw-gauge-wrapper">
                    <div className="raw-gauge-header">
                        <div>
                            <img src={this.getIcon()}/>
                        </div>
                        <div>
                            <div className="raw-gauge-header-value">
                                {this.props.anomalyData[this.props.metric].last}
                            </div>
                            <div className="raw-gauge-header-name">
                                {this.props.metric}
                            </div>
                        </div>
                    </div>
                    <div className="raw-gauge-body">
                        <Gauage maxValue={METRIC_UPPER_BOUNDS[this.props.metric]}
                                value={this.props.anomalyData[this.props.metric].last}/>
                    </div>
                </div>
                <div style={{position: 'relative', height: 150}}>
                    <Bar data={{
                        labels: this.props.anomalyData.TIME,
                        datasets: [{
                            label: "Anomaly Score",
                            yAxisID: "Anomaly",
                            data: this.props.anomalyData[this.props.metric].score,
                            fill: true,
                            backgroundColor: this.props.anomalyData[this.props.metric].color,
                            datalabels: {
                                display: false,
                                // display: false,(context) => {
                                //     return this.state.chartData.anomalyData[context.dataIndex] >= 0.1
                                // },
                                formatter: (value, context) => {
                                    return this.props.anomalyData[this.props.metric].raw[context.dataIndex].toFixed(1);
                                },
                                color: 'black',
                                backgroundColor: 'rgba(255,255,255,0.5)',
                                borderColor: 'black',
                                font: {
                                    size: 11,
                                    color: 'black'
                                },
                                padding: 2,
                                align: 'end',
                                offset: 2,
                                anchor: 'end'
                            }
                        }, {
                            label: this.props.metric,
                            yAxisID: "Metric",
                            data: this.props.anomalyData[this.props.metric].raw,
                            fill: true,
                            borderColor: "rgb(20,66,214)",
                            backgroundColor: "rgba(53, 75, 178, 0.56)",
                            borderWidth: 1,
                            pointRadius: 0,
                            type: 'line',
                            datalabels: {
                                display: false
                            }
                        }],
                    }} options={{
                        maintainAspectRatio: false,
                        animation: {
                            duration: 0
                        },
                        events: [],
                        elements: {
                            line: {
                                tension: 0, // disables bezier curves
                            }
                        },
                        hover: {
                            animationDuration: 0, // duration of animations when hovering an item
                        },
                        responsiveAnimationDuration: 0, // animation duration after a resize,
                        legend: {
                            display: false
                        },
                        scales: {
                            xAxes: [{
                                display: true,//!(this.props.hideX),
                                ticks: {
                                    display: false,
                                    //autoSkip: false,
                                    //maxRotation: 90,
                                    //minRotation: 90,
                                    fontColor: "black"
                                },
                                scaleLabel: {
                                    display: false,
                                    labelString: "Time of Day",
                                    fontColor: 'black'
                                },
                                gridLines: {
                                    display: false
                                },
                            }],
                            yAxes: [{
                                id: 'Metric',
                                type: 'linear',
                                position: 'left',
                                ticks: {
                                    min: 0,
                                    fontColor: "black",
                                    max: METRIC_UPPER_BOUNDS[this.props.metric]
                                },
                                scaleLabel: {
                                    display: false,
                                    labelString: this.props.metric,
                                    fontColor: 'black'
                                },
                                afterFit: function (scaleInstance) {
                                    scaleInstance.width = 70; // sets the width to 100px
                                }
                            }, {
                                id: 'Anomaly',
                                type: 'linear',
                                position: 'right',
                                ticks: {
                                    max: 1,
                                    min: 0,
                                    display: false
                                },
                                scaleLabel: {
                                    display: false,
                                    labelString: "Anomaly Score"
                                },
                                gridLines: {
                                    display: false
                                }
                            }
                            ]
                        }
                    }} ref={(ref) => {
                        this.chart = ref
                    }}/>

                </div>
            </div>
        )
    }
}

const speedAnomalyCom = connect(state => {
    return {
        ...state.AnomalyInfo,
        index: state.AnomalyInfo.anomalyData.index
    };
})(SpeedAnomalyComponent);

export default speedAnomalyCom;