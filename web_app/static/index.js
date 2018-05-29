/**
 * @author Chathura Widanage
 */

//Actual Dimensions
let trackLongLength = 1006;
let trackShortLength = 201;
let trackWidth = 15.2;
let turnArc = 402;
let turnRadius = turnArc * 4 / (2 * Math.PI);

//calculating optimum scale for screen size
var scale = Math.min((window.innerWidth - 200) / (trackLongLength + (turnRadius * 2)), (window.innerHeight - 200) / (trackShortLength + turnRadius * 2));
var widthSCale = scale * 5;
var carScale = 15;

//scaled dimensions
let scaledRadius = turnRadius * scale;
let scalledTurnArc = 2 * Math.PI * scaledRadius / 4;

var longStraightWay = {
    length: trackLongLength * scale,
    width: trackWidth * widthSCale
};

var shortStraightWay = {
    length: trackShortLength * scale,
    width: trackWidth * widthSCale
};

var roadTexture = "/static/road_two.jpg";

var draw = SVG('drawing').size('100%', '100%');

/* var outerTrack = draw.rect(longStraightWay.length + (2 * scaledRadius), shortStraightWay.length + (2 * scaledRadius))
     .attr({ fill: 'transparent', stroke: roadTexture, 'stroke-width': 15.2 * widthSCale }).radius(turnRadius * scale);
 outerTrack.center(window.innerWidth / 2, window.innerHeight / 2);*/


//adding display padding for clarity
let paddingTop = (window.innerHeight - shortStraightWay.length - 2 * scaledRadius) / 2;
let paddingLeft = (window.innerWidth - longStraightWay.length - 2 * scaledRadius) / 2;

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
let oneToTwo = longStraightWay.length;
let twoToThree = oneToTwo + scalledTurnArc;
let threeToFour = twoToThree + shortStraightWay.length;
let fourToFive = threeToFour + scalledTurnArc;
let fiveToSix = fourToFive + longStraightWay.length;
let sixToSeven = fiveToSix + scalledTurnArc;
let sevenToEight = sixToSeven + shortStraightWay.length;
let eightToOne = sevenToEight + scalledTurnArc;

console.log("Total", eightToOne);

//drawing track
let path = draw.path
    (`
        M${x1} ${y1} 
        L${x2} ${y2} 
        Q ${xc2} ${yc2} ${x3} ${y3}
        L${x4} ${y4}
        Q ${xc3} ${yc3} ${x5} ${y5}
        L${x6} ${y6}
        Q ${xc4} ${yc4} ${x7} ${y7}
        L${x8} ${y8}
        Q ${xc1} ${yc1} ${x1} ${y1}
    `)
    .attr({ stroke: roadTexture, fill: 'transparent', 'stroke-width': longStraightWay.width })
    .center(window.innerWidth / 2, window.innerHeight / 2);
let length = path.length();

//start pattern
let pattern = draw.pattern(20, 20, function (add) {
    add.rect(20, 20).fill('#fff')
    add.rect(10, 10)
    add.rect(10, 10).move(10, 10)
});

//start line
let startLine = draw.rect(20 * scale, longStraightWay.width).fill(pattern).move(x1, y1 - (longStraightWay.width / 2));

console.log("Length", length / scale);

// Start - List of functions for animating laps

function getSectionDetailsArr(sectionInfo, sectionTimingInfo) {
  var i;
  var sectionDetails = [];
  for (i=0; i< sectionTimingInfo.length; i++) {
    var sectionId = sectionTimingInfo[i].section_id;
    var j;
    for (j=0; j<sectionInfo.length; j++) {
      var section = sectionInfo[j];
      if (sectionId===section.section_name) {
        var sectionDetail = {};
        sectionDetail.sectionId = sectionId;
        sectionDetail.sectionLength = section.section_length
        sectionDetail.section_time = sectionTimingInfo[i].last_section_time
        sectionDetails.push(sectionDetail)
      }
    }
  }
  
  return sectionDetails
}

