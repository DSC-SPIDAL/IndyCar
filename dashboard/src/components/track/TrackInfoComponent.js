import React from "react";
import {SocketService} from "../../services/SocketService";

export default class TrackInfoComponent extends React.Component {

    constructor(props) {
        super(props);
        this.socketService = new SocketService();
        this.state = {
            danger: {},
            warn: {}
        };

        this.timeouts = {};
    }

    onAnomalyClass = (anoClass) => {
        if (this.timeouts[anoClass.carNumber]) {
            clearTimeout(this.timeouts[anoClass.carNumber]);
        }

        if (anoClass.anomalyClass === 1) {
            this.setState({
                warn: {...this.state.warn, [anoClass.carNumber]: true},
                danger: {...this.state.danger, [anoClass.carNumber]: false}
            });

            this.timeouts[anoClass.carNumber] = setTimeout(() => {
                this.setState({
                    warn: {...this.state.warn, [anoClass.carNumber]: false}
                });
            }, 5000);
        } else {
            this.setState({
                warn: {...this.state.warn, [anoClass.carNumber]: false},
                danger: {...this.state.danger, [anoClass.carNumber]: true}
            });

            this.timeouts[anoClass.carNumber] = setTimeout(() => {
                this.setState({
                    danger: {...this.state.danger, [anoClass.carNumber]: false}
                });
            }, 5000);
        }
    };

    componentWillUnmount() {
        this.socketService.unsubscribe("anomaly_class", this.onAnomalyClass);
    }

    componentDidMount() {
        this.socketService.subscribe("anomaly_class", this.onAnomalyClass);
    }

    render() {

        let warns = Object.keys(this.state.warn).filter(key => this.state.warn[key]).map((carNumber, index) => {
            return <img src={`img/cars/car_${('' + carNumber).padStart(2, '0')}.png`} width="50" className="car-img"
                        key={index}/>
        });

        let danger = Object.keys(this.state.danger).filter(key => this.state.danger[key]).map((carNumber, index) => {
            return <img src={`img/cars/car_${('' + carNumber).padStart(2, '0')}.png`} width="50" className="car-img"
                        key={index}/>
        });

        return (
            <div className="ic-track-info" style={this.props.style}>
                <div className="ic-track-info-cat-wrapper">
                    <div className="title warn">
                        WARN
                    </div>
                    <div className="content warn">
                        {warns}
                    </div>
                </div>
                <div className="ic-track-info-cat-wrapper">
                    <div className="title danger">
                        DANGER
                    </div>
                    <div className="content danger">
                        {danger}
                    </div>
                </div>
            </div>
        )
    }
}