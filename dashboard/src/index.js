import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import {SocketService} from "./services/SocketService";

let socketService = new SocketService("http://localhost", 9092);
socketService.start(() => {
    console.log("Loading GUI");
    ReactDOM.render(<App/>, document.getElementById('root'));
});

registerServiceWorker();
