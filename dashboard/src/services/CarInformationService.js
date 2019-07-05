export const CAR_INFO_LISTENER = "CAR_INFO_LISTENER";
export const CAR_LAP_LISTENER = "CAR_LAP_LISTENER";
export const CAR_LAP_BULK_LISTENER = "CAR_LAP_BULK_LISTENER";
export const CAR_RANK_LISTENER = "CAR_RANK_LISTENER";

let eventListeners = {
    [CAR_INFO_LISTENER]: [],
    [CAR_LAP_LISTENER]: [],
    [CAR_LAP_BULK_LISTENER]: [],
    [CAR_RANK_LISTENER]: []
};

let carInformation = {};

let carLapRecords = {};

/**
 * @author Chathura Widanage
 */
export default class CarInformationService {

    static init(socket) {
        //Car entries
        let addEntry = (entry) => {
            carInformation[entry.carNumber] = entry;
            this.notifyListeners(CAR_INFO_LISTENER, entry);
        };
        socket.subscribe("entry", (entry) => {
            addEntry(entry);
        });

        socket.subscribe("entries", (entries) => {
            entries.forEach(entry => {
                addEntry(entry);
            });
        });

        //Cat lap records
        let addLapRecord = (lapRecord) => {
            if (!carLapRecords[lapRecord.carNumber]) {
                carLapRecords[lapRecord.carNumber] = [];
            }
            carLapRecords[lapRecord.carNumber].push(lapRecord);
            this.notifyListeners(CAR_LAP_LISTENER, lapRecord);
        };
        socket.subscribe("lap-records", (lapRecords) => {
            Object.keys(lapRecords).forEach(carNum => {
                lapRecords[carNum].forEach(addLapRecord)
            });
        });

        socket.subscribe("lap-record", (lapRecord) => {
            addLapRecord(lapRecord);
        });

        socket.subscribe("ranks", (ranks) => {
            let rankMap = {
                rankToCar: {},
                carToRank: {},
            };
            ranks && ranks.forEach((car, i) => {
                if (car && car.carNumber && carInformation[car.carNumber]) {
                    carInformation[car.carNumber].rank = i + 1;
                }
                rankMap.rankToCar[i + 1] = car.carNumber;
                rankMap.carToRank[car.carNumber] = i + 1;
            });
            this.notifyListeners(CAR_RANK_LISTENER, rankMap);
        });
    }

    static notifyListeners = (listeners, data) => {
        eventListeners[listeners].forEach(listener => {
            listener(data);
        });
    };

    static addEventListener(listener, func) {
        eventListeners[listener].push(func);
    }

    static removeEventListener(listener, func) {
        let index = -1;
        eventListeners[listener].some((list, i) => {
            if (func === list) {
                index = i;
                return true;
            }
            return false;
        });
        if (index !== -1) {
            eventListeners[listener].splice(index, 1);
        }
    }

    static getCarInformation(carNumber) {
        return carInformation[carNumber] || {};
    }

    static getCarLapTimes(carNumber) {
        return carLapRecords[carNumber] || {};
    }

    static getLapTimes() {
        return carLapRecords;
    }

    static getCarList() {
        return carInformation;
    }
}