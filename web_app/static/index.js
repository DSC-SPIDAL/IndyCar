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
let twoToOne = longStraightWay.length;
let oneToEight = twoToOne + scalledTurnArc;
let eightToSeven = oneToEight + shortStraightWay.length;
let sevenToSix = eightToSeven + scalledTurnArc;
let sixToFive = sevenToSix + longStraightWay.length;
let fiveToFour = sixToFive + scalledTurnArc;
let fourToThree = fiveToFour + shortStraightWay.length;
let threeToTwo = fourToThree + scalledTurnArc;

console.log("Total", threeToTwo);

// drawing track (counter-clock direction)
let path = draw.path
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
let startLine = draw.rect(20 * scale, longStraightWay.width).fill(pattern).move(x2, y1 - (longStraightWay.width / 2));

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
    animationTimes.push(Math.ceil((seconds * 1000)/5)) 
  }
  
  return animationTimes
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

//function animateSection(carContainer, car, animationTime, sectionLengthProp, startPos, carRect, fillColor) {
function animateSection(carContainer, car, animationTime, sectionLengthProp, startPos, fillColor) {
  carContainer.animate(animationTime).during(function (pos, morph, eased) {
    var adjustedPos = (eased*sectionLengthProp) + startPos
    var p = path.pointAt(adjustedPos * length)
    carContainer.center(p.x, p.y);

    let distance = length * adjustedPos;
    var angle;
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

    car.rotate(angle)
    //carRect.rotate(angle)
    //carRect.fill(fillColor)

  })

}

//Animation of one Lap
//function animateLap(carContainer, car, animationTimesArr, lengthPropArr, startPosArr, carRect, color) {
function animateLap(carContainer, car, animationTimesArr, lengthPropArr, startPosArr, color) {
  for (i=0; i<animationTimesArr.length; i++) {
    var fillColor;
    if (i%2==0) {
      fillColor = color
    } else {
      fillColor = '#000'
    }
    animateSection(carContainer, car, animationTimesArr[i], lengthPropArr[i], startPosArr[i], fillColor)
  }
}

function animateCar(image, animationTimesArrAll, lengthPropArrAll, startPosArrAll, y) {
  var carContainer = draw.group();
  var car = carContainer.image(image).size(5.5 * carScale, 2.5 * carScale);
  let boundingBoxMax = Math.sqrt(Math.pow(4.8 * carScale, 2) * 2);
  carContainer.rect(boundingBoxMax, boundingBoxMax).fill('transparent');
  car.move(boundingBoxMax / 2 - 4.8 * carScale / 2, boundingBoxMax / 2 - y * carScale / 2);
  //var carRect = carContainer.rect(4.8 * carScale, 1.8 * carScale).stroke(1).fill('#ccc').attr({ 'stroke-width': 1 })
  
  var fillColors = []
  fillColors.push('#42aaf4')
  fillColors.push('#f9a2de')
  fillColors.push('#29516d')
  fillColors.push('#a8217e')
  fillColors.push('#031f33')

  var g;
  for (g=0; g<lengthPropArrAll.length; g++) {
    //animateLap(carContainer, car, animationTimesArrAll[g], lengthPropArrAll[g], startPosArrAll[g], carRect, fillColors[g])
    animateLap(carContainer, car, animationTimesArrAll[g], lengthPropArrAll[g], startPosArrAll[g], fillColors[g])
  }

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

function setLapInformation(lap_num, delay_time) {
  setTimeout(function(){
    $('#lapinfo').text('Lap Number: ' + lap_num)
  }, delay_time);
}

function setLapInformation_Outer(animationTimesArrAll) {
  var delayTimes = []
  var lapNums = []
  delayTimes.push(0)
  var prevDelay = delayTimes[0]
  for (g=1; g < animationTimesArrAll.length; g++) {
    var timeArr = animationTimesArrAll[g]
    delay = prevDelay + timeArr.reduce(function(acc, val) { return parseInt(acc) + parseInt(val); });
    delayTimes.push(delay)
    prevDelay = delay
    lapNums.push(g)
  }
  lapNums.push(g)
                           
  let i
  for (i=0; i<delayTimes.length; i++) {
    setLapInformation(lapNums[i], delayTimes[i])
  }

  return delayTimes
}

function setEntryInformation(carnum, order) {
  $.ajax({
    url: 'http://j-093.juliet.futuresystems.org:5000/getentryinfo?car_num=' + carnum,
    data: {
      format: 'json'
    },
    error: function() {
      console.log("An error occurred - race info");
    },
    success: function(data) {
      var entryinfo = data;
      var teaminfo = entryinfo.team + " #" + entryinfo.team_id
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-Driver-Info-Car_num').text(entryinfo.car_num)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-driver_name').text(entryinfo.driver_name)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-Hometown').text("from " + entryinfo.home_town)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-Team').text(teaminfo)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-Driver-Info-Engine').text(entryinfo.engine)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-competitor_identifier').text(entryinfo.competitor_identifier)
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-licence').text(entryinfo.license)
    },
    type: 'GET'
  });
}

