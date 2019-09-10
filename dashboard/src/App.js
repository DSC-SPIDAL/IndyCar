import React, {Component} from 'react';
import HeaderComponent from "./components/header/HeaderComponent";
import "./App.css";
import Footer from "./components/footer/Footer";
import {HashRouter as Router, Route} from "react-router-dom";
import OverviewComponent from "./components/main/OverviewComponent";
import FocusViewComponent from "./components/main/FocusViewComponent";
import StartComponent from "./components/main/StartComponent";

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
                    <Route path="/focus" component={FocusViewComponent}/>
                    <Route exact path="/" component={OverviewComponent}/>
                    <Route path="/start" component={StartComponent}/>
                </Router>
                <Footer/>
            </div>
        );
    }
}

export default App;