function getLengthPropArr(sectionDetails) {
  var k, totalLength=0;
  var lengthPropArr = []
  
  for (k=0; k<sectionDetails.length;k++){
    totalLength = totalLength + parseInt(sectionDetails[k].sectionLength)
  }

  var lengthProp;
  for (k=0; k<sectionDetails.length;k++){
    lengthProp = sectionDetails[k].sectionLength/totalLength
    lengthPropArr.push(lengthProp);
  }

  return lengthPropArr
}

function getStartPosArr(lengthPropArr) {
  var k, startPosArr = []
  startPosArr.push(0)
  
  var lengthProp;
  for (k=1; k<lengthPropArr.length;k++){
    startPosArr.push(startPosArr[k-1] + lengthPropArr[k-1])
  }

  return startPosArr
}

function getAnimationTimesArr(sectionDetails) {
  var l, secTime, animationTimes = [];
  for (l=0; l<sectionDetails.length;l++){
    secTime = sectionDetails[l].section_time;
    var a = secTime.split(/[:.]+/);
    var seconds = (+a[0]) * 60 * 60 + (+a[1]) * 60 + (+a[2]) + parseFloat("0." + a[3]);
    //divide real time by 4 to show in animation
    animationTimes.push(Math.ceil((seconds * 1000)/4)) 
  }
  
  return animationTimes
}


function animateSection(carContainer, car, animationTime, sectionLengthProp, startPos, carRect, fillColor) {
  carContainer.animate(animationTime).during(function (pos, morph, eased) {
    var adjustedPos = (eased*sectionLengthProp) + startPos
    var p = path.pointAt(adjustedPos * length)
    carContainer.center(p.x, p.y);

    let distance = length * adjustedPos;
    var angle;
    if (distance < oneToTwo) {
      angle = 360;
    } else if (distance < twoToThree) {
      angle = (distance - oneToTwo) / scalledTurnArc * 90;
    } else if (distance < threeToFour) {
      angle = 90;
    } else if (distance < fourToFive) {
      angle = 90 + (distance - threeToFour) / scalledTurnArc * 90;
    } else if (distance < fiveToSix) {
      angle = 180;
    } else if (distance < sixToSeven) {
      angle = 180 + (distance - fiveToSix) / scalledTurnArc * 90;
    } else if (distance < sevenToEight) {
      angle = 270;
    } else if (distance < eightToOne) {
      angle = 270 + (distance - sevenToEight) / scalledTurnArc * 90;
    }

    car.rotate(angle)
    carRect.rotate(angle)
    carRect.fill(fillColor)

  })

}

//Animation of one Lap
function animateLap(carContainer, car, animationTimesArr, lengthPropArr, startPosArr, carRect, color) {
  for (i=0; i<animationTimesArr.length; i++) {
    var fillColor;
    if (i%2==0) {
      fillColor = color
    } else {
      fillColor = '#000'
    }
    animateSection(carContainer, car, animationTimesArr[i], lengthPropArr[i], startPosArr[i], carRect, fillColor)
  }
}


function setLapInformation(lap_num, delay_time) {
  setTimeout(function(){
    $('#lapinfo').text('Lap Number: ' + lap_num)
  }, delay_time);
}


function setSpeedInformation(speed_arr, delay_time) {
  setTimeout(function(){
    var i
    for (i=0; i<speed_arr.length; i++) {
      $('#section' + (i+1)).text('Section ' + (i+1) + ': ' + speed_arr[i])
    }
  }, delay_time);
}


function animateCar(image, animationTimesArrAll, lengthPropArrAll, startPosArrAll) {
  var carContainer = draw.group();
  var car = carContainer.image(image).size(5.5 * carScale, 2.5 * carScale);
  let boundingBoxMax = Math.sqrt(Math.pow(4.8 * carScale, 2) * 2);
  carContainer.rect(boundingBoxMax, boundingBoxMax).fill('transparent');
  car.move(boundingBoxMax / 2 - 4.8 * carScale / 2, boundingBoxMax / 2 - 1.8 * carScale / 2);
  var carRect = carContainer.rect(4.8 * carScale, 1.8 * carScale).stroke(1).fill('#ccc').attr({ 'stroke-width': 1 })

  var fillColors = []
  fillColors.push('#42aaf4')
  fillColors.push('#f9a2de')
  fillColors.push('#29516d')
  fillColors.push('#a8217e')
  fillColors.push('#031f33')

  var g;
  for (g=0; g<lengthPropArrAll.length; g++) {
    animateLap(carContainer, car, animationTimesArrAll[g], lengthPropArrAll[g], startPosArrAll[g], carRect, fillColors[g])
  }

}

