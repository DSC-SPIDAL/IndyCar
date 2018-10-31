import RestService from "./RestService";
import axios from "axios";

/**
 * @author Chathura Widanage
 */
class RaceInformationService {

    getRaceInformation() {
        return axios.get(RestService.getUrl("raceinfo"));
    }
}

const raceInfoService = new RaceInformationService();
export default raceInfoService;