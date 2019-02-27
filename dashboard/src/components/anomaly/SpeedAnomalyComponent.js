import React from "react";
import AnomalySubscriber from "../../subscribers/AnomalySubscriber";
import {Line} from "react-chartjs-2";
import "chartjs-plugin-datalabels"
import {SocketService} from "../../services/SocketService";

export default class SpeedAnomalyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            chartData: {
                speedData: [],
                anomalyData: [],
                labels: [],
                anomalyLabels: [],
            },
            windowSize: 25,
            anomalyLabelsCount: 0
        };

        this.anamolySubscriber = new AnomalySubscriber();

        this.chart = undefined;

        this.chartUpdateInterval = -1;

        this.needChartUpdate = false;

        this.socket = new SocketService();
    }

    onReceiveChartData = (data) => {
        console.log(data);
        let anomalyObject = data.anomalies[this.props.metric];
        let chartData = this.state.chartData;

        let speedData = chartData.speedData;
        speedData.push(anomalyObject.rawData);
        speedData.length > this.state.windowSize && speedData.splice(0, speedData.length - this.state.windowSize);

        let anomalyData = chartData.anomalyData;
        anomalyData.push(anomalyObject.anomaly);
        anomalyData.length > this.state.windowSize && anomalyData.splice(0, anomalyData.length - this.state.windowSize);

        let anomalyLabels = chartData.anomalyLabels;
        if (data.anomalyLabel) {
            this.state.anomalyLabelsCount = this.state.windowSize
        }
        this.state.anomalyLabelsCount--;
        anomalyLabels.push(data.anomalyLabel);
        anomalyLabels.length > this.state.windowSize && anomalyLabels.splice(0, anomalyLabels.length - this.state.windowSize);

        let labels = chartData.labels;
        labels.push(data.timeOfDayString);
        labels.length > this.state.windowSize && labels.splice(0, labels.length - this.state.windowSize);

        this.needChartUpdate = true;
    };

    componentDidMount() {
        console.log("Sending Join room Request");
        this.socket.subscribe("anomaly_" + this.props.carNumber, this.onReceiveChartData);

        this.chartUpdateInterval = setInterval(() => {
            if (this.chart && this.chart.chartInstance && this.needChartUpdate) {
                this.chart.chartInstance.update();
                this.needChartUpdate = false;
            }
        }, 1000 / 10);//10 frames per second
    }

    componentWillUnmount() {
        this.socket.unsubscribe("anomaly_" + this.props.carNumber, this.onReceiveChartData);
        clearInterval(this.chartUpdateInterval);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        return false;
    }

    render() {

        return (
            <div style={{position: 'relative', height: !(this.props.hideX) ? 265 : 150}}>
                <Line data={{
                    labels: this.state.chartData.labels,
                    datasets: [{
                        label: this.props.metric,
                        yAxisID: "Metric",
                        data: this.state.chartData.speedData,
                        fill: false,
                        borderColor: this.props.rawDataColor,
                        backgroundColor: this.props.rawDataColor,
                        borderWidth: 3,
                        pointRadius: 0,
                        datalabels: {
                            display: false
                        }
                    }, {
                        label: "Anomaly Score",
                        yAxisID: "Anomaly",
                        data: this.state.chartData.anomalyData,
                        fill: false,
                        borderColor: "#c62828",
                        backgroundColor: "#c62828",
                        borderWidth: 3,
                        pointRadius: 0,
                        steppedLine: true,
                        datalabels: {
                            display: (context) => {
                                return this.state.anomalyLabelsCount > 0 && !!(this.state.chartData.anomalyLabels[context.dataIndex])
                                    && context.dataIndex % 5 === 0;
                            },
                            formatter: (value, context) => {
                                return this.state.chartData.anomalyLabels[context.dataIndex] ?
                                    this.state.chartData.anomalyLabels[context.dataIndex].label : "";
                            },
                            color: '#293742',
                            backgroundColor: 'white',
                            borderColor: 'white',
                            borderRadius: 4,
                            font: {
                                weight: 'bold'
                            },
                            padding: 5
                        }
                    }],
                }} options={{
                    maintainAspectRatio: false,
                    animation: {
                        duration: 0
                    },
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
                                minRotation: 90
                            },
                            scaleLabel: {
                                display: true,
                                labelString: "Time of Day"
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