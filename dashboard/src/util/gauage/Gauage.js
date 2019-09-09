import React from "react";

export default class Gauage extends React.Component {

    constructor(props) {
        super(props);
        this.canvas = null;
        this.gauge = null;
    }


    shouldComponentUpdate(nextProps, nextState, nextContext) {
        return false;
    }

    componentWillReceiveProps(nextProps, nextContext) {
        this.gauge.set(nextProps.value);
    }

    componentDidMount() {
        let opts = {
            angle: 0.07, // The span of the gauge arc
            lineWidth: 0.44, // The line thickness
            radiusScale: 1, // Relative radius
            pointer: {
                length: 0.6, // // Relative to gauge radius
                strokeWidth: 0.035, // The thickness
                color: '#f1545d' // Fill color
            },
            limitMax: false,     // If false, max value increases automatically if value > maxValue
            limitMin: false,     // If true, the min value of the gauge will be fixed
            colorStart: 'rgba(53, 75, 178, 0.56)',   // Colors
            colorStop: 'rgba(53, 75, 178, 0.56)',    // just experiment with them
            strokeColor: '#E0E0E0',  // to see which ones work best for you
            generateGradient: true,
            highDpiSupport: true,     // High resolution support

        };
        this.gauge = new window.Gauge(this.canvas).setOptions(opts);
        this.gauge.maxValue = this.props.maxValue; // set max gauge value
        this.gauge.setMinValue(0);  // Prefer setter over gauge.minValue = 0
        this.gauge.animationSpeed = 17; // set animation speed (32 is default value)
        this.gauge.set(0);
    }


    render() {
        return (
            <canvas ref={(ref => {
                this.canvas = ref;
            })} className="gauge-canvas"/>
        );
    }
}