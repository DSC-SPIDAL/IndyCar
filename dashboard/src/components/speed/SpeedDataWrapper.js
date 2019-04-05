import React from "react";
import SpeedDataComponent from "./SpeedDataComponent";
import "./SpeedDataWrapper.css";
import CarInformationService, {CAR_INFO_LISTENER} from "../../services/CarInformationService";
import {SocketService} from "../../services/SocketService";
import {Button, ButtonGroup} from "@blueprintjs/core";

const VIEW_MODE = {
    ALL: 10000,
    TOP_3: 3,
    TOP_5: 5
};

/**
 * @author Chathura Widanage
 */
export default class SpeedDataWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            carDistances: [],
            carsList: {},
            ranks: [],
            viewMode: VIEW_MODE.TOP_3
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


        let speedDataComponents = this.state.ranks
            .slice(0, this.state.viewMode)
            .map((rankObj, index) => {
                return <SpeedDataComponent carNumber={rankObj.carNumber}
                                           carData={rankObj}
                                           key={rankObj.carNumber}
                                           rank={index + 1}/>
            });

        return (
            <div className="speed-data-wrapper">
                {
                    this.state.ranks && this.state.ranks.length > 0 &&
                    <div className="speed-data-wrapper-controls">
                        <ButtonGroup>

                            <Button active={this.state.viewMode === VIEW_MODE.TOP_3} onClick={() => {
                                this.changeViewMode(VIEW_MODE.TOP_3)
                            }}>Show Top 3</Button>
                            <Button active={this.state.viewMode === VIEW_MODE.TOP_5} onClick={() => {
                                this.changeViewMode(VIEW_MODE.TOP_5)
                            }}>Show Top 5</Button>
                            <Button active={this.state.viewMode === VIEW_MODE.ALL} onClick={() => {
                                this.changeViewMode(VIEW_MODE.ALL)
                            }}>Show All</Button>
                        </ButtonGroup>
                    </div>
                }
                <div className="speed-data-wrapper-cards">
                    {speedDataComponents}
                </div>
            </div>
        );
    }
}