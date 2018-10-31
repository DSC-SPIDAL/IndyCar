/**
 * @author Chathura Widanage
 */
const baseUrl = "http://j-093.juliet.futuresystems.org:5000";

class RestService {
    getUrl(path) {
        return `${baseUrl}/${path}`;
    }
}


const restService = new RestService();
export default restService;