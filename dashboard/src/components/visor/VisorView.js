import React from "react";
import IMG from "./maxresdefault-2.jpg";
import "./VisorView.css";

export default class VisorView extends React.Component {
    render() {
        return (
            <div className="visor-view-wrapper">
                <div className="car-image">
                    <img src={IMG} width="100%" alt="car image"/>
                    <div className="positions-wrapper">
                        <div className="current-position">
                            Current Position <span>6th</span>
                        </div>
                        <div className="predicted-position">
                            Predicted Position <span>7th</span>
                        </div>
                    </div>
                </div>
                <div className="best-time-widget">
                    <div className="visor-view-row">
                        <div>
                            BEST TIME (s)
                        </div>
                        <div>
                            112.82
                        </div>
                    </div>
                    <div className="visor-view-row">
                        <div>
                            CURRENT (s)
                        </div>
                        <div>
                            80.83
                        </div>
                    </div>
                </div>

                <div className="lap-history-widget">
                    <div className="visor-view-row">
                        <div>
                            BEST TIME (s)
                        </div>
                        <div>
                            112.82
                        </div>
                    </div>
                    <div className="visor-view-row">
                        <div>
                            CURRENT (s)
                        </div>
                        <div>
                            80.83
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}