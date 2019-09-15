import {
    ACTION_PLAYER_INFO_RECEIVED,
    ACTION_PLAYER_LAP_RECORD_RECEIVED,
    ACTION_PLAYER_RANK_RECEIVED
} from "../reducers/PlayerInfoReducer";

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

let cache = {
    [CAR_INFO_LISTENER]: {},
    [CAR_RANK_LISTENER]: {
        rankToCar: {},
        carToRank: {},
    }
};

let carInformation = {};

let carLapRecords = {};

/**
 * @author Chathura Widanage
 */
export default class CarInformationService {

    static init(socket, store) {
        //Car entries
        let addEntry = (entry) => {
            carInformation[entry.carNumber] = entry;
            this.notifyListeners(CAR_INFO_LISTENER, entry);

            store.dispatch({
                type: ACTION_PLAYER_INFO_RECEIVED,
                carNumber: entry.carNumber,
                player: entry
            })
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

            store.dispatch({
                type: ACTION_PLAYER_LAP_RECORD_RECEIVED,
                carNumber: lapRecord.carNumber,
                lapRecord: lapRecord
            });
        };
        socket.subscribe("lap-records", (lapRecords) => {
            Object.keys(lapRecords).forEach(carNum => {
                lapRecords[carNum].forEach(addLapRecord)
            });
        });

        socket.subscribe("lap-record", (lapRecord) => {
            addLapRecord(lapRecord);
        });

        socket.subscribe("ranking-data", rankingData => {
            store.dispatch({
                type: ACTION_PLAYER_RANK_RECEIVED,
                ranks: rankingData
            });
        });
    }

    static notifyListeners = (listeners, data) => {
        eventListeners[listeners].forEach(listener => {
            listener(data);
        });
    };

    static addEventListener(listener, func) {
        if (listener === CAR_INFO_LISTENER) {
            return carInformation;
        } else if (listener === CAR_RANK_LISTENER) {
            return cache[CAR_RANK_LISTENER];
        }
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