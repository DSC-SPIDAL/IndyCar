import openSocket from "socket.io-client";
import CarInformationService from "./CarInformationService";

export class SocketService {

    static instance;

    constructor(host, port, store) {
        if (SocketService.instance) {
            return SocketService.instance;
        }

        SocketService.instance = this;

        this.host = host;
        this.port = port;
        this.store = store;
        this.socket = undefined;
    }

    start = (cb) => {
        this.socket = openSocket(`${this.host}:${this.port}`, {
            reconnection: true,
            reconnectionDelay: 1000,
            reconnectionDelayMax: 5000,
            reconnectionAttempts: 99999,
            transports: ['websocket', 'flashsocket'
                , 'htmlfile'
                , 'xhr-polling'
                , 'jsonp-polling']
        });

        this.socket.on('connect', () => {
            console.log("Connected to server", this.socket);
            this.socket.on("reload", () => {
                console.log("Reloading page in 5 seconds...");
                setTimeout(() => {
                    window.location.reload(true);
                }, 5000);
            });

            //for latency calculation
            this.socket.on('ping', () => {
                this.send('pongi');
            });
            CarInformationService.init(this, this.store);
            cb();
        });
    };

    send = (event, msg) => {
        this.socket.emit(event, msg);
    };

    subscribe = (event, cb) => {
        this.socket.on(event, cb);
    };

    unsubscribe = (event, cb) => {
        this.socket.off(event, cb);
    };
}