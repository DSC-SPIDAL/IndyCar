import React from "react";
import LeaderboardComponent from "./speed/LeaderboardComponent";
import VideosWrapper from "./videos/VideosWrapper";
import "./LeaderboardAndVideo.css";

export default class LeaderboardAndVideo extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        return (
            <div className="lb-and-video-wrapper">
                <LeaderboardComponent/>
                <VideosWrapper/>
            </div>
        );
    }

}