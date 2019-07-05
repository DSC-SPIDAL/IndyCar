import React from "react";
import {Line} from "react-chartjs-2";
import {Button, Card, Colors} from "@blueprintjs/core";
import CarInformationService, {CAR_LAP_LISTENER, CAR_RANK_LISTENER} from "../../services/CarInformationService";
import "./LapTimesComponent.css";
import {ButtonGroup} from "@blueprintjs/core/lib/cjs";

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

const DISPLAY_OPTIONS = {
    ONLY_SELECTED: 0,
    NEIGHBOURS: 1,
    ALL: 2
};

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
            maxLap: 0,
            displayOption: DISPLAY_OPTIONS.NEIGHBOURS,
            rankMap: {
                carToRank: {},
                rankToCar: {}
            }
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

    onRankMapReceived = (rankMap) => {
        this.setState({
            rankMap: rankMap
        });
    };

    componentDidMount() {
        let lapTimes = CarInformationService.getLapTimes();
        Object.values(lapTimes).forEach(records => {
            records.forEach(this.onLapRecordReceived);
        });

        CarInformationService.addEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
        CarInformationService.addEventListener(CAR_RANK_LISTENER, this.onRankMapReceived);
    }

    componentWillUnmount() {
        CarInformationService.removeEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
        CarInformationService.removeEventListener(CAR_RANK_LISTENER, this.onRankMapReceived);
    }

    changeDisplayOption = (displayOption) => {
        this.setState({
            displayOption
        });
    };

    render() {

        let neighbours = {
            [this.props.selectedCarNumber]: true
        };

        if (this.state.displayOption === DISPLAY_OPTIONS.NEIGHBOURS) {
            //find and add neighbours
            let subjectRank = this.state.rankMap.carToRank[this.props.selectedCarNumber];

            if (!isNaN(subjectRank)) {
                let lowerRank = Math.max(1, subjectRank - 2);

                let higherRank = Math.min(33, subjectRank + 2);

                if (higherRank - lowerRank < 4) {
                    let diff = 4 - (higherRank - lowerRank);
                    if (lowerRank === 1) { //capped in lower rank
                        higherRank += diff;
                    } else {
                        lowerRank -= diff;
                    }
                }

                for (let i = lowerRank; i <= higherRank; i++) {
                    neighbours[parseInt(this.state.rankMap.rankToCar[i])] = true;
                }
            }
        }

        //update the chart
        let dataSet = [];
        Object.keys(this.state.carData).filter(carNumber => {
            return this.state.displayOption === DISPLAY_OPTIONS.ALL || neighbours[carNumber];
        }).forEach(carNumber => {
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
                    <div className="ic-lap-time-header">
                        <div className="ic-section-title">
                            Lap Time (s)
                        </div>
                        <div className="ic-lap-time-header-controls">
                            <ButtonGroup>
                                <Button onClick={(e => {
                                    this.changeDisplayOption(DISPLAY_OPTIONS.ONLY_SELECTED)
                                })} active={this.state.displayOption === DISPLAY_OPTIONS.ONLY_SELECTED}>
                                    Only {this.props.selectedCarNumber}
                                </Button>
                                <Button onClick={(e => {
                                    this.changeDisplayOption(DISPLAY_OPTIONS.NEIGHBOURS)
                                })} active={this.state.displayOption === DISPLAY_OPTIONS.NEIGHBOURS}>
                                    Neighbours
                                </Button>
                                <Button onClick={(e => {
                                    this.changeDisplayOption(DISPLAY_OPTIONS.ALL)
                                })} active={this.state.displayOption === DISPLAY_OPTIONS.ALL}>
                                    All
                                </Button>
                            </ButtonGroup>
                        </div>
                    </div>
                    <Line data={{
                        labels: labels,
                        datasets: dataSet,
                    }}
                          height={window.innerWidth > 800 ? 300 : 200}
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