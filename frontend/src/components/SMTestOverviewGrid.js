import './../App.css'
import React, {Component} from 'react';
import DataTable from 'react-data-table-component';
import {GoCheck} from "react-icons/go";
import {
    IoIosCheckmarkCircle,
    IoIosCloseCircle,
    IoIosPlay,
    IoIosRefreshCircle,
    IoMdCloudy,
    IoMdCog
} from "react-icons/io";
import {makeStyles} from '@material-ui/core/styles';
import LinearProgress from '@material-ui/core/LinearProgress';
import axios from 'axios';
import _ from 'lodash';
import {Col, Container, Row} from 'reactstrap';

import {VerticalBarSeries, XYPlot,} from 'react-vis'
import ReportsGrid from "./ReportsGrid";
import Moment from "react-moment";
import ToggleButton from 'react-toggle-button'
import Kpi from './Kpi'
import Humanizer from 'humanize-duration-es6';

const shortEnLocale = {
    h: () => "hour",
    m: () => "min",
    s: () => "sec",
    ms: () => "millis",
};

const h = new Humanizer(shortEnLocale);

const MAX_BAR_CHART_NODES = 20

const useStyles = makeStyles(theme => ({
    root: {
        width: '100%',
        '& > * + *': {
            marginTop: theme.spacing(2),
        },
    },
}));

const LinearIndeterminate = () => {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <LinearProgress/>
        </div>
    );
};

const enableDisableSMTest = (row, smTestOverviewGrid) => {
    axios.post('http://localhost:8080/toggle_synthetic_test',
        null,
        {params: {testName: row.name}})
        .then(function (success) {
            let value = success.data
            smTestOverviewGrid.updateActiveFlag(row.name, value)
        })
        .catch(function (error) {
            alert(error)
        })
}


const ToggleEnableButton = ({row, smTestOverviewGrid}) => (
    <ToggleButton
        inactiveLabel={"OFF"}
        activeLabel={<GoCheck/>}
        value={row.active || false}
        onToggle={(value) => {
            enableDisableSMTest(row, smTestOverviewGrid)
        }}/>
);

const runNow = (row, smTestOverviewGrid) => {
        axios.post('http://localhost:8080/execute_synthetic_test',
            null,
            {params: {testName: row.name}})
            .then(function (success) {
                let value = success.data
                smTestOverviewGrid.updateRow(row.name, value)
            })
            .catch(function (error) {
                alert(error)
            })
    }
;

const RunButton = ({row, smTestOverviewGrid}) => (
    <button type="button" title="Run test now" onClick={() => runNow(row, smTestOverviewGrid)} ><IoIosPlay/></button>
);

const LastExecutedTimeColumn = ({row}) => (
    <div>
        <Moment fromNow>{row.timeLastExecuted}</Moment>
    </div>
);

const StatusColumn = ({row}) => (
    <div>
        {(() => {
            if (row.status = "passed") {
                return (
                    <div><h3><i className="green"><IoIosCheckmarkCircle/></i></h3></div>
                )
            } else {
                return (
                    <div><h3><i className="red"><IoIosCloseCircle/></i></h3></div>
                )
            }
        })()}
    </div>
);


const TypeColumn = ({row}) => (
    <div>
        {(() => {
            if (row.type == "API") {
                return (<div><h6><i style={{color: 'blue'}}><IoMdCloudy/></i> {row.type}</h6></div>)
            } else if (row.type == "Transactions") {
                return (<div><h6><i style={{color: 'darkGrey'}}><IoMdCog/></i> TX</h6></div>)
            }
        })()}
    </div>
);


const AverageResponseTimeColumn = ({row}) => (
    <div>{h.humanize(row.averageResponseTime.toFixed(0),{ units: ['h','s','ms'] })}</div>
);


const Label = ({labelName, className, value}) =>
    <div className="container"><span className={className}>{labelName}:</span> {value}</div>


/**
 *
 */
class SMTestOverviewGrid extends Component {

    constructor() {
        super()
        this.state = {
            data: [],
            selectedRow: null,
            fetchPending: false,
            autoRefresh: true,
            kpiAverage: 0.0,
            totalTestsPassed: 0,
            totalTestsFailed: 0,
            totalTestsOptimalResponseTime: 0,
            totalTestsUnderMaxResponseTime: 0,
            totalTestsOverMaxResponseTime: 0,
            totalTestsNotMatchStatusCode: 0
        }
    }