function setSpeedInformation(speed_arr, delay_time, order) {
  setTimeout(function(){
    var i
    for (i=0; i<speed_arr.length; i++) {
      var num = "" + (i+1);
      if (num.length==1) {
          num = "0" + num
      }
      $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-section-' + num).text(speed_arr[i].toFixed(1))
    }
  }, delay_time);
}

function setSpeedInformation_Outer(sectionLengthsArrAll, animationTimesArrAll, delayTimes, order) {
  var k
  var speedArrAll = [] 
  for (k=0; k<sectionLengthsArrAll.length; k++) {
    var elem = sectionLengthsArrAll[k]
    var animTime = animationTimesArrAll[k] 
    var j
    var speedArr = []
    for (j=0; j<elem.length; j++) {
      var length = elem[j]
      var time = animTime[j] * (2.77778 * 0.0000001) * 5 //since we divided by 4 to get animation times
      speedArr.push(length/time)
    }
    speedArrAll.push(speedArr)
  }             
         
  let d
  for (d=0; d<delayTimes.length; d++) {
    setSpeedInformation(speedArrAll[d], delayTimes[d], order)
  }
}

function setRankforCar(order, rank, driver_name, delay) {
  setTimeout(function() {
    $('.sk-Driver-Information-and-Speed_-rank-' + order + ' .sk-rank').text(rank);
    $('.sk-Rank-' + order + '-text').text(rank + " " + driver_name)
  }, delay)
}

function setRankInformation(carnum, lapbeg, lapend, order, delayTimes) {
  $.ajax({
    url: 'http://j-093.juliet.futuresystems.org:5000/getrankinfo?car_num=' + carnum +'&lap_beg=' + lapbeg + "&lap_end=" + lapend,
    data: {
        format: 'json'
    },
    error: function() {
        console.log("An error occurred - get rank info");
    },
    success: function(data) {
    	var i
        for (i=0; i< delayTimes.length; i++) {
          setRankforCar(order, data[i].rank, data[i].driver_name, delayTimes[i])
        }
    },
    type: 'GET'
  });
}

var sectionInfoAllCars = [];

function getLapRangeInfo(carnum, lapbeg, lapend, sectionInfo, order, y) {
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
      sectionInfoAllCars.push(sectionTimingInfoforLapRange);
      var g;
               
      var lengthPropArrAll = []
      var startPosArrAll = []
      var animationTimesArrAll = []
      var sectionLengthsArrAll = []
      //Animate Car Num for Lap Range
      for (g=0; g<sectionTimingInfoforLapRange.length; g++) {
        var sectionTimingInfo = sectionTimingInfoforLapRange[g].section_timing;
        sectionDetails = getSectionDetailsArr(sectionInfo, sectionTimingInfo)
        lengthPropArr = getLengthPropArr(sectionDetails)
        lengthPropArrAll.push(lengthPropArr)
        startPosArr = getStartPosArr(lengthPropArr)
        startPosArrAll.push(startPosArr)
        animationTimes = getAnimationTimesArr(sectionDetails) //in ms and divided by 4
        animationTimesArrAll.push(animationTimes)
        sectionLengths = getSectionLengthsArr(sectionInfo, sectionTimingInfo) //in miles
        sectionLengthsArrAll.push(sectionLengths)
      }

      var carnumstr = "" + carnum
      if (carnumstr.length == 1) {
        carnumstr = "0" + carnumstr
      }
      
      setTimeout(function() {
        animateCar('/static/cars/car_' + carnumstr + '.png', animationTimesArrAll, lengthPropArrAll, startPosArrAll, y)
      
        //Set Lap Information
        var delayTimes = setLapInformation_Outer(animationTimesArrAll)

        //Set Entry Information
        setEntryInformation(carnum, order)

        //Set Speed Information
        setSpeedInformation_Outer(sectionLengthsArrAll, animationTimesArrAll, delayTimes, order)

        //Set Rank Information
        setRankInformation(carnum, lapbeg, lapend, order, delayTimes)
      }, 1000)
    },
    type: 'GET'
  });
}

