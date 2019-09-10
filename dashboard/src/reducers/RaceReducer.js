export const TIME_ACTION = "TIME_ACTION";

export const RaceInfo = (state = {time: ""}, action) => {
    switch (action.type) {
        case TIME_ACTION:
            state.time = action.time.split(".")[0];
            break;
    }
    return {...state};
};