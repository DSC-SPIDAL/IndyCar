export const ACTION_PLAYER_CHANGED = "ACTION_PLAYER_CHANGED";
export const ACTION_CLEAR_ANOMALY_DATA = "ACTION_CLEAR_ANOMALY_DATA";
export const ACTION_ANOMALY_DATA_RECEIVED = "ACTION_ANOMALY_DATA_RECEIVED";

function trimArray(array, windowSize = 25) {
    if (array.length > windowSize) {
        array.splice(0, array.length - windowSize);
    }
}

function resetAnomalyData(state) {
    state.anomalyData = {
        RPM: {
            raw: [],
            score: [],
            color: [],
            last: 0
        },
        SPEED: {
            raw: [],
            score: [],
            color: [],
            last: 0
        },
        THROTTLE: {
            raw: [],
            score: [],
            color: [],
            last: 0
        },
        TIME: [],
        index: 0
    };
}

function handleAnomalyData(state, anomalyData) {
    if (anomalyData.carNumber != state.focusedPlayer) {
        return;
    }
    if (!state.anomalyData) {
        resetAnomalyData(state);
    }

    let anomalies = anomalyData.anomalies;

    //Time
    state.anomalyData.TIME.push(anomalyData.timeOfDayString);
    trimArray(state.anomalyData.TIME);

    Object.keys(anomalies).forEach(key => {
        state.anomalyData[key].raw.push(anomalies[key].rawData);
        state.anomalyData[key].last = anomalies[key].rawData;
        trimArray(state.anomalyData[key].raw);
        let anomalyScore = anomalies[key].anomaly;
        if (anomalyScore > 0.5) {
            state.anomalyData[key].color.push("#f1545d");
            state.anomalyData[key].score.push(0.2);
        } else if (anomalyScore > 0.3) {
            state.anomalyData[key].color.push("#f9924e");
            state.anomalyData[key].score.push(0.1);
        } else {
            state.anomalyData[key].color.push("#8fe588");
            state.anomalyData[key].score.push(0.05);
        }
        trimArray(state.anomalyData[key].score);
        trimArray(state.anomalyData[key].color);
    });
    state.anomalyData.index = anomalyData.index;
}


export const AnomalyInfo = (state = [], action) => {
    switch (action.type) {
        case ACTION_PLAYER_CHANGED:
            state.focusedPlayer = action.player;
            resetAnomalyData(state);
            break;
        case ACTION_ANOMALY_DATA_RECEIVED:
            handleAnomalyData(state, action.data);
            break;
        case ACTION_CLEAR_ANOMALY_DATA:
            resetAnomalyData(state);
            break;
    }

    if (!state.focusedPlayer) {
        state.focusedPlayer = "21";
    }

    if (!state.anomalyData) {
        resetAnomalyData(state);
    }

    return {...state};
};