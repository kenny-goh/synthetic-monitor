import React, { Component } from 'react';
import { Collapse, Nav, Navbar, NavbarBrand, NavbarToggler, NavItem, NavLink } from 'reactstrap';

export default class AppNavbar extends Component {
    constructor(props) {
        super(props);
        this.state = {isOpen: false};
        this.toggle = this.toggle.bind(this);
    }

    toggle() {
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {
        return <Navbar color="dark" dark expand="md">
            <NavbarBrand>Synthetic Test Runner v0.1</NavbarBrand>
            {/*<NavbarToggler onClick={this.toggle}/>*/}
            {/*<Collapse isOpen={this.state.isOpen} navbar>*/}
            {/*    <Nav className="ml-auto" navbar>*/}
            {/*        <NavItem>*/}
            {/*            <NavLink*/}
            {/*                href="">@KennyGoh</NavLink>*/}
            {/*        </NavItem>*/}
            {/*        <NavItem>*/}
            {/*            <NavLink href="">GitHub</NavLink>*/}
            {/*        </NavItem>*/}
            {/*    </Nav>*/}
            {/*</Collapse>*/}
        </Navbar>;
    }
}