import React from "react";
import "./TrackComponent.css";
import SVG from "svg.js";
import roadTexture from "./img/road_two.jpg";
import CBuffer from "CBuffer";

import CarInformationService from "../../services/CarInformationService";
import {SocketService} from "../../services/SocketService";

//GLOBAL CALCULATIONS//

//Actual Dimensions
let trackLongLength = 1006;
let trackShortLength = 201;
let trackWidth = 15.2;
let turnArc = 402;
let turnRadius = turnArc * 4 / (2 * Math.PI);

let totalTrackLength = (trackLongLength * 2) + (trackShortLength * 2) + (turnArc * 4);
console.log("Total track length", totalTrackLength);

//calculating optimum scale for screen size
let scale = Math.min((window.innerWidth) / (trackLongLength + (turnRadius * 2)), (window.innerHeight) / (trackShortLength + turnRadius * 2));
let widthSCale = scale * 5;

//calculate the scale with feedback
scale = Math.min((window.innerWidth - trackWidth * widthSCale * 2) / (trackLongLength + (turnRadius * 2)), (window.innerHeight) / (trackShortLength + turnRadius * 2));
let carScale = 15 * scale;

//scaled dimensions
let scaledRadius = turnRadius * scale;
let scalledTurnArc = 2 * Math.PI * scaledRadius / 4;

let longStraightWay = {
    length: trackLongLength * scale,
    width: trackWidth * widthSCale
};

let shortStraightWay = {
    length: trackShortLength * scale,
    width: trackWidth * widthSCale
};

//------------------------------------------------------//

//adding display padding for clarity
let paddingTop = 0;//100;//(window.innerHeight - shortStraightWay.length - 2 * scaledRadius) / 2;
let paddingLeft = 0;//(window.innerWidth - longStraightWay.length - 2 * scaledRadius) / 2;

//------------------------------//
//          TRACK POINTS        //
//------------------------------//
let xc1 = paddingLeft;
let yc1 = paddingTop;

let x1 = xc1 + scaledRadius;
let y1 = yc1;

let x2 = x1 + longStraightWay.length;
let y2 = y1;

let xc2 = x2 + scaledRadius;
let yc2 = y2;

let x3 = xc2;
let y3 = yc2 + scaledRadius;

let x4 = x3;
let y4 = y3 + shortStraightWay.length;

let xc3 = x4;
let yc3 = y4 + scaledRadius;

let x5 = xc3 - scaledRadius;
let y5 = yc3;

let x6 = x5 - longStraightWay.length;
let y6 = y5;

let xc4 = x6 - scaledRadius;
let yc4 = y6;

let x7 = xc4;
let y7 = yc4 - scaledRadius;

let x8 = x7;
let y8 = y7 - shortStraightWay.length;

//--------------------------------//
//          TRACK DISTANCE        //
//--------------------------------//
let twoToOne = longStraightWay.length;
let oneToEight = twoToOne + scalledTurnArc;
let eightToSeven = oneToEight + shortStraightWay.length;
let sevenToSix = eightToSeven + scalledTurnArc;
let sixToFive = sevenToSix + longStraightWay.length;
let fiveToFour = sixToFive + scalledTurnArc;
let fourToThree = fiveToFour + shortStraightWay.length;
let threeToTwo = fourToThree + scalledTurnArc;