function setWeatherInformation(weatherInfo, delay) {
  if ((delay < 1000) & (delay != 0)) {
    delay = 2000
  }
  
  setTimeout(function(){
    $("#ambientTemp").text("Ambient Temperature: " + weatherInfo.ambient_temp)
    $("#barometricPressure").text("Barometric Pressure: " + weatherInfo.barometric_pressure)
    $("#relativeHumidity").text("Relative Humidity: " + weatherInfo.relative_humidity + "%")
    $("#timeofday").text("Time of Day: " + weatherInfo.time_of_day)
  }, delay)
}

function getTotalSec(tod) {
  var todcomp = tod.split(/[:.]+/)
  var hr2sec = todcomp[0]*60*60
  var min2sec = todcomp[1]*60
  var sec = todcomp[2]
  var fracsec = parseFloat('0.' + todcomp[3])
  
  var totalSec = hr2sec + min2sec + sec + fracsec
  
  return totalSec
}

function getSectionLengthsArr(sectionInfo, sectionTimingInfo) {
  var h
  var sectionLengths = []
  for (h=0; h<sectionTimingInfo.length; h++) {
    var sectionId = sectionTimingInfo[h].section_id
    var j
    for (j=0; j<sectionInfo.length; j++) {
      if (sectionInfo[j].section_name === sectionId) {
        var length = sectionInfo[j].section_length * 1.57828 * 0.00001
        sectionLengths.push(length)
        break
      }
    }
  }

  return sectionLengths

}

