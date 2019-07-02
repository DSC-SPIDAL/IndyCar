import React from "react";
import "./HeaderComponent.css";
import {
    Alignment,
    Button,
    Card,
    Navbar,
    Elevation,
    NavbarDivider,
    NavbarGroup,
    Popover,
    Position,
    Tooltip
} from "@blueprintjs/core";
import THERMO_IMG from "./img/noun-temperature-1492955.svg";
import BARO_IMG from "./img/noun-pressure-1928050.svg";
import HUMIDITY_IMG from "./img/noun-humidity-2500693.svg";
import WEATHER_IMG from "./img/weather-icon.svg";
import {SocketService} from "../../services/SocketService";
import TimeListenerService from "../../services/TimeListenerService";

/**
 * @author Chathura Widanage
 */
export default class HeaderComponent extends React.Component {

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
        this.timeService = TimeListenerService;
        this.timeService.addListener((time) => {
            this.updateTime(time);
        });
    }

    updateTime = (time) => {
        if (Date.now() - this.lastUpdatedTime > 60000) {
            try {
                let t = time.split(".")[0];
                this.setState({
                    timeOfDay: t
                });
                this.lastUpdatedTime = Date.now();
            } catch (e) {
                console.error("Error in updating time");
            }
        }
    };

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
                            <Popover content={
                                <Card interactive={true} elevation={Elevation.TWO}>
                                    <div className="weather-information-rect-content">
                                        <div className="weather-indicator">
                                            <img src={THERMO_IMG} alt="thermometer"/>
                                            <div className="weather-indicator-value">
                                                <p>{this.state.weather.temperature} &deg; F</p>
                                                <p className="weather-indicator-value-hint">Temperature</p>
                                            </div>
                                        </div>
                                        <div className="weather-indicator">
                                            <img src={BARO_IMG} alt="barometer"/>
                                            <div className="weather-indicator-value">
                                                <p>{this.state.weather.pressure}</p>
                                                <p className="weather-indicator-value-hint">Pressure</p>
                                            </div>
                                        </div>
                                        <div className="weather-indicator">
                                            <img src={HUMIDITY_IMG} alt="humidity"/>
                                            <div className="weather-indicator-value">
                                                <p> {this.state.weather.relativeHumidity}%</p>
                                                <p className="weather-indicator-value-hint">Humidity</p>
                                            </div>
                                        </div>
                                    </div>
                                </Card>
                            } position={Position.BOTTOM_RIGHT}>
                                <p style={{letterSpacing: 5}}>
                                    T+ {this.state.timeOfDay}
                                    <img
                                        src={WEATHER_IMG}
                                        height={30}
                                        style={{paddingLeft: 20}}/>
                                </p>
                            </Popover>
                        </div>
                    </NavbarGroup>
                </Navbar>
            </div>
        );
    }
}