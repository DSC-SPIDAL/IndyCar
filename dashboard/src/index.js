import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import {createStore} from 'redux';
import {Provider} from 'react-redux';
import registerServiceWorker from './registerServiceWorker';
import {SocketService} from "./services/SocketService";
import RootReducer from "./reducers/RootReducer";

const IP = "192.168.49.2"
const PORT = 30425

let store = createStore(RootReducer, window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__());

let socketService = new SocketService(IP, PORT, store);
//let socketService = new SocketService("localhost", 8080, store);
//let socketService = new SocketService("j-093.juliet.futuresystems.org", 5000, store);
// let socketService = new SocketService("localhost", 5000, store);
// let socketService = new SocketService("149.165.150.51", 31623, store);
//let socketService = new SocketService("149.165.150.51", 30190, store);
socketService.start(() => {
    console.log("Loading GUI...");
    ReactDOM.render(<Provider store={store}><App/></Provider>, document.getElementById('root'));

    let scroll = window.localStorage.getItem("scroll");
    if (!isNaN(scroll)) {
        window.scrollTo(0, parseInt(scroll, 10));
    }
});

window.onfocus = () => {
    window.location.reload();
};

window.onblur = () => {
    window.localStorage.setItem("scroll", window.scrollY);
};

let refreshTimer = -1;

window.onscroll = () => {
    clearInterval(refreshTimer);
    refreshTimer = setInterval(() => {
        window.location.reload(true);
    }, 1000 * 60 * 10);
};

registerServiceWorker();
