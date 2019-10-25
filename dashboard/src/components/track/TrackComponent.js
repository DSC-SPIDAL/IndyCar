import React from "react";
import "./TrackComponent.css";
import SVG from "svg.js";
import CBuffer from "CBuffer";
import {SocketService} from "../../services/SocketService";
import LOADER from "./img/buffering.gif";


//GLOBAL CALCULATIONS//

//Actual Dimensions
let trackLongLength = 1006;
let trackShortLength = 201;
let trackWidth = 15.2;
let turnArc = 402;
let turnRadius = turnArc * 4 / (2 * Math.PI);

let widthOffset = 0;

let widgetWidth = window.innerWidth - 360;

console.log(widgetWidth);

if (widgetWidth > 1000) {
    widthOffset = 200;
}

//calculating optimum scale for screen size
let scale = Math.min((widgetWidth - widthOffset) / (trackLongLength + (turnRadius * 2)), (window.innerHeight) / (trackShortLength + turnRadius * 2));
let widthSCale = scale * 5;

//calculate the scale with feedback
scale = Math.min((widgetWidth - widthOffset - trackWidth * widthSCale * 2) / (trackLongLength + (turnRadius * 2)), (window.innerHeight) / (trackShortLength + turnRadius * 2));
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
let paddingLeft = 0;//(widgetWidth - longStraightWay.length - 2 * scaledRadius) / 2;

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
        this.state = {
            buffering: true,
            intermediateBuffering: false
        };
        this.socketService = new SocketService();
        this.reset();
    }

    reset = () => {
        if (this.cars) {
            Object.values(this.cars).forEach(carObj => {
                if (carObj.anime) {
                    carObj.anime.stop();
                }
            });
        }

        this.cars = {};

        this.pastRecords = {};

        this.bufferSize = 60;

        this.timeReducers = {};

        this.diffs = {};

        this.bufferingCars = 0;
    };

    onAnomalyClass = (anoClass) => {
        if (this.cars[anoClass.carNumber]) {
            if (anoClass.anomalyClass === 1) {
                this.warningContainer(anoClass.carNumber);
            } else {
                this.criticalContainer(anoClass.carNumber);
            }
        }
    };

    componentDidMount() {
        console.log("Track component mounting...");
        this.drawTrack(this.trackWrapper);
        this.socketService.subscribe("position", this.positionEventReceiver);
        this.socketService.subscribe("anomaly_class", this.onAnomalyClass);
    }

    componentWillUnmount() {
        console.log("Track Component Un-mounting...");
        this.socketService.unsubscribe("position", this.positionEventReceiver);
        this.socketService.unsubscribe("anomaly_class", this.onAnomalyClass);
        Object.values(this.cars).forEach(frameBuffer => {
            frameBuffer.carContainer.stop(false, true);
        });
        this.draw.clear();
        this.reset();
    }

    positionEventReceiver = (e) => {
        if (window.debug) {
            console.log("Event recv", e);
        }
        e.forEach(event => {
            this.positionCars(event);
        })
        //this.positionCars(e);
    };

    positionCars = (carPosition) => {
        let key = carPosition.carNumber;
        if (!this.cars[key]) {
            let trackOffsets = [-1.5, 1.5, 5];
            let carImages = ["R", "G", "B", "Y"];
            this.cars[key] = this.addCar(`img/cars/car_${('' + key).padStart(2, '0')}.png`, trackOffsets[key % 3], key);
            //this.cars[key] = this.addCar(`img/cars_new/Car${carImages[key % carImages.length]}.png`, trackOffsets[key % 3], key);
        }

        this.cars[key].push(carPosition);
        this.cars[key].eventAdded(carPosition);
    };

    drawTrack = (ref) => {
        this.draw = SVG(ref).size('100%', shortStraightWay.length + (2 * scaledRadius) + (trackWidth * widthSCale * 2));

        /* let outerTrack = draw.rect(longStraightWay.length + (2 * scaledRadius), shortStraightWay.length + (2 * scaledRadius))
             .attr({ fill: 'transparent', stroke: roadTexture, 'stroke-width': 15.2 * widthSCale }).radius(turnRadius * scale);
         outerTrack.center(widgetWidth / 2, window.innerHeight / 2);*/


// drawing track (counter-clock direction)
        this.path = this.draw.path(`M${x2} ${y2} 
        L${x1} ${y1} 
        Q ${xc1} ${yc1} ${x8} ${y8}
        L${x7} ${y7}
        Q ${xc4} ${yc4} ${x6} ${y6}
        L${x5} ${y5}
        Q ${xc3} ${yc3} ${x4} ${y4}
        L${x3} ${y3}
        Q ${xc2} ${yc2} ${x2} ${y2}`)
            .attr({stroke: "#3E3E3E", fill: 'transparent', 'stroke-width': longStraightWay.width})
            .center(widgetWidth / 2, (shortStraightWay.length + (2 * scaledRadius) + trackWidth * widthSCale * 2) / 2);

//start pattern
        let pattern = this.draw.pattern(20, 20, function (add) {
            add.rect(20, 20).fill('#fff');
            add.rect(10, 10);
            add.rect(10, 10).move(10, 10)
        });

//start line
        this.draw.rect(20 * scale, longStraightWay.width).fill(pattern).move(this.path.pointAt(0).x - 20 * scale/*x2 + 20 * scale*/, longStraightWay.width / 2/*y1 - (longStraightWay.width / 2) */);

        window.path = this.path;
    };

    positionCar = (carContainer, car, distance) => {
        if (distance >= (this.path.length())) {
            distance = distance - (this.path.length());
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
    };

    animateCar = (carNumber, carContainer, carNumberGroup, numberOffset, car, newRecord, cb, trckoff) => {
        //skip the first record
        if (!this.pastRecords[carNumber]) {
            this.pastRecords[carNumber] = newRecord;
            cb();
            return;
        }

        let pastRecord = this.pastRecords[carNumber];

        //normalize distances
        let pastDistance = pastRecord.distance;
        let newDistance = newRecord.distance;

        let distanceFromStart = pastDistance * scale;
        let deltaDistance = (newDistance - pastDistance) * scale;
        // let deltaTime = Math.max(1, newRecord.time - pastRecord.time - diffOfActualTime);
        let diff = (this.diffs[carNumber] - Date.now() + pastRecord.time);

        let rawDeltaTime = newRecord.time - pastRecord.time;//diff;
        let deltaTime = rawDeltaTime;

        let speedAdjusted = false;
        if (this.cars[carNumber].length > 5) { //slow down only if we have enough buffer
            deltaTime = Math.max(1, rawDeltaTime * this.timeReducers[carNumber]);
            speedAdjusted = true;
        }

        if (deltaDistance < 0) {
            //sometimes recorded distance is larger than totalTrack length
            let initDistance = deltaDistance;
            deltaDistance = ((this.path.length() / scale) - pastDistance + newDistance) * scale;
            if (deltaDistance < 0) {
                console.log("Still negative", carNumber, initDistance,
                    deltaDistance, newRecord.distance, pastRecord.distance);
                deltaDistance = 0;
            }
        }

        let anime = carContainer
            .animate(deltaTime)
            .during((pos, morph, eased) => {
                let distance = distanceFromStart + (pos * deltaDistance);
                //this.positionCar(carContainer, car, distance);
                if (distance >= (this.path.length())) {
                    distance = distance - (this.path.length());
                }
                let p = this.path.pointAt(distance);
                let angle;
                if (distance < twoToOne) {
                    angle = 180;
                    //xOffset -= halfOfBox;
                } else if (distance < oneToEight) {
                    angle = 180 - (distance - twoToOne) / scalledTurnArc * 90;
                } else if (distance < eightToSeven) {
                    angle = 90;
                    //yOffset -= numberOffset;
                } else if (distance < sevenToSix) {
                    angle = 90 - (distance - eightToSeven) / scalledTurnArc * 90;
                } else if (distance < sixToFive) {
                    angle = 0;
                    //xOffset -= halfOfBox;
                } else if (distance < fiveToFour) {
                    angle = (distance - sixToFive) / scalledTurnArc * -90;
                } else if (distance < fourToThree) {
                    angle = 270;
                } else if (distance < threeToTwo) {
                    angle = 270 + (distance - fourToThree) / scalledTurnArc * -90;
                }
                carContainer.center(p.x - trckoff * carScale / 2 + 0.5 * carScale,
                    p.y - trckoff * carScale / 3 + 0.5 * carScale);
                car.rotate(angle);
                //carNumberGroup.center(xOffset, yOffset);
            }).after(() => {

                this.pastRecords[carNumber] = newRecord;
                if (carNumber === window.debugCar) {
                    console.log("Timing", this.diffs[carNumber] - Date.now() + newRecord.time, this.timeReducers[carNumber], rawDeltaTime);
                }
                if (diff < 0 && this.timeReducers[carNumber] > 0.1) {
                    this.timeReducers[carNumber] -= 0.01;
                } else if (diff > 0 && this.timeReducers[carNumber] < 1.5) {
                    this.timeReducers[carNumber] += 0.01;
                }

                //override all if buffer is running out
                if (this.cars[carNumber].length < 3) {
                    this.timeReducers[carNumber] = 1.1;
                    if (carNumber === window.debugCar) {
                        console.log("time reducer overridden", this.cars[carNumber].length);
                    }
                }

                if (carNumber === window.debugCar) {
                    console.log("Completed animation", deltaTime, rawDeltaTime, this.diffs[carNumber], this.timeReducers[carNumber], "Speed adjusted", speedAdjusted);
                }
                cb();
            });

        this.cars[carNumber].anime = anime;

    };

    addCar = (image, trackOffset = 1.8, carNumber) => {


        let carContainer = this.draw.group();
        let boundingBoxMax = trackWidth * widthSCale;//Math.sqrt(Math.pow(4.8 * carScale, 2) * 2);

        let car = carContainer.image(image).size(4.8 * carScale, 1.8 * carScale);
        let rect = carContainer.rect(boundingBoxMax, boundingBoxMax);
        rect.fill('transparent');
        // car.move(boundingBoxMax / 2 - 4.8 * carScale / 2, boundingBoxMax / 2 - trackOffset * carScale / 2);
        car.move(boundingBoxMax / 2 - 4.8 * carScale / 2, boundingBoxMax / 2 - carScale / 2);
        //car.center(carContainer.cx(), carContainer.cy());
        car.rotate(180);
        car.opacity(0);

        // let carNumberGroup = this.draw.group();
        // carNumberGroup.center(carContainer.cx(), carContainer.cy());
        // let radius = 18 * carScale / 9.230726182704274;
        // carNumberGroup.circle(18 * carScale / 9.230726182704274).attr({
        //     fill: "white",
        //     stroke: "#3E3E3E",
        //     "stroke-width": 1
        // }).center(carContainer.cx(), carContainer.cy());
        //
        // carNumberGroup.text(carNumber).font({size: 14 * carScale / 9.230726182704274})
        //     .center(carContainer.cx(), carContainer.cy());
        // carNumberGroup.opacity(0);

        let numberOffset = 0;// (boundingBoxMax - radius) / 2;


        this.timeReducers[carNumber] = 1;
        this.bufferingCars++;

        let initPoint = this.path.pointAt(0);
        carContainer.center(initPoint.x, initPoint.y);

        let frameBuffer = new CBuffer(this.bufferSize);
        frameBuffer.carContainer = carContainer;
        frameBuffer.rect = rect;

        let firstTime = true;
        let excessCount = 0;

        let animationCallback = (data = frameBuffer.shift()) => {
            if (carNumber === window.debugCar) {
                console.log("Buffer size", frameBuffer.length);
            }
            if (data) {
                this.animateCar(carNumber, carContainer, null,
                    numberOffset, car, data, animationCallback, trackOffset);
            } else {
                //could be because it has run out of buffer
                console.log("No records in buffer", carNumber);
                this.bufferingCars++;
                car.opacity(0.5);
                //carNumberGroup.opacity(0.5);
                firstTime = true;
                excessCount = 0;
                // this.setState({
                //     intermediateBuffering: true
                // });
                //
                // setTimeout(() => {
                //     if (this.state.intermediateBuffering) {
                //         this.setState({
                //             intermediateBuffering: false
                //         });
                //     }
                // }, 60 * 1000)
            }
        };

        // frameBuffer.overflow = (data) => {
        //     console.log("DROPPED DUE TO BUFFER OVERFLOW");
        // };

        frameBuffer.eventAdded = (data) => {
            excessCount++;
            if (firstTime && excessCount === 1) { // five seconds buffering
                let startingRecord = frameBuffer.shift();
                this.diffs[carNumber] = Date.now() - startingRecord.time;
                firstTime = false;
                animationCallback(startingRecord);
                setTimeout(() => {
                    car.opacity(1);
                    //carNumberGroup.opacity(1);
                }, 1000);
                if (this.state.buffering || this.state.intermediateBuffering) {
                    this.setState({
                        buffering: false,
                        intermediateBuffering: false
                    });
                }
            }
        };


        return frameBuffer;
    };


    /*COLORING THE CONTAINER*/

    applyContainerColor(carNumber, fill, stroke, scheduleClearTimer = false) {
        if (this.cars[carNumber]) {
            this.cars[carNumber].rect.fill(fill).stroke(stroke);
            clearTimeout(this.cars[carNumber].containerColorTimer);
            if (scheduleClearTimer) {
                this.cars[carNumber].containerColorTimer = setTimeout(() => {
                    this.clearContainer(carNumber);
                }, 5000);
            }
        }
    }

    clearContainer = (carNumber) => {
        this.applyContainerColor(carNumber,
            'transparent',
            'transparent'
        );
    };

    criticalContainer = (carNumber) => {
        this.applyContainerColor(carNumber,
            'rgba(255,0,0,0.4)',
            'rgb(255,0,0)',
            true
        );
    };

    warningContainer = (carNumber) => {
        this.applyContainerColor(carNumber,
            'rgba(251,192,45 ,0.4)',
            '#FBC02D',
            true
        );
    };


    render() {
        return (
            <div className="ic-track">
                <div ref={(ref) => {
                    this.trackWrapper = ref;
                }} className="ic-track-wrapper" style={{visibility: !this.state.buffering ? 'visible' : 'hidden'}}
                     id="drawing">
                </div>
                {/*<PlayerRawDataComponent selectedCarNumber={this.props.selectedCarNumber}/>*/}
                <div className="ic-track-buffering"
                     style={{visibility: this.state.buffering || this.state.intermediateBuffering ? 'visible' : 'hidden'}}>
                    <div>
                        <img src={LOADER} width={20} alt="loading"/>
                        <p>Buffering.....</p>
                    </div>
                </div>
            </div>
        )
    }
}