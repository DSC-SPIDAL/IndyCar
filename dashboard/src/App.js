import React, {Component} from 'react';
import HeaderComponent from "./components/header/HeaderComponent";
import "./App.css";
import SpeedDataWrapper from "./components/speed/SpeedDataWrapper";
import TrackComponent from "./components/track/TrackComponent";
import LapTimesComponent from "./components/laps/LapTimesComponent";
import AnomalyWrapper from "./components/anomaly/AnomalyWrapper";
import VideosWrapper from "./components/videos/VideosWrapper";
import LeaderboardAndVideo from "./components/LeaderboardAndVideo";
import PlayerSelectionComponent from "./components/player/PlayerSelectionComponent";
import Footer from "./components/footer/Footer";
import {HashRouter as Router, Route} from "react-router-dom";
import OverviewComponent from "./components/main/OverviewComponent";

class App extends Component {
    render() {
        return (
            <div className="App">
                <HeaderComponent/>
                {/*<PlayerSelectionComponent/>*/}
                {/*<TrackCanvasComponent/>*/}
                {/*<TrackComponent/>*/}
                {/*<LeaderboardAndVideo/>*/}
                {/*<SpeedDataWrapper/>*/}
                {/*<AnomalyWrapper/>*/}
                {/*<VideosWrapper/>*/}
                {/*<LapTimesComponent/>*/}
                <Router>
                    <Route path="/overview" component={OverviewComponent}/>
                </Router>
                <Footer/>
            </div>
        );
    }
}

export default App;
