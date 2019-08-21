export const ACTION_PLAYER_INFO_RECEIVED = "ACTION_PLAYER_INFO_RECEIVED";
export const ACTION_PLAYER_RANK_RECEIVED = "ACTION_PLAYER_RANK_RECEIVED";
export const ACTION_PLAYER_LAP_RECORD_RECEIVED = "ACTION_PLAYER_LAP_RECORD_RECEIVED";


export const PlayerInfo = (state = [], action) => {
    switch (action.type) {
        case ACTION_PLAYER_INFO_RECEIVED:
            state.players = {...state.players, [action.carNumber]: action.player};
            break;
        case ACTION_PLAYER_RANK_RECEIVED:
            state.ranks = action.ranks;
            break;
        case ACTION_PLAYER_LAP_RECORD_RECEIVED:
            if (!state.laps) {
                state.laps = {};
            }
            if (!state.laps[action.carNumber]) {
                state.laps[action.carNumber] = {};
            }
            state.laps[action.carNumber][action.lapRecord.completedLaps] = action.lapRecord;

            //update the last lap of the player
            if (!state.lastLaps) {
                state.lastLaps = {};
            }

            if (!state.lastLaps[action.carNumber]) {
                state.lastLaps[action.carNumber] = {
                    lap: 0,
                    time: 0
                };
            }

            let lastLap = state.lastLaps[action.carNumber];

            if (lastLap.lap < action.lapRecord.completedLaps) {
                lastLap.lap = action.lapRecord.completedLaps;
                lastLap.time = action.lapRecord.time;
            }
            break;
    }
    return {...state};
};