export default class TrackComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.socketService = new SocketService();

        this.cars = {};

        this.pastRecords = {};
    }

    componentDidMount() {
        this.drawTrack(this.trackWrapper);
        this.socketService.subscribe("position", this.positionEventReceiver);
    }

    componentWillUnmount() {
        this.socketService.unsubscribe("position", this.positionEventReceiver);
    }

    positionEventReceiver = (e) => {
        this.positionCars(e);
    };

    positionCars = (carPositions) => {
        let trackOffsets = [-1.5, 1.5, 5];
        Object.keys(carPositions).forEach(key => {
            if (!this.cars[key]) {
                this.cars[key] = this.addCar(`img/cars/car_${('' + key).padStart(2, '0')}.png`, trackOffsets[key % 3], key);
            }

            this.cars[key].push(carPositions[key]);

            // if (this.path) {
            //     let p = this.path.pointAt(carPositions[key].distance * scale);
            //     this.cars[key].carContainer
            //         .animate(carPositions[key].distance.deltaTime)
            //         .center(p.x, p.y)
            //         .after(cb => {
            //             console.log(cb);
            //         });
            //
            //     // this.animateLap(
            //     //     this.cars[key].carContainer,
            //     //     this.cars[key].car,
            //     //     key,
            //     //     0,
            //     //     1000/60,
            //     //     carPositions[key].distance
            //     // )
            // }
        });
    };

    drawTrack = (ref) => {

        this.draw = SVG(ref).size('100%', shortStraightWay.length + (2 * scaledRadius) + (trackWidth * widthSCale * 2));

        /* let outerTrack = draw.rect(longStraightWay.length + (2 * scaledRadius), shortStraightWay.length + (2 * scaledRadius))
             .attr({ fill: 'transparent', stroke: roadTexture, 'stroke-width': 15.2 * widthSCale }).radius(turnRadius * scale);
         outerTrack.center(window.innerWidth / 2, window.innerHeight / 2);*/


// drawing track (counter-clock direction)
        this.path = this.draw.path
        (`
        M${x2} ${y2} 
        L${x1} ${y1} 
        Q ${xc1} ${yc1} ${x8} ${y8}
        L${x7} ${y7}
        Q ${xc4} ${yc4} ${x6} ${y6}
        L${x5} ${y5}
        Q ${xc3} ${yc3} ${x4} ${y4}
        L${x3} ${y3}
        Q ${xc2} ${yc2} ${x2} ${y2}
    `)
            .attr({stroke: roadTexture, fill: 'transparent', 'stroke-width': longStraightWay.width})
            .center(window.innerWidth / 2, (shortStraightWay.length + (2 * scaledRadius) + trackWidth * widthSCale * 2) / 2);

//start pattern
        let pattern = this.draw.pattern(20, 20, function (add) {
            add.rect(20, 20).fill('#fff');
            add.rect(10, 10);
            add.rect(10, 10).move(10, 10)
        });

//start line
        let startLine = this.draw.rect(20 * scale, longStraightWay.width).fill(pattern).move(x2 + 20 * scale, longStraightWay.width / 2/*y1 - (longStraightWay.width / 2) */);


        let trackOffsets = [-1.5, 1.5, 5];
        let index = 0;
        CarInformationService.getCarList().then(response => {
            response.data.forEach(carNumber => {
                console.log(`img/cars/car_${('' + carNumber).padStart(2, '0')}.png`)
                this.addCar(`img/cars/car_${('' + carNumber).padStart(2, '0')}.png`, trackOffsets[index++], carNumber);
                if (index === 3) {
                    index = 0;
                }
            })
        });

        window.addCar = this.addCar;


        //this.addCar(car2, 11000, 1.8, 2);
        //this.addCar(car3, 13000, 3.6, 3);
    };

    animateLap = (carContainer, car, carNumber, lapNumber, animationTime, length, callback) => {
        carContainer.animate(animationTime).during((pos, morph, eased) => {
            let p = this.path.pointAt(eased * length);
            carContainer.center(p.x, p.y);

            //distance travelled
            let distance = length * pos;
            //handling car rotation at arcs and long runways
            let angle;
            if (distance < twoToOne) {
                angle = 180;
            } else if (distance < oneToEight) {
                angle = 180 - (distance - twoToOne) / scalledTurnArc * 90;
            } else if (distance < eightToSeven) {
                angle = 90;
            } else if (distance < sevenToSix) {
                angle = 90 - (distance - eightToSeven) / scalledTurnArc * 90;
            } else if (distance < sixToFive) {
                angle = 0;
            } else if (distance < fiveToFour) {
                angle = (distance - sixToFive) / scalledTurnArc * -90;
            } else if (distance < fourToThree) {
                angle = 270;
            } else if (distance < threeToTwo) {
                angle = 270 + (distance - fourToThree) / scalledTurnArc * -90;
            }
            car.rotate(angle);
            CarInformationService.reportDistance(carNumber, distance + (lapNumber - 1) * length, lapNumber);
        }).after(callback);
    };

    animateCar = (carNumber, carContainer, car, newRecord, cb) => {
        if (!this.pastRecords[carNumber]) {
            this.pastRecords[carNumber] = newRecord;
            cb();
            return;
        }
        let pastRecord = this.pastRecords[carNumber];
        let distanceFromStart = pastRecord.distance * scale;
        let deltaDistance = (newRecord.distance - pastRecord.distance) * scale;
        let deltaTime = newRecord.time - pastRecord.time;
        console.log("Distance", newRecord.distance);
        if (deltaDistance < 0) {
            console.log("DDB", deltaDistance, newRecord.distance, pastRecord.distance, deltaTime);
            deltaDistance = totalTrackLength - pastRecord.distance + newRecord.distance;
            console.log("DD", deltaDistance)
        }

        carContainer
            .animate(deltaTime)
            .during((pos, morph, eased) => {
                let distance = distanceFromStart + (eased * deltaDistance);
                if (distance >= totalTrackLength) {
                    distance = totalTrackLength - distance;
                }
                let p = this.path.pointAt(distance);
                carContainer.center(p.x, p.y);
                let angle;
                if (distance < twoToOne) {
                    angle = 180;
                } else if (distance < oneToEight) {
                    angle = 180 - (distance - twoToOne) / scalledTurnArc * 90;
                } else if (distance < eightToSeven) {
                    angle = 90;
                } else if (distance < sevenToSix) {
                    angle = 90 - (distance - eightToSeven) / scalledTurnArc * 90;
                } else if (distance < sixToFive) {
                    angle = 0;
                } else if (distance < fiveToFour) {
                    angle = (distance - sixToFive) / scalledTurnArc * -90;
                } else if (distance < fourToThree) {
                    angle = 270;
                } else if (distance < threeToTwo) {
                    angle = 270 + (distance - fourToThree) / scalledTurnArc * -90;
                }
                car.rotate(angle);
            }).after(() => {
            this.pastRecords[carNumber] = newRecord;
            cb();
        });

    };

    addCar = (image, trackOffset = 1.8, carNumber) => {
        let length = this.path.length();
        let carContainer = this.draw.group();
        let car = carContainer.image(image).size(4.8 * carScale, 1.8 * carScale);
        let boundingBoxMax = Math.sqrt(Math.pow(4.8 * carScale, 2) * 2);
        carContainer.rect(boundingBoxMax, boundingBoxMax).fill('transparent');
        car.move(boundingBoxMax / 2 - 4.8 * carScale / 2, boundingBoxMax / 2 - trackOffset * carScale / 2);

        let initPoint = this.path.pointAt(0);
        carContainer.center(initPoint.x, initPoint.y);

        //var car = carContainer.rect(4.8 * carScale, 1.8 * carScale).stroke(1).fill('transparent').attr({ 'stroke-width': 1 })
        // this.animateLap(carContainer, car, animationTime, length, () => {
        //     console.log(carNumber, "finished one lap");
        // });

        CarInformationService.getCarLapTimes(carNumber).then(response => {
            let lapTimes = response.data;
            let startIndex = 0;

            let animationCallback = () => {
                if (startIndex < lapTimes.length) {
                    this.animateLap(carContainer, car, carNumber, lapTimes[startIndex].lap_num, lapTimes[startIndex].lap_time * 500, length, animationCallback);
                    startIndex++;
                }
            };
            animationCallback();
        });

        let frameBuffer = new CBuffer(24 * 5);//buffer 5 seconds, assuming 24 fps

        let firstTime = true;

        let animationCallback = (data = frameBuffer.shift()) => {
            if (data) {
                this.animateCar(carNumber, carContainer, car, data, animationCallback);
            } else {
                //could be because it has run out of buffer
                firstTime = true;
            }
        };

        //wait till overflow to start
        frameBuffer.overflow = (data) => {
            if (firstTime) {
                firstTime = false;
                animationCallback(data);
            }
        };


        return frameBuffer;
    };

    render() {
        return (
            <div ref={(ref) => {
                this.trackWrapper = ref;
            }} className="ic-track-wrapper">
            </div>
        )
    }
}