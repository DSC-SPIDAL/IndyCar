import axios from "axios";
import RestService from "./RestService";

let carDistances = {};
let rankSubscribers = [];

/**
 * @author Chathura Widanage
 */
export default class CarInformationService {

    static getCarInformation(carNumber) {
        return axios.get(RestService.getUrl(`getentryinfo?car_num=${carNumber}`));
    }

    static getCarLapTimes(carNumber) {
        return axios.get(RestService.getUrl(`getlaptimes?car_num=${carNumber}`));
    }

    static getCarList() {
        return axios.get(RestService.getUrl(`carslist`));
    }

    static getCarRank(carNumber) {
        return axios.get(RestService.getUrl(`getoverallrank?car_num=${carNumber}`));
    }

    static subscribeToRankChanges(cb) {
        rankSubscribers.push(cb);
    }

    static reportDistance(carNumber, distance, currentLap) {
        carDistances[carNumber] = {
            distance, currentLap, carNumber
        };

        rankSubscribers.forEach(subs => {
            subs(carDistances);
        })
    }

    static getSectionTiming(carNumber, lapNumber) {
        return axios.get(RestService.getUrl(`sectiontiminginfo?car_num=${carNumber}&lap_num=${lapNumber}`));
    }
}