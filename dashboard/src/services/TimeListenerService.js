class TimeListenerService {

    constructor() {
        this.listeners = [];
    }

    addListener = (lst) => {
        this.listeners.push(lst);
    };

    notifyListeners = (time) => {
        this.listeners.forEach(lst => {
            lst(time);
        });
    }
}

const tls = new TimeListenerService();
export default tls;