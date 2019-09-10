/**
 * @author Chathura Widanage
 */
import {TIME_ACTION} from "../reducers/RaceReducer";

export default class RaceInformationService {

    static init(socket, store) {
        socket.subscribe("time", time => {
            store.dispatch({
                type: TIME_ACTION,
                time
            })
        });
    }
}