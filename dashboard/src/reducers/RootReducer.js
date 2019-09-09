import {combineReducers} from 'redux'
import {PlayerInfo} from "./PlayerInfoReducer";
import {AnomalyInfo} from "./AnomalyReducer";

export default combineReducers({
    PlayerInfo,
    AnomalyInfo
});