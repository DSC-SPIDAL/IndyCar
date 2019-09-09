import React from "react";
import "./PlayerSelectionComponent.css";
import {Spinner} from "@blueprintjs/core";
import TEAM_ICO from "./img/teamIcon.svg";
import CAR_ICO from "./img/carNumberIcon.svg";
import {connect} from "react-redux";
import {ACTION_PLAYER_CHANGED} from "../../reducers/AnomalyReducer";

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
        let rank = this.props.ranks[this.state.selected];

        return (
            <div>
                <div className="player-selection-wrapper">
                    <div className="player-selection">
                        <div>
                            <span className="player-selection-car-label">Car</span>
                            <div className="pt-select">
                                <select onChange={this.changePlayer} value={this.state.selected}>
                                    {
                                        options
                                    }
                                </select>
                            </div>
                        </div>
                        <div>
                            <span className="player-selection-car-label">Current Position : {rank ||
                            <Spinner small={true}/>}</span>
                        </div>
                    </div>
                    <div className="player-information">
                        <div className="driver-info">
                            <h4>Driver Information</h4>
                            <div className="driver-info-content">
                                <div className="player-image"
                                     style={{backgroundImage: `url(img/drivers/${this.state.selected}.jpg)`}}/>
                                <div>
                                    <div className="player-name">
                                        {selectedCar.driverName}
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
                                </div>
                            </div>
                        </div>
                        <div className="car-info">
                            <h4>Car Information</h4>
                            <div className="player-engine">
                                {selectedCar.engine}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    changePlayer = (event) => {
        this.setState({
            selected: event.target.value
        });

        this.props.dispatch({
            type: ACTION_PLAYER_CHANGED,
            player: event.target.value
        });
    }
}

const playerSelection = connect(state => {
    let players = {};
    let ranks = {};
    if (state.PlayerInfo.players) {
        players = state.PlayerInfo.players;
    }

    if (state.PlayerInfo.ranks) {
        ranks = state.PlayerInfo.ranks.carToRank;
    }
    return {players, ranks}
})(PlayerSelectionComponent);

export default playerSelection;