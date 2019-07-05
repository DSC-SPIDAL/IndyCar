import React from "react";
import "./PlayerSelectionComponent.css";
import {Navbar, NavbarDivider, NavbarGroup, Alignment, Spinner} from "@blueprintjs/core";
import TEAM_ICO from "./img/teamIcon.svg";
import CAR_ICO from "./img/carNumberIcon.svg";
import RANK_ICO from "./img/rank.png";
import CarInformationService, {CAR_INFO_LISTENER, CAR_RANK_LISTENER} from "../../services/CarInformationService";
import TrackComponent from "../track/TrackComponent";
import LeaderboardAndVideo from "../LeaderboardAndVideo";
import AnomalyWrapper from "../anomaly/AnomalyWrapper";
import LapTimesComponent from "../laps/LapTimesComponent";

/**
 * @author Chathura Widanage
 */
export default class PlayerSelectionComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            cars: {},
            selected: 21,
            rankMap: {}
        };
    }

    componentDidMount() {
        CarInformationService.addEventListener(CAR_INFO_LISTENER, (player) => {
            let carNumber = parseInt(player.carNumber);
            if (!isNaN(carNumber)) {
                this.setState({
                    cars: {...this.state.cars, [carNumber]: player}
                });
            }
        });

        CarInformationService.addEventListener(CAR_RANK_LISTENER, (rankMap) => {
            this.setState({rankMap: rankMap});
        })
    }


    render() {
        if (!this.state.cars[this.state.selected]) {
            return null;
        }

        let options = Object.keys(this.state.cars).map(carNumber => {
            return <option value={carNumber}
                           key={carNumber}>{carNumber}</option>
        });

        let selectedCar = this.state.cars[this.state.selected];

        return (
            <div>
                <div className="player-selection-wrapper">
                    <Navbar style={{height: '100px'}} align={Alignment.LEFT}>
                        <NavbarGroup className="player-selection" style={{height: '100px'}}>
                            <span className="player-selection-car-label">Car</span>
                            <div className="pt-select">
                                <select onChange={this.changePlayer} value={this.state.selected}>
                                    {
                                        options
                                    }
                                </select>
                            </div>
                        </NavbarGroup>
                        <NavbarGroup style={{height: '100px'}}>
                            <NavbarDivider style={{height: 80}}/>
                        </NavbarGroup>
                        <NavbarGroup className="player-information" style={{height: '100px'}}>
                            <div className="player-image"
                                 style={{backgroundImage: `url(img/drivers/${this.state.selected}.jpg)`}}/>
                            <div className="player-name-and-engine">
                                <div className="player-name">
                                    {selectedCar.driverName}
                                </div>
                                <div className="player-engine">
                                    {selectedCar.engine}
                                </div>
                            </div>
                            <div className="player-team">
                                <div>
                                    <img src={TEAM_ICO} height={25}/>
                                </div>
                                <div className="player-team-name">
                                    {selectedCar.team}
                                </div>
                            </div>
                            <div className="player-car">
                                <div>
                                    <img src={CAR_ICO} height={15}/>
                                </div>
                                <div className="player-car-name">
                                    {this.state.selected}
                                </div>
                            </div>
                            <div className="player-rank">
                                <div>
                                    <img src={RANK_ICO} height={25}/>
                                </div>
                                <div className={"player-rank-name"}>
                                    {selectedCar.rank || <Spinner small={true}/>}
                                </div>
                            </div>
                        </NavbarGroup>
                    </Navbar>
                </div>

                <TrackComponent selectedCarNumberselectedCarNumber={this.state.selected}/>
                <LeaderboardAndVideo/>
                <AnomalyWrapper selectedCarNumber={this.state.selected}/>
                <LapTimesComponent selectedCarNumber={this.state.selected}/>
            </div>
        );
    }

    changePlayer = (event) => {
        this.setState({
            selected: event.target.value
        })
    }
}