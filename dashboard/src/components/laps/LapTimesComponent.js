import React from "react";
import {Line} from "react-chartjs-2";
import {Card, Colors} from "@blueprintjs/core";
import CarInformationService, {CAR_LAP_LISTENER} from "../../services/CarInformationService";
import "./LapTimesComponent.css";

function getRandomColor() {
    // let letters = '0123456789ABCDEF';
    //     // let color = '#';
    //     // for (let i = 0; i < 6; i++) {
    //     //     color += letters[Math.floor(Math.random() * 16)];
    //     // }
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
            dataSet.push({
                label: `[${carNumber}] ${CarInformationService.getCarInformation(carNumber).driverName}`,
                data: lapTimes,
                fill: false,
                borderColor: getCarColor(carNumber),
                backgroundColor: getCarColor(carNumber),
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
                    <h5>Lap Times</h5>
                    {/*<Popover content={<Menu>*/}
                    {/*{carsListSwitch}*/}
                    {/*</Menu>} position={Position.RIGHT_TOP}>*/}
                    {/*<Button icon="share" text="Select Drivers" className="ic-lap-times-driver-selection"/>*/}
                    {/*</Popover>*/}
                    <Line data={{
                        labels: labels,
                        datasets: dataSet,
                    }}
                          height={400}
                          options={{
                              responsive: true,
                              maintainAspectRatio: false,
                              legend: {
                                  display: true,
                                  position: 'bottom',
                                  labels: {
                                      fontColor: 'white'
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
                                          fontColor: "white",
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
                                          fontColor: "white",
                                      },
                                  }],
                              }
                          }}/>
                </Card>
            </div>
        );
    }
}