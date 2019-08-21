import React from "react";
import {connect} from "react-redux";

class StartComponent extends React.Component {
    render() {
        return (
            <div>
                Start
            </div>
        )
    }
}

const start = connect(state => state)(StartComponent);

export default start;