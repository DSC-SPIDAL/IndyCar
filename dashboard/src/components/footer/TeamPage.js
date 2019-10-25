import React from "react";
import "./TeamPage.css";
import TEAM_IMG from "./img/Group_Luddy.jpg";

export default class TeamPage extends React.Component {
    render() {
        return (
            <div className="team-page">
                <div className="image-wrapper">
                    <img src={TEAM_IMG} width="100%"/>
                    <div className="title-wrapper">
                        <h2>About the Team</h2>
                        <hr className="red-line"/>
                    </div>
                </div>
                <div className="team-content">
                    <p>
                        Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been
                        the
                        industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of
                        type
                        and scrambled it to make a type specimen book. It has survived not only five centuries, but also
                        the
                        leap into electronic typesetting, remaining essentially unchanged. It was popularised in the
                        1960s
                        with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with
                        desktop
                        publishing software like Aldus PageMaker including versions of Lorem Ipsum.
                    </p>
                    <h4>Faculty and associates</h4>
                    <hr className="red-line"/>
                    <div className="team-profile-container">
                        <TeamMember img="xqiu.jpg" name="Judy Fox"
                                    designation="Associate Professor & Associate Director"/>
                        <TeamMember img="bo.jpg" name="Bo Peng"
                                    designation="Visiting Research Faculty"/>
                        {/*<TeamMember img="kannan.jpg" name="Kannan Govindarajan"*/}
                        {/*            designation="Postdoctoral Scholar"/>*/}
                    </div>

                    <h4>PHD and graduate researchers</h4>
                    <hr className="red-line"/>
                    <div className="team-profile-container">
                        <TeamMember img="slo.jpeg" name="Selahattin Akkas"
                                    designation="(PHD)"/>
                        <TeamMember img="jiyau.jpg" name="Jiyau Li"
                                    designation="(PHD)"/>
                        <TeamMember img="miao.jpg" name="Jiyang Miao"
                                    designation="(PHD)"/>
                        <TeamMember img="chathura.jpg" name="Chathura Widanage"
                                    designation="Software Engineer"/>
                        <TeamMember img="fugang.jpg" name="Fugang Wang"
                                    designation="Software Research Engineer"/>
                        <TeamMember img="ram.JPG" name="Dinesh Ram"
                                    designation="Credentials"/>
                        <TeamMember img="profile.png" name="Sahaj Singh"
                                    designation="Credentials"/>
                        <TeamMember img="profile.png" name="Sumeet Mishra"
                                    designation="Credentials"/>
                    </div>

                    <h4>Undergraduate researchers</h4>
                    <hr className="red-line"/>
                    <div className="team-profile-container">
                        <TeamMember img="tajuan.jpg" name="TaJuan Beckworth"
                                    designation="Undergraduate"/>
                        <TeamMember img="laandrea.JPG" name="LaAndrea Cates"
                                    designation="Undergraduate"/>
                        <TeamMember img="taeyonn.jpg" name="Taeyonn Renolds"
                                    designation="Undergraduate"/>
                        <TeamMember img="kyanie.JPG" name="Kyanie Waters"
                                    designation="Undergraduate"/>
                    </div>
                </div>
            </div>
        )
    }
}

class TeamMember extends React.Component {
    render() {
        return (
            <div className="team-member">
                <div style={{backgroundImage: "url(img/team/" + this.props.img + ")"}} className="team-member-img">
                </div>
                <div className="team-member-info">
                    <h6>{this.props.name}</h6>
                    <p>
                        {this.props.designation}
                    </p>
                </div>
            </div>
        );
    }
}