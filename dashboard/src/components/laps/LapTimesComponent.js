import React from "react";
import {Line} from "react-chartjs-2";
import {Menu, MenuItem, Card, Button, Switch, Popover, Position, Colors} from "@blueprintjs/core";
import CarInformationService from "../../services/CarInformationService";
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
            carData: {}
        };
    }

    componentDidMount() {
        let preSelectedCards = [];
        CarInformationService.getCarList().then(response => {
            return Promise.all(response.data.map((carNumber, index) => {
                if (index < 5) {
                    preSelectedCards.push(carNumber);
                }
                return CarInformationService.getCarInformation(carNumber);
            })).then(infoResponses => infoResponses.map(infoResponse => infoResponse.data));
        }).then(carsList => {
            this.setState({
                carsList
            }, () => {
                preSelectedCards.forEach(this.fetchDataForCar);
            })
        });

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
        let carInfoMap = {};
        let carsListSwitch = this.state.carsList.map(car => {
            carInfoMap[car.entry_info_data.car_num] = car;
            return <Switch label={car.entry_info_data.driver_name} key={car.entry_info_data.car_num}
                           onChange={(event) => {
                               this.onCarDriverSwitchChange(car.entry_info_data.car_num, event.target.checked)
                           }}
                           checked={this.state.carData[car.entry_info_data.car_num] !== undefined}/>
        });

        console.log(Object.values(this.state.carData));

        //update the chart
        let lapMax = -1;
        let dataSet = [];
        Object.keys(this.state.carData).forEach(carNumber => {
            let lapTimes = this.state.carData[carNumber];
            let dataArray = lapTimes.map(time => {
                if (lapMax < time['lap_num']) {
                    lapMax = time['lap_num'];
                }
                return time['lap_time'];
            });
            dataSet.push({
                label: carInfoMap[carNumber].entry_info_data.driver_name,
                data: dataArray,
                fill: false,
                borderColor: getCarColor(carNumber),
                backgroundColor: getCarColor(carNumber),
                borderWidth: 0.5,
            })
        });
        //x axis
        let labels = [];
        for (let i = 1; i <= lapMax; i++) {
            labels.push(i);
        }

        return (
            <div className="ic-section ic-lap-time-wrapper">
                <Card>
                    <h5>Lap Times</h5>
                    <Popover content={<Menu>
                        {carsListSwitch}
                    </Menu>} position={Position.RIGHT_TOP}>
                        <Button icon="share" text="Select Drivers" className="ic-lap-times-driver-selection"/>
                    </Popover>
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