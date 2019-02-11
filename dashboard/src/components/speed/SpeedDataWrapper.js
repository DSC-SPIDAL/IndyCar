import React from "react";
import SpeedDataComponent from "./SpeedDataComponent";
import "./SpeedDataWrapper.css";
import CarInformationService, {CAR_INFO_LISTENER} from "../../services/CarInformationService";
import {SocketService} from "../../services/SocketService";

/**
 * @author Chathura Widanage
 */
export default class SpeedDataWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carDistances: [],
            carsList: {},
            ranks: []
        };
        this.socket = new SocketService();
    }

    componentWillUnmount() {
        this.socket.unsubscribe("ranks", this.onRankUpdate)
    }


    onRankUpdate = (rankUpdate) => {
        this.setState({
            ranks: rankUpdate
        });
    };

    componentDidMount() {
        CarInformationService.addEventListener(CAR_INFO_LISTENER, (info) => {
            let carsList = this.state.carsList;
            carsList[info.carNumber] = info;
            this.setState({
                carsList
            })
        });

        let carsList = CarInformationService.getCarList();
        this.setState({
            carsList
        });

        this.socket.subscribe("ranks", this.onRankUpdate);
    }

    render() {

        let speedDataComponents = this.state.ranks.map((rankObj, index) => {
            return <SpeedDataComponent carNumber={rankObj.carNumber}
                                       carData={{}}
                                       key={rankObj.carNumber}
                                       rank={index + 1}/>
        });

        return (
            <div className="speed-data-wrapper">
                {speedDataComponents}
            </div>
        );
    }
}