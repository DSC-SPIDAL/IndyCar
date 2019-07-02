import React from "react";
import {Card} from "@blueprintjs/core";
import "./SpeedDataComponent.css";
import CarInformationService, {CAR_LAP_LISTENER} from "../../services/CarInformationService";
import PropTypes from 'prop-types';
import "./LeaderboardItem.css";

/**
 * @author Chathura Widanage
 */
export default class LeaderboardItem extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            x: [],
            carInfo: {},
            lapRecords: {},
            driverImage: `url(img/drivers/no.jpg)`
        };

        // for (let i = 0; i < 18; i++) {
        //     this.state.x.push(Math.floor(Math.random() * 250))
        // }
    }

    updateCarInformation = (props = this.props) => {
        //get car information
        let carInfo = CarInformationService.getCarInformation(props.carNumber);

        let rawImgUrl = `img/drivers/${carInfo.carNumber}.jpg`;
        let imageUrl = `url(img/drivers/no.jpg)`;

        this.imageExists(rawImgUrl, (exists) => {
            if (exists) {
                imageUrl = `url(img/drivers/${carInfo.carNumber}.jpg)`;
            }
            this.setState({
                carInfo,
                driverImage: imageUrl
            });
        });
    };

    onLapRecordReceived = (lapRecord) => {
        if (lapRecord.carNumber === this.props.carNumber) {
            let lapRecords = this.state.lapRecords;
            if (!lapRecords[lapRecord.completedLaps]) {
                lapRecords[lapRecord.completedLaps] = lapRecord.time;
                this.setState({
                    lapRecords
                });
            }
        }
    };

    componentDidMount() {
        this.updateCarInformation();


        let lapTimes = CarInformationService.getLapTimes();
        Object.values(lapTimes).forEach(records => {
            records.forEach(this.onLapRecordReceived);
        });


        CarInformationService.addEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
    }

    componentWillUnmount() {
        CarInformationService.removeEventListener(CAR_LAP_LISTENER, this.onLapRecordReceived);
    }


    componentWillReceiveProps(nextProps) {
        if (nextProps.carNumber !== this.props.carNumber) {
            this.updateCarInformation(nextProps);
        }
    }

    imageExists = (image_url, cb) => {
        let xhr = new XMLHttpRequest();

        xhr.open('HEAD', image_url, true);
        xhr.onload = function (e) {
            cb(xhr.status !== 404)
        };
        xhr.onerror = function (e) {
            cb(false)
        };

        xhr.send();
    };

    render() {

        let lapRecs = Object.keys(this.state.lapRecords).map(key => {
            return {lap: key, time: this.state.lapRecords[key]}
        });

        let sortedLapNumbers = [];
        let sortedLaps = lapRecs.sort((a, b) => {
            let lapA = parseInt(a.lap, 10);
            let lapB = parseInt(b.lap, 10);
            if (lapA < lapB) {
                return -1;
            } else if (lapA > lapB) {
                return 1;
            }
            return 0;
        }).map(lapR => {
            sortedLapNumbers.push(lapR.lap);
            return lapR.time;
        });

        let lastLapTime = sortedLaps.length > 0 ? sortedLaps[sortedLaps.length - 1] : "N/A";

        return (
            <div className="leader-board-item">
                <div className="leader-board-item-rank">
                    #{this.props.rank}
                </div>
                <div className="leader-board-item-name">
                    {this.state.carInfo.driverName}
                    <span className="leader-board-item-engine">{this.state.carInfo.engine}</span>
                </div>
                <div className="leader-board-last-lap">
                    {lastLapTime}
                </div>
            </div>
        );
    }
}

LeaderboardItem.propTypes = {
    carNumber: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};