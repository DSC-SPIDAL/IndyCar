import React from "react";
import "./HeaderComponent.css";
import {Alignment, ButtonGroup, Navbar, NavbarGroup} from "@blueprintjs/core";
import THERMO_IMG from "./img/temp.svg";
import BARO_IMG from "./img/baro.svg";
import HUMIDITY_IMG from "./img/humidity.svg";
import WEATHER_IMG from "./img/weather-icon.svg";
import {SocketService} from "../../services/SocketService";
import {HashRouter as Router, Link} from "react-router-dom";
import {connect} from "react-redux";

/**
 * @author Chathura Widanage
 */
class HeaderComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            eventName: "Indianapolis 500",
            runName: "05-27-2018",
            startDate: "2018-05-27",
            timeOfDay: "",
            weather: {
                pressure: "...",
                relativeHumidity: "...",
                temperature: "...",
                timeOfDay: "..."
            }
        };
        this.socketService = new SocketService();
        this.lastUpdatedTime = -1;
    }

    onWeatherRecord = (data) => {
        // console.log("Weather data", data);
        // let hours = Math.floor(data.timeOfDay / (1000 * 60 * 60)) + "";
        // let mins = Math.floor((data.timeOfDay % (1000 * 60 * 60)) / (1000 * 60)) + "";
        // if (mins.length === 1) {
        //     mins = "0" + mins;
        // }
        this.setState({
            weather: {
                pressure: data.pressure,
                relativeHumidity: data.relativeHumidity,
                temperature: data.temperature,
                //timeOfDay: hours + ":" + mins
            }
        })
    };

    componentDidMount() {
        // RaceInformationService.getRaceInformation().then(response => {
        //     this.setState({
        //         eventName: response.data['event_name'],
        //         runName: response.data['run_name'],
        //         startDate: response.data['start_date'],
        //     })
        // }).catch(err => {
        //     console.error("Error in fetching race info", err);
        // });

        this.socketService.subscribe("weather", this.onWeatherRecord);
    }

    componentWillUnmount() {
        this.socketService.unsubscribe("weather", this.onWeatherRecord);
    }

    render() {
        return (
            <div>
                <Navbar style={{height: '100px', paddingRight: 0}}>
                    <NavbarGroup className="header" style={{height: '100px'}}>
                        <div className='title'>
                            <h1 className='event-name title-typeface'>
                                {this.state.eventName}
                            </h1>
                            <h3 className='run-name title-typeface'>
                                {/*{this.state.runName}*/}
                            </h3>
                        </div>
                    </NavbarGroup>
                    <NavbarGroup align={Alignment.RIGHT} style={{height: '100px', position: 'relative'}}>
                        <div className="weather-information-rect-small weather-information-rect-small-1">
                        </div>
                        <div className="weather-information-rect-small weather-information-rect-small-2">
                        </div>
                        <div className="weather-information-rect">
                            <div className="weather-information-time">
                                <p style={{letterSpacing: 5}}>
                                    T+ <span>{this.props.time}</span>
                                </p>
                            </div>

                        </div>
                    </NavbarGroup>
                </Navbar>
                <Navbar style={{height: '100px', backgroundColor: "#181818"}}>
                    <NavbarGroup className="header" style={{height: '100px'}}>
                        <div className="weather-information-indicators">
                            <div className="weather-indicator-title">
                                <p>Track Conditions</p>
                            </div>
                            <div className="weather-indicator">
                                <img src={THERMO_IMG} alt="thermometer"/>
                                <div>
                                    <p className="weather-indicator-value">{this.state.weather.temperature} &deg; F</p>
                                    <p className="weather-indicator-name">Temperature</p>
                                </div>
                            </div>
                            <div className="weather-indicator">
                                <img src={BARO_IMG} alt="barometer"/>
                                <div>
                                    <p className="weather-indicator-value">{this.state.weather.pressure}</p>
                                    <p className="weather-indicator-name">Surface</p>
                                </div>
                            </div>
                        </div>
                    </NavbarGroup>
                    <NavbarGroup align={Alignment.RIGHT} style={{height: '100px'}}>
                        <div className="weather-information-indicators">
                            <div className="weather-indicator-title">
                                <p>Ambient Conditions</p>
                            </div>
                            <div className="weather-indicator">
                                <img src={THERMO_IMG} alt="thermometer"/>
                                <div>
                                    <p className="weather-indicator-value">{this.state.weather.temperature} &deg; F</p>
                                    <p className="weather-indicator-name">Temperature</p>
                                </div>
                            </div>
                            <div className="weather-indicator">
                                <img src={BARO_IMG} alt="barometer"/>
                                <div>
                                    <p className="weather-indicator-value">{this.state.weather.pressure}</p>
                                    <p className="weather-indicator-name">Pressure</p>
                                </div>
                            </div>
                            <div className="weather-indicator">
                                <img src={HUMIDITY_IMG} alt="humidity"/>
                                <div>
                                    <p className="weather-indicator-value"> {this.state.weather.relativeHumidity}%</p>
                                    <p className="weather-indicator-name">Humidity</p>
                                </div>
                            </div>
                        </div>
                    </NavbarGroup>
                </Navbar>
                <div className="sub-menu">
                    <ButtonGroup minimal={true} className="sub-menu-buttons">
                        <Router>
                            {/*<Link to="/start" className="pt-button">Starting Grid</Link>*/}
                            <Link to="/" className="pt-button">Overview</Link>
                            <Link to="/focus" className="pt-button">Focus View</Link>
                        </Router>
                    </ButtonGroup>
                </div>
            </div>
        );
    }
}

export default connect(state => {
    return {
        time: state.RaceInfo.time
    }
})(HeaderComponent)