import React, {Component} from 'react';
import HeaderComponent from "./components/header/HeaderComponent";
import "./App.css";
import SpeedDataWrapper from "./components/speed/SpeedDataWrapper";
import TrackComponent from "./components/track/TrackComponent";
import LapTimesComponent from "./components/laps/LapTimesComponent";
import AnomalyWrapper from "./components/anomaly/AnomalyWrapper";
import VideosWrapper from "./components/videos/VideosWrapper";
import LeaderboardAndVideo from "./components/LeaderboardAndVideo";

class App extends Component {
    render() {
        return (
            <div className="App">
                <HeaderComponent/>
                {/*<TrackCanvasComponent/>*/}
                <TrackComponent/>
                <LeaderboardAndVideo/>
                {/*<SpeedDataWrapper/>*/}
                <AnomalyWrapper/>
                {/*<VideosWrapper/>*/}
                <LapTimesComponent/>
            </div>
        );
    }
}

export default App;
