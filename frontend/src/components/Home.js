import React, { Component } from 'react';
import AppNavBar from './AppNavBar';
import SMTestOverviewGrid from "./SMTestOverviewGrid";
import { Container } from 'reactstrap';


class Home extends Component {
    render() {
        return (
            <div>
                <AppNavBar/>
                <Container fluid>
                    <SMTestOverviewGrid/>
                </Container>
            </div>
    );

    }
}

export default Home;