function getSectionTimingForEachCar() {
    var sectionTimes =[];
    for(var i = 0; i < sectionInfoAllCars.length; i++) {

        var sectionTimeCar = sectionInfoAllCars[i];

        var endTimIndex = sectionTimeCar.length - 1;
        var startTimingArr = sectionTimeCar[0].section_timing;
        var endTimingArr = sectionTimeCar[endTimIndex].section_timing;

        var end = endTimingArr.length - 1;
        var section_timing = {};
        section_timing['start_time'] = startTimingArr[0].elapsed_time;
        section_timing['end_time'] = endTimingArr[end].elapsed_time;
        sectionTimes.push(section_timing);
    }
    return sectionTimes;
}

function setWeatherInformation(weatherInfo) {
    var delay = weatherInfo['delay_time'];
    if ((delay < 1000) & (delay != 0)) {
        delay = 2000
    }
    setTimeout(function () {
        $(".sk-ambient_temp").text(weatherInfo.ambient_temp);
        $(".sk-barometric_pressure").text(weatherInfo.barometric_pressure);
        $(".sk-relative_humidity").text(weatherInfo.relative_humidity + "%");
        $(".sk-time_of_day").text(weatherInfo.time_of_day);
    }, delay)
}

function setWeatherInformation_Outer() {
    $.ajax({
        url: 'http://j-093.juliet.futuresystems.org:5000/weather_data',
        data: {
            format: 'json'
        },
        error: function () {
            console.log("An error occurred - weather info");
        },
        success: function (data) {
            var timesArr = data;
            var weatherData = [];
            var sectionTimes = getSectionTimingForEachCar();
            for(var j = 0; j < sectionTimes.length; j++) {
                var startTime = sectionTimes[j]['start_time'];
                var endTime = sectionTimes[j]['end_time'];
                var weather_info = {};
                weather_info['start_time'] = startTime;
                let weatherInfoArr = timesArr.filter(function (elem) {
                    var time = elem.time_of_day;
                    if ((startTime <= time) & (time <= endTime)) {
                        return elem
                    }
                });
                weather_info['weather_data'] = weatherInfoArr;
                weatherData.push(weather_info);
            }

            var weatherInDisplay = [];
            for (var h = 0; h < weatherData.length; h++) {
                var startTotalSec = getTotalSec(weatherData[h]['start_time']);
                var weatherInfo = weatherData[h]['weather_data'];
                for(var k = 0; k < weatherInfo.length; k++) {
                    var info = weatherInfo[k];
                    var tod = info.time_of_day;
                    var totalSec = getTotalSec(tod);
                    info.delay_time = Math.ceil((totalSec - startTotalSec) / 4);
                    weatherInDisplay.push(info);
                }
            }
            for (var h = 0; h < weatherInDisplay.length; h++) {
                setWeatherInformation(weatherInDisplay[h])
            }
        },
        type: 'GET'
    });
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
        var sectionInfo = data
	var lapbeg = 1
        var lapend = 5
        var carnum
        var y

        carnum = 9
        y = 0       
        getLapRangeInfo(carnum, lapbeg, lapend, sectionInfo, 1, y)
        
        carnum = 20
        y=4
        getLapRangeInfo(carnum, lapbeg, lapend, sectionInfo, 2, y)
        
        carnum = 12
        y=7
        getLapRangeInfo(carnum, lapbeg, lapend, sectionInfo, 3, y);

        // Set weather information
        setTimeout(function () {
            setWeatherInformation_Outer();
        }, 1000);
               
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
        var eventinfo = data.event_name.trim().toUpperCase()
        var runinfo = data.run_name.trim()
        var timeinfo = data.start_date.trim()
        $('.sk-event_name').text(eventinfo)
        $('.sk-run_name').text(runinfo)
        $('.sk-start_date').text(timeinfo)
    },
    type: 'GET'
  });

});

