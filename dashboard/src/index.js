import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import {SocketService} from "./services/SocketService";

//let socketService = new SocketService("j-093.juliet.futuresystems.org", 5000);
let socketService = new SocketService("localhost", 5000);
socketService.start(() => {
    console.log("Loading GUI");
    ReactDOM.render(<App/>, document.getElementById('root'));

    let scroll = window.localStorage.getItem("scroll");
    if (!isNaN(scroll)) {
        window.scrollTo(0, parseInt(scroll));
    }
});

window.onfocus = () => {
    window.location.reload(true);
};

window.onblur = () => {
    window.localStorage.setItem("scroll", window.scrollY);
};

registerServiceWorker();
