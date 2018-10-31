import React from "react";
import SpeedDataComponent from "./SpeedDataComponent";
import "./SpeedDataWrapper.css";
import CarInformationService from "../../services/CarInformationService";

/**
 * @author Chathura Widanage
 */
export default class SpeedDataWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carDistances: []
        };
    }

    componentDidMount() {
        CarInformationService.getCarList().then(response => {
            console.log(response.data);
            Promise.all(response.data.map(CarInformationService.getCarRank)).then(response => {
                console.log("R", response);
            });
        });

        let currentDistances = {};
        CarInformationService.subscribeToRankChanges((carDistances) => {
            currentDistances = carDistances;
        });

        setInterval(() => {
            this.setState({
                carDistances: Object.values(currentDistances)
            })
        }, 5000);
    }

    render() {

        let sorted = this.state.carDistances.sort((a, b) => {
            return b.distance - a.distance;
        });

        if (sorted.length < 3) {
            return null;
        }

        let speedDataComponents = sorted.slice(0, 3).map((carDistanceData, index) => {
            return <SpeedDataComponent carNumber={carDistanceData.carNumber}
                                       carData={carDistanceData}
                                       rank={index + 1}/>
        });

        return (
            <div className="speed-data-wrapper">
                {speedDataComponents}
                {/*<SpeedDataComponent carNumber={1} rank={1}/>*/}
                {/*<SpeedDataComponent carNumber={2} rank={2}/>*/}
                {/*<SpeedDataComponent carNumber={3} rank={3}/>*/}
                {/*<SpeedDataComponent carNumber={4} rank={4}/>*/}
            </div>
        );
    }
}