import React from "react";
import "./navbar.scss";
import logo from "./logo3.jpeg";
import {
  Link
} from "react-router-dom";

class NavBar extends React.Component {
  createButton(text, route) {
    return (
      <Link to={route}>
        <div className="option-item">
          <a className="option-anchor" href="#">
            <span className="option-text">{text}</span>
          </a>
        </div>
      </Link>
    );
  }

  render() {
    return (
      <div className="navbar">
        <div className="navbar-wrapper">
          <div className="logo-wrapper">
            <a className="logo-link" href="#">
              <img src={logo} alt="" className="logo-img" />
            </a>
          </div>
          <nav className="optins-wrapper">
            {this.createButton("Home", "/")}
            {this.createButton("Compra", "/buy")}
            {this.createButton("Venda", "/sell")}
          </nav>
        </div>
      </div>
    );
  }
}

export default NavBar;
