import React from "react";
import "./LeaderboardComponent.css";
import LeaderboardItem from "./LeaderboardItem";
import {Spinner} from "@blueprintjs/core";
import {connect} from "react-redux";

const VIEW_MODE = {
    ALL: 10000,
    TOP_3: 3,
    TOP_5: 5
};

/**
 * @author Chathura Widanage
 */
class LeaderboardComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            viewMode: VIEW_MODE.ALL
        };
    }

    render() {
        let leaderBoardItems = this.props.ranks
            .slice(0, this.state.viewMode)
            .map((carNumber, index) => {
                return <LeaderboardItem carNumber={carNumber}
                                        key={carNumber}
                                        rank={index + 1}/>
            });

        let stillLoading = this.props.ranks.length === 0;

        return (
            <div className="leader-board-wrapper">
                {stillLoading ?
                    <Spinner large={true} className="leader-board-loader"/>
                    :
                    <table>
                        <thead>
                        <tr>
                            <td>
                                Car No.
                            </td>
                            <td>
                                Driver
                            </td>
                            <td>
                                Current Pos.
                            </td>
                            <td>
                                Predicted Pos.
                            </td>
                            <td>
                                Fastest Lap(s)
                            </td>
                            <td>
                                Last Lap(s)
                            </td>
                        </tr>
                        </thead>
                        <tbody>
                        {leaderBoardItems}
                        </tbody>
                    </table>
                }
            </div>
        );
    }
}

const LeaderBoard = connect(state => {
    let ranks = [];
    if (state.PlayerInfo && state.PlayerInfo.ranks) {
        ranks = Object.keys(state.PlayerInfo.ranks.carToRank);
    }
    return {
        ranks: ranks
    }
})(LeaderboardComponent);

export default LeaderBoard;