    componentDidMount() {
        console.log("test");
        this.intervalId = setInterval(this.timer.bind(this), 60000);
        this.fetchData();
    }

    componentWillUnmount() {
        clearInterval(this.intervalId);
    }

    timer() {
        if (this.state.autoRefresh) {
            this.fetchData()
        }
    }

    updateActiveFlag(id, value) {
        this.setState(this.state.data.reduce((current, item) => {
            if (item.name === id) {
                item.active = value
            }
            current.push(item);
            return current;
        }, []));
    }

    updateRow(id, value) {
        this.setState(this.state.data.reduce((current, item) => {
            if (item.name === id) {
                Object.assign(item, value)
                this.updateReportColorForRow(item)
            }
            current.push(item)
            return current;
        }, []));
        this.countKpis()
    }

    fetchData = () => {
        this.state.fetchPending = true;
        axios.get(`http://localhost:8080/get_synthetic_tests`)
            .then(this.onFetchDataSuccess)
            .catch(function (error) {
                console.log("Ooops:" + error)
            })
    }

    onFetchDataSuccess = (response) => {
        var data = response.data
        this.postProcessData(data)
        this.setState({
            data: data,
            fetchPending: false
        });
        this.countKpis()
        console.log(this.state.data)
    }

    postProcessData = (data) => {
        data.forEach(this.updateReportColorForRow)
    }

    updateReportColorForRow= (row) => {
        row.reports.forEach(function(report) {

            report.x = report.timestamp
            report.y = report.sumResponseTime

            if (report.allResponseTimeOptimal)  {
                report.color  = "#64E424";
            }
            else if (report.allResponseTimeUnderMax) {
                report.color = "#BEE424";
            }
            else {
                report.color = "red"
            }
        })
    }


    onClickRefresh = () => {
        this.fetchData();
    }

    countKpis = () => {
        this.countAverageRatio24hourKpi()
        this.countOtherKpis()
    }

    countAverageRatio24hourKpi = () => {
        let objects = this.state.data
        let total = 0.0;
        for (let i = 0; i < objects.length; i += 1) {
            total += objects[i].ratio24Hour
        }
        let result = (total / objects.length).toFixed(2)
        this.setState({
            kpiAverage: result
        })
    }

    countOtherKpis = () => {
        let objects = this.state.data
        let totalPassed = 0
        let totalFailed = 0
        let totalTestsOptimalResponseTime = 0
        let totalTestsUnderMaxResponseTime = 0
        let totalTestsOverMaxResponseTime = 0
        let totalTestsNotMatchStatusCode = 0
        for (let i = 0; i < objects.length; i += 1) {
            totalPassed = totalPassed + objects[i].totalTestsPassed
            totalFailed = totalFailed + objects[i].totalTestsFailed
            totalTestsOptimalResponseTime = totalTestsOptimalResponseTime + objects[i].totalTestsOptimalResponseTime
            totalTestsUnderMaxResponseTime = totalTestsUnderMaxResponseTime + objects[i].totalTestsUnderMaxResponseTime
            totalTestsOverMaxResponseTime = totalTestsOverMaxResponseTime + objects[i].totalTestsOverMaxResponseTime
            totalTestsNotMatchStatusCode = totalTestsNotMatchStatusCode + objects[i].totalTestsNotMatchStatusCode
        }
        this.setState({
            totalTestsPassed: totalPassed,
            totalTestsFailed: totalFailed,
            totalTestsOptimalResponseTime: totalTestsOptimalResponseTime,
            totalTestsUnderMaxResponseTime: totalTestsUnderMaxResponseTime,
            totalTestsOverMaxResponseTime: totalTestsOverMaxResponseTime,
            totalTestsNotMatchStatusCode: totalTestsNotMatchStatusCode
        })

    }

