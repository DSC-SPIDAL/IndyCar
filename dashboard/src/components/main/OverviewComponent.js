import React from "react";
import TrackComponent from "../track/TrackComponent";
import {Card, Tab, Tabs} from "@blueprintjs/core";
import VideosWrapper from "../videos/VideosWrapper";
import "./OverviewComponent.css";
import LeaderboardComponent from "../speed/LeaderboardComponent";

export default class OverviewComponent extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="overview-wrapper">
                <div className="overview-top">
                    <div className="overview-track">
                        <Tabs renderActiveTabPanelOnly={true}>
                            <Tab id="Track" title="Map View" panel={<TrackComponent selectedCarNumber={"19"}/>}/>
                            <Tab id="Broadcast" title="Broadcast" panel={<VideosWrapper/>}/>
                        </Tabs>
                    </div>
                    <div className="overview-info">
                        <Card>
                            <h4>RACE INFO</h4>
                            <hr className="red-line"/>
                            <table className="overview-info-table">
                                <tbody>
                                <tr>
                                    <td width="60%">
                                        LAPS COMPLETED
                                    </td>
                                    <td>
                                        49/200
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        CURRENT LEADER
                                    </td>
                                    <td>
                                        Rossi[7]
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        LEADER CHANGES
                                    </td>
                                    <td>
                                        7
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        Fastest Lap Time
                                    </td>
                                    <td>
                                        72.12
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </Card>
                        <Card>
                            <h4>PREDICTED OUTCOME</h4>
                            <hr className="red-line"/>
                            <p>
                                Win for Castroneves [3] followed closely by Will Power [12]. Rossi [6] underperforms,
                                coming in at 6th position.
                            </p>
                        </Card>
                    </div>
                </div>
                <LeaderboardComponent/>
            </div>
        );
    }

}