import openSocket from "socket.io-client";

export class SocketService {

    static instance;

    constructor(host, port) {
        if (SocketService.instance) {
            return SocketService.instance;
        }

        this.host = host;
        this.port = port;
    }

    start = () => {
        this.socket = openSocket(`${this.host}:${this.port}`, {
            reconnection: true,
            reconnectionDelay: 1000,
            reconnectionDelayMax: 5000,
            reconnectionAttempts: 99999
        });

        this.socket.on('telemetry', (event) => {
            console.log(event);
        });
    }
}