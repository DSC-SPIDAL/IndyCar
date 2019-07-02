import React from "react";
import AnomalySubscriber from "../../subscribers/AnomalySubscriber";
import {Bar} from "react-chartjs-2";
import "chartjs-plugin-datalabels"
import {SocketService} from "../../services/SocketService";
import TimeListenerService from "../../services/TimeListenerService";

const METRIC_UPPER_BOUNDS = {
    SPEED: 240,
    RPM: 12600,
    THROTTLE: 115
};

export default class SpeedAnomalyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            chartData: {
                speedData: [],
                anomalyData: [],
                anomalyDataRaw: [],
                anomalyColor: [],
                labels: [],
                anomalyLabels: [],
            },
            windowSize: Math.min(50, 50 * window.innerWidth / 1200),
            currentAnomalyLabel: {
                id: undefined,
                count: 0
            }
        };

        this.anamolySubscriber = new AnomalySubscriber();

        this.chart = undefined;

        this.chartLastUpdated = -1;

        this.socket = new SocketService();

        this.timeService = TimeListenerService;
    }

    onReceiveChartData = (data) => {
        let anomalyObject = data.anomalies[this.props.metric];
        let chartData = this.state.chartData;

        let speedData = chartData.speedData;
        speedData.push(anomalyObject.rawData);
        // speedData.length > this.state.windowSize && speedData.splice(0, speedData.length - this.state.windowSize);

        let anomalyData = chartData.anomalyData;
        let anomalyDataRaw = chartData.anomalyDataRaw;
        anomalyDataRaw.push(anomalyObject.anomaly);

        //colors
        let anomalyColor = chartData.anomalyColor;
        if (anomalyObject.anomaly > 0.5) {
            anomalyColor.push("#d81f29");
            anomalyData.push(0.2);
        } else if (anomalyObject.anomaly > 0.3) {
            anomalyColor.push("#f3b153");
            anomalyData.push(0.1);
        } else {
            anomalyColor.push("#8fe588");
            anomalyData.push(0.05);
        }
        //anomalyData.length > this.state.windowSize && anomalyData.splice(0, anomalyData.length - this.state.windowSize);
        //anomalyColor.length > this.state.windowSize && anomalyColor.splice(0, anomalyColor.length - this.state.windowSize);


        /*let anomalyLabels = chartData.anomalyLabels;

        if (data.anomalyLabel && this.state.currentAnomalyLabel.id !== data.anomalyLabel.uuid) {
            this.state.currentAnomalyLabel.id = data.anomalyLabel.uuid;
            this.state.currentAnomalyLabel.count = 0;
        }

        if (data.anomalyLabel) {
            this.state.currentAnomalyLabel.count++;
        } else {
            this.state.currentAnomalyLabel.id = undefined;
            this.state.currentAnomalyLabel.count = -1;
        }

        if (this.state.currentAnomalyLabel.count % 5 === 0) {
            anomalyLabels.push(data.anomalyLabel);
        } else {
            anomalyLabels.push(undefined);
        }
        anomalyLabels.length > this.state.windowSize && anomalyLabels.splice(0, anomalyLabels.length - this.state.windowSize);*/

        let labels = chartData.labels;
        labels.push(data.timeOfDayString);
        this.timeService.notifyListeners(data.timeOfDayString);
        //labels.length > this.state.windowSize && labels.splice(0, labels.length - this.state.windowSize);

        if (this.chart && this.chart.chartInstance && (Date.now() - this.chartLastUpdated) > 100) {
            //resize to window
            speedData.length > this.state.windowSize && speedData.splice(0, speedData.length - this.state.windowSize);
            anomalyData.length > this.state.windowSize && anomalyData.splice(0, anomalyData.length - this.state.windowSize);
            anomalyDataRaw.length > this.state.windowSize && anomalyDataRaw.splice(0, anomalyDataRaw.length - this.state.windowSize);
            anomalyColor.length > this.state.windowSize && anomalyColor.splice(0, anomalyColor.length - this.state.windowSize);
            labels.length > this.state.windowSize && labels.splice(0, labels.length - this.state.windowSize);

            this.chart.chartInstance.update();
            this.chartLastUpdated = Date.now();
        }
    };

    componentDidMount() {
        console.log("Sending Join room Request");
        this.socket.subscribe("anomaly_" + this.props.carNumber, this.onReceiveChartData);
    }

    componentWillUnmount() {
        this.socket.unsubscribe("anomaly_" + this.props.carNumber, this.onReceiveChartData);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        return false;
    }

    render() {

        return (
            <div style={{position: 'relative', height: !(this.props.hideX) ? 265 : 150}}>
                <Bar data={{
                    labels: this.state.chartData.labels,
                    datasets: [{
                        label: this.props.metric,
                        yAxisID: "Metric",
                        data: this.state.chartData.speedData,
                        fill: false,
                        borderColor: this.props.rawDataColor,
                        backgroundColor: this.props.rawDataColor,
                        borderWidth: 1,
                        pointRadius: 0,
                        type: 'line',
                        datalabels: {
                            display: false
                        }
                    }, {
                        label: "Anomaly Score",
                        yAxisID: "Anomaly",
                        data: this.state.chartData.anomalyData,
                        fill: true,
                        backgroundColor: this.state.chartData.anomalyColor,
                        // datalabels: {
                        //     display: false
                        // }
                        datalabels: {
                            display: (context) => {
                                return this.state.chartData.anomalyData[context.dataIndex] >= 0.1
                            },
                            formatter: (value, context) => {
                                return this.state.chartData.anomalyDataRaw[context.dataIndex].toFixed(1);
                            },
                            color: 'black',
                            backgroundColor: 'white',
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
                            display: !(this.props.hideX),
                            ticks: {
                                display: true,
                                autoSkip: false,
                                maxRotation: 90,
                                minRotation: 90,
                                fontColor: "black"
                            },
                            scaleLabel: {
                                display: true,
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
                                display: true,
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
        )
    }
}