    render() {

        const getYDomain = (data) => {
            const yDomain = data.reduce(
                (res, row) => {
                    return {
                        max: Math.max(res.max, row.y),
                        min: Math.min(res.min, row.x)
                    };
                },
                {max: -Infinity, min: Infinity}
            );
        }


        const CustomLastResultsChartColumn = ({row}) => (
            <div>
                <XYPlot
                    width={120}
                    height={120}
                    xType="ordinal"
                    yDomain={getYDomain(row)}>
                    <VerticalBarSeries data={row} colorType="literal"/>
                </XYPlot>
            </div>
        );


        const columns = [
            {   name: "Status",
                width: "60px",
                selector: 'status',
                sortable: true,
                cell: row => <StatusColumn row={row}/>},
            {   name: 'Name',
                width: "300px",
                selector: 'name',
                sortable: true},
            {
                name: 'Type',
                width: "100px",
                selector: 'type',
                sortable: true,
                cell: row => <TypeColumn row={row}/>
            },
            {
                name: 'Tags',
                width: "100px",
                selector: 'tags',
                sortable: true,
            },
            {
                name: 'Last Results',
                minWidth: "150px",
                selector: 'lastResults',
                cell: row => <CustomLastResultsChartColumn row={_.reverse(_.take(row.reports, MAX_BAR_CHART_NODES))}/>
            },
            {
                name: '24H ratio',
                minWidth: "50px",
                selector: 'ratio24Hour',
                sortable: true,
                format: row => `${row.ratio24Hour.toFixed(2)}%`
            },
            {
                name: 'Avg Sum. Time',
                width: "200px",
                selector: 'averageResponseTime',
                sortable: true,
                cell: row => <AverageResponseTimeColumn row={row}/>
            },
            {   name: 'Schedule',
                width: "150px",
                selector: 'scheduleTimeInSeconds',
                sortable: true,
                format: row => `${h.humanize(row.scheduleTimeInSeconds*1000)}`
            },
            {   name: '#Runs',
                width: "50px",
                selector: 'totalRuns',
                sortable: true},
            {
                name: 'Last Run Time',
                minWidth: "150px",
                selector: 'lastExecutedTime',
                sortable: true,
                cell: row => <LastExecutedTimeColumn row={row}/>
            },
            {name: 'Enable', button: true, cell: row => <ToggleEnableButton row={row} smTestOverviewGrid={this}/>},
            {name: '', button: true, cell: (row) => <RunButton row={row} smTestOverviewGrid={this}/>},
        ]


        return (
            <div>
                <Container fluid>
                    <Row>
                        <Col md={{span: 1, offset: 0}}>
                            <div className="container"><Kpi className="ml-2 label" label={"Success ratio:"}
                                                            value={this.state.kpiAverage}></Kpi></div>
                        </Col>
                        <Col><Label labelName={"Passed"} className={"passed-label"}
                                    value={this.state.totalTestsPassed}/></Col>
                        <Col><Label labelName={"Status NOK"} className={"failed-label"}
                                    value={this.state.totalTestsNotMatchStatusCode}/></Col>
                        <Col><Label labelName={"Fast time"} className={"passed-label"}
                                    value={this.state.totalTestsOptimalResponseTime}/></Col>
                        <Col><Label labelName={"OK time"} className={"warning-label"}
                                    value={this.state.totalTestsUnderMaxResponseTime}/></Col>
                        <Col><Label labelName={"Slow time"} className={"failed-label"}
                                    value={this.state.totalTestsOverMaxResponseTime}/></Col>
                        <Col md={{span: 1, offset: 0}}>
                            <div className="container">
                                <div className="item">Auto-refresh every minute</div>
                                <div className="item">
                                    <ToggleButton
                                        inactiveLabel={"OFF"}
                                        activeLabel={<GoCheck/>}
                                        value={this.state.autoRefresh || false}
                                        onToggle={(value) => {
                                            this.setState({
                                                autoRefresh: !value,
                                            })
                                        }}/>
                                </div>
                                <div className="item">
                                    <button onClick={this.onClickRefresh} title="Refresh manually"><IoIosRefreshCircle/></button>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </Container>
                <DataTable title={"Test monitoring over 24 hours"}
                           stroke={"black"}
                           columns={columns}
                           data={this.state.data}
                           progressPending={this.state.fetchPending}
                           progressComponent={<LinearIndeterminate/>}
                           highlightOnHover={true}
                           expandableRows
                           expandableRowsComponent={<ReportsGrid/>}
                           expandOnRowClicked/>
            </div>
        );
    }
}

export default SMTestOverviewGrid;