$(document).ready(function() {
  
  $.ajax({
    url: 'http://j-093.juliet.futuresystems.org:5000/sectioninfo',
    data: {
        format: 'json'
    },
    error: function() {
        console.log("An error occurred - section info");
    },
    success: function(data) {
        var sectionInfo = data;
        var carnum = 9;
        $('#carinfo').text('Car Number: ' + carnum)
	var lapbeg = 1;
        var lapend = 5;
	$.ajax({
          url: 'http://j-093.juliet.futuresystems.org:5000/gettiminginfoinlaprange?car_num=' + carnum + '&lap_beg=' + lapbeg + '&lap_end=' + lapend,
          data: {
              format: 'json'
          },
          error: function() {
              console.log("An error occurred - section timing info");
          },
          success: function(data) {
              var sectionTimingInfoforLapRange = data;
              var g;
               
              var lengthPropArrAll = []
              var startPosArrAll = []
              var animationTimesArrAll = []
              var sectionLengthsArrAll = []
              //Animate Car Num 9 for Laps 1-5
              for (g=0; g<sectionTimingInfoforLapRange.length; g++) {
                var sectionTimingInfo = sectionTimingInfoforLapRange[g].section_timing;
                sectionDetails = getSectionDetailsArr(sectionInfo, sectionTimingInfo)
		lengthPropArr = getLengthPropArr(sectionDetails)
                lengthPropArrAll.push(lengthPropArr)
                startPosArr = getStartPosArr(lengthPropArr)
                startPosArrAll.push(startPosArr)
                animationTimes = getAnimationTimesArr(sectionDetails) //in ms
                animationTimesArrAll.push(animationTimes)
                sectionLengths = getSectionLengthsArr(sectionInfo, sectionTimingInfo) //in miles
                sectionLengthsArrAll.push(sectionLengths)
              }

              animateCar('/static/cars/car_0' + carnum  + '.png', animationTimesArrAll, lengthPropArrAll, startPosArrAll)
              //Set Lap Information
              var delayTimes = []
              var lapNums = []
              delayTimes.push(0)
              var prevDelay = delayTimes[0]
              for (g=1; g < animationTimesArrAll.length; g++) {
                var timeArr = animationTimesArrAll[g]
                delay = prevDelay + timeArr.reduce(function(acc, val) { return parseInt(acc) + parseInt(val); });
                console.log(prevDelay)
                delayTimes.push(delay)
                prevDelay = delay
                lapNums.push(g)
              }
              lapNums.push(g)
                            
              let i
              for (i=0; i<delayTimes.length; i++) {
                setLapInformation(lapNums[i], delayTimes[i])
              }

              //Set Weather Information
              $.ajax({
                url: 'http://j-093.juliet.futuresystems.org:5000/weather_data',
                data: {
                  format: 'json'
                },
                error: function() {
                  console.log("An error occurred - weather info");
                },
                success: function(data) {
                  var begTimIndex = 0
                  var endTimIndex = sectionTimingInfoforLapRange.length - 1
                  var startTimingArr = sectionTimingInfoforLapRange[begTimIndex].section_timing
                  var endTimingArr = sectionTimingInfoforLapRange[endTimIndex].section_timing

                  var beg = 0
                  var end = endTimingArr.length - 1

                  var startTime = startTimingArr[beg].elapsed_time
                  var endTime = endTimingArr[end].elapsed_time

                  var timesArr = data
                  
                  let weatherInfoArr = timesArr.filter(function(elem){
                    var time = elem.time_of_day
                    if ((startTime <= time) & (time <= endTime)) {
                      return elem
                    }
                  })
                   
                  var h
                  var startTotalSec = getTotalSec(startTime)
                  var delayTimes = []
                  delayTimes.push(0)
                  for (h=1; h<weatherInfoArr.length; h++) {
	            var info = weatherInfoArr[h-1]
	            var tod = info.time_of_day
                    var totalSec = getTotalSec(tod)
                    delayTimes.push(Math.ceil((totalSec-startTotalSec)/4))
                  }
                  
                  for (h=0;h<weatherInfoArr.length; h++) {
                    setWeatherInformation(weatherInfoArr[h], delayTimes[h])
                  }

                },
                type: 'GET'
              });

              //Set Entry Information
              $.ajax({
                url: 'http://j-093.juliet.futuresystems.org:5000/getentryinfo?car_num=' + carnum,
                data: {
                  format: 'json'
                },
                error: function() {
                  console.log("An error occurred - race info");
                },
                success: function(data) {
                  var entryinfo = data.entry_info_data
                  console.log(entryinfo)
                  $('#carnum').text(entryinfo.car_num)
                  $('#drivername').text(entryinfo.driver_name)
                  $('#hometown').text("from " + entryinfo.home_town)
                  $('#teamname').text(entryinfo.team)
                  $('#teamid').text("#" + entryinfo.team_id)
                  $('#engine').text("Engine: " + entryinfo.engine)
                  $('#competitorid').text("Competitor: " + entryinfo.competitor_identifier)
                  $('#license').text("License: " + entryinfo.license)
                },
                type: 'GET'
             });

             //Set Speed Information
             var k
             var speedArrAll = [] 
             for (k=0; k<sectionLengthsArrAll.length; k++) {
               var elem = sectionLengthsArrAll[k]
               var animTime = animationTimesArrAll[k] 
               var j
               var speedArr = []
               for (j=0; j<elem.length; j++) {
                 var length = elem[j]
                 var time = animTime[j] * (2.77778 * 0.0000001)
                 speedArr.push(length/time)
               }
               speedArrAll.push(speedArr)
             }             
             
             let d
             for (d=0; d<delayTimes.length; d++) {
               setSpeedInformation(speedArrAll[d], delayTimes[d])
             }

          },
          type: 'GET'
        });
    },
    type: 'GET'
  });

  //Display Race Info
  $.ajax({
    url: 'http://j-093.juliet.futuresystems.org:5000/raceinfo',
    data: {
        format: 'json'
    },
    error: function() {
        console.log("An error occurred - race info");
    },
    success: function(data) {
        var eventinfo = data.event_name + '  ' + data.run_name
        var timeinfo = data.start_date
        $('#eventinfo').text(eventinfo)
        $('#timeinfo').text(timeinfo)
    },
    type: 'GET'
  });

});
