import React from "react";
import {Card} from "@blueprintjs/core";
import "./VideosWrapper.css"

export default class VideosWrapper extends React.Component {

    render() {
        return (<div className="ic-section ic-video-wrapper">
                <Card>
                    <h5>Related Videos</h5>
                    <div className="ic-videos">
                        <div className="ic-video-container">
                            <iframe
                                src="https://www.youtube.com/embed/FJeMKnch868"
                                className="ic-video" allow="encrypted-media" allowFullScreen/>
                        </div>
                        <div className="ic-video-container">
                            <iframe
                                className="ic-video"
                                src="https://www.youtube.com/embed/iWqrvfCJaCI"
                                allow="encrypted-media" allowFullScreen/>
                        </div>
                    </div>
                </Card>
            </div>
        );
    }
}