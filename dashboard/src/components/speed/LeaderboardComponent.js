import React from "react";
import "./LeaderboardComponent.css";
import CarInformationService, {CAR_INFO_LISTENER} from "../../services/CarInformationService";
import {SocketService} from "../../services/SocketService";
import LeaderboardItem from "./LeaderboardItem";
import {Spinner} from "@blueprintjs/core";

const VIEW_MODE = {
    ALL: 10000,
    TOP_3: 3,
    TOP_5: 5
};

/**
 * @author Chathura Widanage
 */
export default class LeaderboardComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carDistances: [],
            carsList: {},
            ranks: [],
            viewMode: VIEW_MODE.ALL
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
            if (!carsList[info.carNumber]) {
                carsList[info.carNumber] = info;
                this.setState({
                    carsList
                });
            }
        });

        let carsList = CarInformationService.getCarList();
        this.setState({
            carsList
        });

        this.socket.subscribe("ranks", this.onRankUpdate);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        let oldRanks = this.state.ranks;
        let newRanks = nextState.ranks;

        if (oldRanks.length !== newRanks.length || this.state.viewMode !== nextState.viewMode) {
            return true;
        } else {
            for (let i = 0; i < oldRanks.length; i++) {
                if (oldRanks[i].carNumber !== newRanks[i].carNumber) {
                    return true;
                }
            }
        }
        return false;
    }

    changeViewMode = (viewMode) => {
        console.log("Changing view mode", viewMode);
        this.setState({
            viewMode: viewMode
        })
    };

    render() {


        let leaderBoardItems = this.state.ranks
            .slice(0, this.state.viewMode)
            .map((rankObj, index) => {
                return <LeaderboardItem carNumber={rankObj.carNumber}
                                        carData={rankObj}
                                        key={rankObj.carNumber}
                                        rank={index + 1}/>
            });

        let stillLoading = leaderBoardItems.length === 0;

        return (
            <div className="leader-board-wrapper">
                <div className="leader-board-title">
                    <div className="leader-board-title-main">
                        Leaderboard
                    </div>
                    <div className="leader-board-title-sub">
                        Last lap time(s)
                    </div>
                </div>
                {stillLoading ?
                    <Spinner large={true} className="leader-board-loader"/>
                    :
                    <div className="leader-board-list">
                        {leaderBoardItems}
                    </div>
                }
            </div>
        );
    }
}