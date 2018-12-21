import {SocketService} from "../services/SocketService";

export default class AnomalySubscriber {

    static instance;

    constructor() {
        if (AnomalySubscriber.instance) {
            return AnomalySubscriber.instance;
        }

        AnomalySubscriber.instance = this;

        this.socketService = new SocketService();

        this.socketService.subscribe("anomaly", (data) => {
            this.publish(data);
        });

        this.socketService.subscribe("init-anomaly", (data) => {
            this.publish(data);
        });

        this.subscribers = {};
    }

    subscribe = (key, subscriber) => {
        this.subscribers[key] = subscriber;
    };

    unsubscribe = (key) => {
        delete this.subscribers[key];
    };

    publish = (data) => {
        Object.values(this.subscribers).forEach(func => {
            func(data);
        })
    };
}