import {combineReducers} from 'redux'
import {PlayerInfo} from "./PlayerInfoReducer";
import {AnomalyInfo} from "./AnomalyReducer";
import {RaceInfo} from "./RaceReducer";

export default combineReducers({
    PlayerInfo,
    AnomalyInfo,
    RaceInfo
});