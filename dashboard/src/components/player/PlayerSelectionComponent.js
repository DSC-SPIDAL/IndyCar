import React from "react";
import "./PlayerSelectionComponent.css";
import {Alignment, Navbar, NavbarDivider, NavbarGroup, Spinner} from "@blueprintjs/core";
import TEAM_ICO from "./img/teamIcon.svg";
import CAR_ICO from "./img/carNumberIcon.svg";
import RANK_ICO from "./img/rank.png";
import AnomalyWrapper from "../anomaly/AnomalyWrapper";
import LapTimesComponent from "../laps/LapTimesComponent";
import {connect} from "react-redux";

/**
 * @author Chathura Widanage
 */
class PlayerSelectionComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            selected: 21
        };
    }

    render() {
        if (!this.props.players[this.state.selected]) {
            return null;
        }

        let options = Object.values(this.props.players).map(player => {
            return <option value={player.carNumber}
                           key={player.carNumber}>{player.carNumber}</option>
        });

        let selectedCar = this.props.players[this.state.selected];

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

                {/*<TrackComponent selectedCarNumber={this.state.selected}/>*/}
                {/*<LeaderboardAndVideo/>*/}
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

const playerSelection = connect(state => {
    let players = {};
    if (state.PlayerInfo.players) {
        players = state.PlayerInfo.players;
    }
    return {players}
})(PlayerSelectionComponent);

export default playerSelection;