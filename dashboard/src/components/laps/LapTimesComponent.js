import React from "react";
import {Line} from "react-chartjs-2";
import {Card, Colors} from "@blueprintjs/core";
import CarInformationService, {CAR_LAP_LISTENER} from "../../services/CarInformationService";
import "./LapTimesComponent.css";

const goodColors = ["#ef5350", "#EC407A", "#AB47BC",
    "#42A5F5", "#26A69A", "#66BB6A",
    "#FFA726", "#558B2F"];

function getRandomColor() {
    if (goodColors.length > 0) {
        return goodColors.pop();
    }
    let colors = Object.values(Colors);
    return Object.values(Colors)[Math.floor(Math.random() * colors.length)];
}

let colorCache = {};

/**
 * to prevent color change on next poll
 * @param car
 */
function getCarColor(car) {
    if (!colorCache[car]) {
        colorCache[car] = getRandomColor();
    }
    return colorCache[car];
}


export default class LapTimesComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            labels: [],
            datasets: [],
            carsList: [],
            carData: {},
            maxLap: 0
        };

        this.setStateTimeout = -1;
    }

    onLapRecordReceived = (record) => {
        if (record.carNumber !== "") {
            clearTimeout(this.setStateTimeout);
            let carData = this.state.carData;
            if (!carData[record.carNumber]) {
                carData[record.carNumber] = [];
            }
            let maxLap = this.state.maxLap;
            carData[record.carNumber][record.completedLaps] = record.time;
            if (record.completedLaps > maxLap) {
                maxLap = record.completedLaps;
            }
            this.setStateTimeout = setTimeout(() => {
                this.setState({carData, maxLap});
            }, 1000);
        }
    };

    onCarInformationChanged = () => {
        let carListObj = CarInformationService.getCarList();
        if (carListObj) {
            let carsList = Object.keys(carListObj).map(carNumber => {
                return carListObj[carNumber];
            });
            this.setState({carsList});
        }
    };

    componentDidMount() {
        let lapTimes = CarInformationService.getLapTimes();
        Object.values(lapTimes).forEach(records => {
            records.forEach(this.onLapRecordReceived);
        });

        CarInformationService.addEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
    }

    componentWillUnmount() {
        CarInformationService.removeEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
    }

    fetchDataForCar = (carNumber) => {
        CarInformationService.getCarLapTimes(carNumber).then(response => {
            this.setState({
                carData: {
                    ...this.state.carData,
                    [carNumber]: response.data
                }
            })
        });
    };

    onCarDriverSwitchChange = (carNumber, checked) => {
        if (!checked) {
            let carData = this.state.carData;
            delete carData[carNumber];
            this.setState({
                carData
            })
        } else {
            this.fetchDataForCar(carNumber);
        }
    };

    render() {
        // let carsListSwitch = this.state.carsList.map(car => {
        //     return <Switch label={car.driverName} key={car.carNumber}
        //                    onChange={(event) => {
        //                        this.onCarDriverSwitchChange(car.carNumber, event.target.checked)
        //                    }}
        //                    checked={this.state.carData[car.carNumber] !== undefined}/>
        // });


        //update the chart
        let dataSet = [];
        Object.keys(this.state.carData).forEach(carNumber => {
            let lapTimes = this.state.carData[carNumber];
            let color = getCarColor(carNumber);
            dataSet.push({
                label: `[${carNumber}] ${CarInformationService.getCarInformation(carNumber).driverName}`,
                data: lapTimes,
                fill: false,
                borderColor: color,
                backgroundColor: color,
                borderWidth: 0.5,
                datasetKeyProvider: () => {
                    return carNumber;
                },
                datalabels: {
                    display: false
                }
            });
        });
        //x axis
        let labels = [];
        for (let i = 1; i <= this.state.maxLap; i++) {
            labels.push(i);
        }

        return (
            <div className="ic-section ic-lap-time-wrapper">
                <Card>
                    <h5 className="ic-section-title">Lap Times</h5>
                    {/*<Popover content={<Menu>*/}
                    {/*{carsListSwitch}*/}
                    {/*</Menu>} position={Position.RIGHT_TOP}>*/}
                    {/*<Button icon="share" text="Select Drivers" className="ic-lap-times-driver-selection"/>*/}
                    {/*</Popover>*/}
                    <Line data={{
                        labels: labels,
                        datasets: dataSet,
                    }}
                          height={window.innerWidth > 800 ? 400 : 200}
                          options={{
                              responsive: true,
                              maintainAspectRatio: false,
                              legend: {
                                  display: window.innerWidth > 800,
                                  position: 'bottom',
                                  labels: {
                                      fontColor: 'black'
                                  }
                              },
                              scales: {
                                  yAxes: [{
                                      scaleLabel: {
                                          display: true,
                                          labelString: 'Lap time (s)',
                                          fontColor: 'white'
                                      },
                                      gridLines: {
                                          color: "#90A4AE"
                                      },
                                      ticks: {
                                          fontColor: "black",
                                      },
                                  }],
                                  xAxes: [{
                                      scaleLabel: {
                                          display: true,
                                          labelString: 'Lap number',
                                          fontColor: 'white'
                                      },
                                      gridLines: {
                                          color: "#90A4AE"
                                      },
                                      ticks: {
                                          fontColor: "black",
                                      }
                                  }],
                              }
                          }}/>
                </Card>
            </div>
        );
    }
}