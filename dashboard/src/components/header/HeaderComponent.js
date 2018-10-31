import React from "react";
import "./HeaderComponent.css";
import RaceInformationService from "../../services/RaceInformationService";
import {Navbar, NavbarGroup, NavbarDivider, Alignment, Tooltip, Position} from "@blueprintjs/core";
import THERMO_IMG from "./img/thermometer.png";
import BARO_IMG from "./img/gauge.png";
import HUMIDITY_IMG from "./img/humidity.png";
import CLOCK_IMG from "./img/clock.png";

/**
 * @author Chathura Widanage
 */
export default class HeaderComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            eventName: "",
            runName: "",
            startDate: ""
        }
    }

    componentDidMount() {
        RaceInformationService.getRaceInformation().then(response => {
            this.setState({
                eventName: response.data['event_name'],
                runName: response.data['run_name'],
                startDate: response.data['start_date'],
            })
        }).catch(err => {
            console.error("Error in fetching race info", err);
        })
    }

    render() {
        return (
            <div>
                <Navbar style={{height: '100px'}}>
                    <NavbarGroup className="header" style={{height: '100px'}}>
                        <div className='title'>
                            <h1 className='event-name title-typeface'>
                                {this.state.eventName}
                            </h1>
                            <h3 className='run-name title-typeface'>
                                {this.state.runName}
                            </h3>
                        </div>
                    </NavbarGroup>
                </Navbar>
                <Navbar>
                    <NavbarGroup className='weather-information' align={Alignment.RIGHT}>
                        <Tooltip content="Ambient Temperature" position={Position.BOTTOM}>
                            <div className="weather-indicator">
                                <img src={THERMO_IMG} alt="thermometer"/>
                                <div className="weather-indicator-value">
                                    77
                                </div>
                            </div>
                        </Tooltip>
                        <NavbarDivider/>
                        <Tooltip content="Barometer" position={Position.BOTTOM}>
                            <div className="weather-indicator">
                                <img src={BARO_IMG} alt="barometer"/>
                                <div className="weather-indicator-value">
                                    2893
                                </div>
                            </div>
                        </Tooltip>
                        <NavbarDivider/>
                        <Tooltip content="Relative Humidity" position={Position.BOTTOM}>
                            <div className="weather-indicator">
                                <img src={HUMIDITY_IMG} alt="humidity"/>
                                <div className="weather-indicator-value">
                                    63%
                                </div>
                            </div>
                        </Tooltip>
                        <NavbarDivider/>
                        <Tooltip content="Time of the Day" position={Position.BOTTOM}>
                            <div className="weather-indicator">
                                <img src={CLOCK_IMG} alt="time"/>
                                <div className="weather-indicator-value">
                                    12:07
                                </div>
                            </div>
                        </Tooltip>
                    </NavbarGroup>
                </Navbar>
            </div>
        );
    }
}