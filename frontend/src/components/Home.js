import React, { Component } from 'react';
import AppNavbar from './app-navbar';
import SyntheticTestGrid from "./SyntheticTestGrid";
import { Container } from 'reactstrap';

class Home extends Component {
    render() {
        return (
            <div>
                <AppNavbar/>
                <Container fluid>
                    <SyntheticTestGrid/>
                </Container>
            </div>
    );

    }
}

export default Home;