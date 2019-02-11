import React from "react";
import {Card} from "@blueprintjs/core";
import "./SpeedDataComponent.css";
import {Icon} from "@blueprintjs/core/lib/esm/components/icon/icon";
import {Line} from "react-chartjs-2";
import CarInformationService from "../../services/CarInformationService";
import PropTypes from 'prop-types';

/**
 * @author Chathura Widanage
 */
export default class SpeedDataComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            x: [],
            carInfo: {}
        };

        // for (let i = 0; i < 18; i++) {
        //     this.state.x.push(Math.floor(Math.random() * 250))
        // }
    }

    updateCarInformation = (props = this.props) => {
        //get car information
        let carInfo = CarInformationService.getCarInformation(props.carNumber);
        this.setState({
            carInfo
        })
    };

    updateSectionTiming(props = this.props) {
        // CarInformationService.getSectionTiming(props.carNumber, props.carData.currentLap).then(response => {
        //     let times = response.data.map(sectionTime => 100 / parseFloat(sectionTime.last_section_time.split(":")[2]));
        //     this.setState({
        //         x: times
        //     })
        // });
    }

    componentDidMount() {
        // setInterval(() => {
        //     let x = [];
        //     for (let i = 0; i < 18; i++) {
        //         x.push(Math.floor(Math.random() * 250))
        //     }
        //     this.setState({x});
        // }, 5000);

        this.updateCarInformation();
        this.updateSectionTiming();
    }


    componentWillReceiveProps(nextProps) {
        if (nextProps.carNumber !== this.props.carNumber) {
            this.updateCarInformation(nextProps);
            this.updateSectionTiming(nextProps);
        } else if (nextProps.carData.currentLap !== this.props.carData.currentLap) {
            this.updateSectionTiming(nextProps);
        }
    }

    render() {
        return (
            <Card className="speed-data-component">
                <div className="speed-data-rank-wrapper">
                    <div className="speed-data-info-wrapper">
                        <div className="speed-data-car-info">
                            <div className="speed-data-car-info-middle">
                                <div className="speed-data-car-info-number">
                                    {this.state.carInfo.carNumber}
                                </div>
                                <div className="speed-data-car-info-engine">
                                    {this.state.carInfo.engine}
                                </div>
                            </div>
                        </div>
                        <div className="speed-data-driver-info">
                            <div className="speed-data-driver-info-bio">
                                <div className='speed-data-driver-name'>{this.state.carInfo.driverName}</div>
                                <div className='speed-data-driver-hometown'>
                                    <Icon icon="map-marker"/>&nbsp;{this.state.carInfo.hometown}
                                </div>
                                <div className='speed-data-driver-team'>
                                    <Icon icon="people"/>&nbsp;{this.state.carInfo.team}
                                </div>
                            </div>
                            <div className="speed-data-driver-info-other">
                                <div className="speed-data-driver-info-other-col">
                                    <div>
                                        <Icon icon="id-number"/>
                                    </div>
                                    <div
                                        className="speed-data-driver-info-other-licence">{this.state.carInfo.license}</div>
                                </div>
                                <div className="speed-data-driver-info-other-col">
                                    <div>
                                        <Icon icon="drive-time"/>
                                    </div>
                                    <div
                                        className="speed-data-driver-info-other-competitor-id">{this.state.carInfo.uid}</div>
                                </div>
                                <div className="speed-data-driver-info-other-col">
                                    <div>
                                        <Icon icon="badge"/>
                                    </div>
                                    <div className="speed-data-driver-info-other-rank">#{this.props.rank}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="speed-data-lap-sections">
                        <Line data={{
                            labels: this.state.x,
                            datasets: [
                                {
                                    borderColor: "#90A4AE",
                                    backgroundColor: "#263238",
                                    borderWidth: 0.5,
                                    data: this.state.x
                                }
                            ]
                        }}
                              options={{
                                  responsive: true,
                                  maintainAspectRatio: false,
                                  legend: {
                                      display: false
                                  },
                                  elements: {
                                      line: {
                                          tension: 0, // disables bezier curves
                                      }
                                  },
                                  animation: {
                                      duration: 0, // general animation time
                                  },
                                  hover: {
                                      animationDuration: 0, // duration of animations when hovering an item
                                  },
                                  responsiveAnimationDuration: 0, // animation duration after a resize
                                  scales: {
                                      yAxes: [{
                                          display: false,
                                          ticks: {
                                              display: false,
                                              beginAtZero: true
                                          },
                                          gridLines: {
                                              display: false,
                                          },
                                          scaleLabel: {
                                              display: false
                                          }
                                      }],
                                      xAxes: [{
                                          display: false,
                                          gridLines: {
                                              display: false,
                                          },
                                      }]
                                  },
                              }}/>
                        {/*<div className="speed-data-lap-sections-title">*/}
                        {/*Last Lap Section Speeds*/}
                        {/*</div>*/}
                        {/*<div className="speed-data-lap-sections-info">*/}
                        {/*{this.state.x.map((i, index) => {*/}
                        {/*return (<div className="speed-data-lap-section speed-data-lap-section-1">*/}
                        {/*<div className="speed-data-lap-section-label-wrapper">*/}
                        {/*<span className="speed-data-lap-section-label">{index + 1}</span>*/}
                        {/*</div>*/}
                        {/*<div className="speed-data-lap-section-time">*/}
                        {/*{Math.floor(Math.random() * 500)}*/}
                        {/*</div>*/}
                        {/*</div>)*/}
                        {/*})}*/}

                        {/*</div>*/}
                    </div>
                </div>
            </Card>
        );
    }
}

SpeedDataComponent.propTypes = {
    carNumber: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};