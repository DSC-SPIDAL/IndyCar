import React from "react";
import TrackComponent from "../track/TrackComponent";
import {Tab, Tabs} from "@blueprintjs/core";
import VideosWrapper from "../videos/VideosWrapper";

export default class OverviewComponent extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <Tabs renderActiveTabPanelOnly={true}>
                    <Tab id="Track" title="Map View" panel={<TrackComponent selectedCarNumber={"19"}/>}/>
                    <Tab id="Broadcast" title="Broadcast" panel={<VideosWrapper/>}/>
                </Tabs>
            </div>
        );
    }

}