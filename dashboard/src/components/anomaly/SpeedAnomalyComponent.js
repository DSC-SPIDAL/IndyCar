import React from "react";
import AnomalySubscriber from "../../subscribers/AnomalySubscriber";
import {Line} from "react-chartjs-2";
import {SocketService} from "../../services/SocketService";

export default class SpeedAnomalyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            chartData: {
                speedData: [],
                anomalyData: [],
                labels: []
            },
            windowSize: 100
        };

        this.anamolySubscriber = new AnomalySubscriber();

        this.chart = undefined;

        this.chartUpdateInterval = -1;

        this.needChartUpdate = false;

        this.socket = new SocketService();
    }

    onReceiveChartData = (data) => {
        let chartData = this.state.chartData;

        let speedData = chartData.speedData;
        speedData.push(data.rawData);
        speedData.length > this.state.windowSize && speedData.splice(0, speedData.length - this.state.windowSize);

        let anomalyData = chartData.anomalyData;
        anomalyData.push(parseFloat(data.anomaly));
        anomalyData.length > this.state.windowSize && anomalyData.splice(0, anomalyData.length - this.state.windowSize);

        let labels = chartData.labels;
        labels.push(data.index);
        labels.length > this.state.windowSize && labels.splice(0, labels.length - this.state.windowSize);

        this.needChartUpdate = true;
    };

    componentDidMount() {
        console.log("Sending Join room Request");
        this.socket.send("EVENT_SUB", {
            roomName: this.props.carNumber + this.props.metric
        });
        this.socket.subscribe("anomaly", this.onReceiveChartData);

        this.chartUpdateInterval = setInterval(() => {
            if (this.chart && this.chart.chartInstance && this.needChartUpdate) {
                this.chart.chartInstance.update();
                this.needChartUpdate = false;
            }
        }, 1000 / 60);//24 frames per second
    }

    componentWillUnmount() {
        this.socket.send("EVENT_UNSUB", {
            roomName: this.props.carNumber + this.props.metric
        });
        this.socket.unsubscribe("anomaly", this.onReceiveChartData);
        clearInterval(this.chartUpdateInterval);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        return false;
    }

    render() {
        return (
            <div>
                <Line data={{
                    labels: this.state.chartData.labels,
                    datasets: [{
                        label: this.props.metric,
                        yAxisID: "Metric",
                        data: this.state.chartData.speedData,
                        fill: false,
                        borderColor: "#1565C0",
                        backgroundColor: "#1565C0",
                        borderWidth: 3,
                        pointRadius: 0
                    }, {
                        label: "Anomaly Score",
                        yAxisID: "Anomaly",
                        data: this.state.chartData.anomalyData,
                        fill: false,
                        borderColor: "#c62828",
                        backgroundColor: "#c62828",
                        borderWidth: 3,
                        pointRadius: 0,
                        steppedLine: true
                    }],
                }} options={{
                    animation: {
                        duration: 0
                    },
                    scales: {
                        xAxes: [{
                            ticks: {
                                display: true
                            },
                            scaleLabel: {
                                display: true,
                                labelString: "Time of Day (ms)"
                            }
                        }],
                        yAxes: [{
                            id: 'Metric',
                            type: 'linear',
                            position: 'left',
                            ticks: {
                                min: 0
                            },
                            scaleLabel: {
                                display: true,
                                labelString: this.props.metric
                            }
                        }, {
                            id: 'Anomaly',
                            type: 'linear',
                            position: 'right',
                            ticks: {
                                max: 1,
                                min: 0
                            },
                            scaleLabel: {
                                display: true,
                                labelString: "Anomaly Score"
                            }
                        }]
                    }
                }} ref={(ref) => {
                    this.chart = ref
                }}/>

            </div>
        )
    }
}