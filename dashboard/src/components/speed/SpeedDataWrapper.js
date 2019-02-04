import React from "react";
import SpeedDataComponent from "./SpeedDataComponent";
import "./SpeedDataWrapper.css";
import CarInformationService, {CAR_INFO_LISTENER} from "../../services/CarInformationService";

/**
 * @author Chathura Widanage
 */
export default class SpeedDataWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carDistances: [],
            carsList: {}
        };
    }

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
        // CarInformationService.getCarList().then(response => {
        //     console.log(response.data);
        //     Promise.all(response.data.map(CarInformationService.getCarRank)).then(response => {
        //         console.log("R", response);
        //     });
        // });
        //
        // let currentDistances = {};
        // CarInformationService.subscribeToRankChanges((carDistances) => {
        //     currentDistances = carDistances;
        // });
        //
        // setInterval(() => {
        //     this.setState({
        //         carDistances: Object.values(currentDistances)
        //     })
        // }, 5000);
    }

    render() {

        // let sorted = this.state.carDistances.sort((a, b) => {
        //     return b.distance - a.distance;
        // });
        //
        // if (sorted.length < 3) {
        //     return null;
        // }

        let selectedCars = Object.keys(this.state.carsList);

        selectedCars = selectedCars.slice(0, Math.min(3, selectedCars.length));

        let speedDataComponents = []/*selectedCars.map((carNumber, index) => {
            return <SpeedDataComponent carNumber={carNumber}
                                       carData={{}}
                                       key={carNumber}
                                       rank={index + 1}/>
        });*/

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