import './../App.css'
import React, {Component} from 'react';
import DataTable from 'react-data-table-component';
import {GoCheck} from "react-icons/go";
import Button from './Button';
import styled from 'styled-components';
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

const MAX_BAR_CHART_NODES = 20;


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
            statisticsSuccessRatio: 0.0,
            statisticsTestsPassed: 0,
            statisticsTestsFailed: 0,
            statisticsTestsUnderMaxResponseTime: 0,
            statisticsTestsOverMaxResponseTime: 0,
            statisticsTestsNotMatchStatusCode: 0,
            filteredItems: [],
            filterText: ''
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

    updateMonitoredFlag(id, value) {
        this.setState(this.state.data.reduce((current, item) => {
            if (item.name === id) {
                item.monitored = value
            }
            current.push(item);
            return current;
        }, []));

        this.updateStatistics();
    }

    updateSMTest(id, value) {
        this.setState(this.state.data.reduce((current, item) => {
            if (item.name === id) {
                Object.assign(item, value)
                this.updateReportColorForRow(item)
            }
            current.push(item)
            return current;
        }, []));
        this.updateStatistics()
    }

    fetchData = () => {
        this.state.fetchPending = true;
        axios.get(`http://localhost:8080/find-all-tests`)
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
        this.updateStatistics()
        console.log(this.state.data)
    }

    postProcessData = (data) => {
        data.forEach(this.updateReportColorForRow)
    }

    updateReportColorForRow= (row) => {
        row.reports.forEach(function(report) {
            report.x = report.timestamp
            report.y = report.sumResponseTime

            if (!report.allStatusCodeMatching) {
                report.color  = "red";
            }
            else if (report.allResponseTimeUnderMax) {
                report.color  = "#64E424";
            }
            else {
                report.color = "darkOrange"
            }
        })
    }


    onClickRefresh = () => {
        this.fetchData();
    }

    updateStatistics = () => {
        this.updateStatisticsSuccessRatio()
        this.updateAggregatedStatistics()
    }

    updateStatisticsSuccessRatio = () => {
        let tests = this.state.data
        let total = 0.0;
        for (let i = 0; i < tests.length; i += 1) {
            if (tests[i].monitored) {
                total += tests[i].ratio24Hour
            } else {
                total += 100.0;
            }
        }
        let result = (total / tests.length).toFixed(2)
        this.setState({
            statisticsSuccessRatio: result
        })
    }

    updateAggregatedStatistics = () => {

        //fixme: Use reducer to simplify code here

        let tests = this.state.data
        let passed = 0
        let failed = 0
        let underMaxResponseTime = 0
        let overMaxResponseTime = 0
        let notMatchStatusCode = 0

        for (let i = 0; i < tests.length; i += 1) {
            if (tests[i].monitored) {
                passed = passed + tests[i].statisticsTestsPassed
                failed = failed + tests[i].statisticsTestsFailed
                underMaxResponseTime = underMaxResponseTime + tests[i].statisticsTestsUnderMaxResponseTime
                overMaxResponseTime = overMaxResponseTime + tests[i].statisticsTestsOverMaxResponseTime
                notMatchStatusCode = notMatchStatusCode + tests[i].statisticsTestsNotMatchStatusCode
            }
        }

        this.setState({
            statisticsTestsPassed: passed,
            statisticsTestsFailed: failed,
            statisticsTestsUnderMaxResponseTime: underMaxResponseTime,
            statisticsTestsOverMaxResponseTime: overMaxResponseTime,
            statisticsTestsNotMatchStatusCode: notMatchStatusCode
        })

    }

    onClearFilter = () => {
        if (this.state.filterText) {
            this.setState({filterText:""})
        }
    }

    getFilteredItems = () => {
        return this.state.data.filter(
            item => item.name.toLowerCase().includes(this.state.filterText.toLowerCase())
            || item.tags.toLowerCase().includes(this.state.filterText.toLowerCase())
            || item.type.toLowerCase().includes(this.state.filterText.toLowerCase())
        );
    }

    render() {

        return (
            <div>
                <Container fluid>
                    <Row>
                        <Col md={{span: 1, offset: 0}}>
                            <div className="container"><Kpi className="ml-2 label" label={"Success ratio:"}
                                                            value={this.state.statisticsSuccessRatio}></Kpi></div>
                        </Col>
                        <Col><Label labelName={"Passed"} className={"passed-label"}
                                    value={this.state.statisticsTestsPassed}/></Col>
                        <Col><Label labelName={"Failed status"} className={"failed-status-label"}
                                    value={this.state.statisticsTestsNotMatchStatusCode}/></Col>
                        <Col><Label labelName={"Failed time"} className={"failed-time-label"}
                                    value={this.state.statisticsTestsOverMaxResponseTime}/></Col>
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
                           columns={this.buildColumns()}
                           //data={this.state.data}
                           data = {this.getFilteredItems()}
                           progressPending={this.state.fetchPending}
                           progressComponent={<LinearIndeterminate/>}
                           highlightOnHover={true}
                           expandableRows
                           expandableRowsComponent={<ReportsGrid/>}
                           expandOnRowClicked
                           subHeader
                           subHeaderComponent={<FilterComponent onFilter={e => this.setState({filterText: e.target.value})}
                                                                onClear={this.onClearFilter}
                                                                filterText={this.state.filterText} />} />
            </div>
        );
    }

    buildColumns() {
        const CustomLastResultsChartColumn = this.buildLastResultsChartColumn();
        const columns = [
            {
                name: "Status",
                width: "60px",
                selector: 'status',
                sortable: true,
                cell: row => <StatusColumn row={row}/>
            },
            {
                name: 'Name',
                width: "250px",
                selector: 'name',
                sortable: true
            },
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
                width: "90px",
                selector: 'ratio24Hour',
                sortable: true,
                format: row => `${row.ratio24Hour.toFixed(2)}%`
            },
            {
                name: 'Avg Sum. Time',
                width: "100px",
                selector: 'averageResponseTime',
                sortable: true,
                cell: row => <AverageResponseTimeColumn row={row}/>
            },
            {
                name: 'Schedule',
                width: "100px",
                selector: 'scheduleTimeInSeconds',
                sortable: true,
                format: row => `${h.humanize(row.scheduleTimeInSeconds * 1000)}`
            },
            {
                name: '#Runs',
                width: "50px",
                selector: 'totalRuns',
                sortable: true
            },
            {
                name: 'Last Run Time',
                minWidth: "180px",
                selector: 'lastExecutedTime',
                sortable: true,
                cell: row => <LastExecutedTimeColumn row={row}/>
            },
            {
                name: 'Enable monitor',
                button: true,
                cell: row => <ToggleMonitoredButton row={row} smTestOverviewGrid={this}/>
            },
            {
                name: 'Enable schedule',
                button: true,
                cell: row => <ToggleSMTestButton row={row} smTestOverviewGrid={this}/>
            },
            {name: '', button: true, cell: (row) => <RunTestNowButton row={row} smTestOverviewGrid={this}/>},
        ]
        return columns;
    }

    buildLastResultsChartColumn() {
        const getYDomain = this.buildYDomain();
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
        return CustomLastResultsChartColumn;
    }

    buildYDomain() {
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
        return getYDomain;
    }
}

/*********************************************************
/* Stateless functional component and helper functions
/*********************************************************

/**
 * Embedded styles for the linear progress bar
 */
const useStyles = makeStyles(theme => ({
    root: {
        width: '100%',
        '& > * + *': {
            marginTop: theme.spacing(2),
        },
    },
}));

/**
 * Linear progress bar on the data grid component
 */
const LinearIndeterminate = () => {
    const classes = useStyles();
    return (
        <div className={classes.root}>
            <LinearProgress/>
        </div>
    );
};

const toggleSMTest = (row, smTestOverviewGrid) => {
    axios.post('http://localhost:8080/toggle-test',
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


const ToggleSMTestButton = ({row, smTestOverviewGrid}) => (
    <ToggleButton
        inactiveLabel={"OFF"}
        activeLabel={<GoCheck/>}
        value={row.active || false}
        onToggle={(value) => {
            toggleSMTest(row, smTestOverviewGrid)
        }}/>
);

const runTestNow = (row, smTestOverviewGrid) => {
    axios.post('http://localhost:8080/execute-test',
        null,
        {params: {testName: row.name}})
        .then(function (success) {
            let value = success.data
            smTestOverviewGrid.updateSMTest(row.name, value)
        })
        .catch(function (error) {
            alert(error)
        })
};

const RunTestNowButton = ({row, smTestOverviewGrid}) => (
    <button type="button" title="Run test now" onClick={() => runTestNow(row, smTestOverviewGrid)} ><IoIosPlay/></button>
);


const toggleMonitored = (row, smTestOverviewGrid) => {
    axios.post('http://localhost:8080/toggle-monitored',
        null,
        {params: {testName: row.name}})
        .then(function (success) {
            let value = success.data
            smTestOverviewGrid.updateMonitoredFlag(row.name, value)
        })
        .catch(function (error) {
            alert(error)
        })
}

const ToggleMonitoredButton = ({row, smTestOverviewGrid}) => (
    <ToggleButton
        inactiveLabel={"OFF"}
        activeLabel={<GoCheck/>}
        value={row.monitored || false}
        onToggle={(value) => {
            toggleMonitored(row, smTestOverviewGrid)
        }}/>
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

const TextField = styled.input`
  height: 32px;
  width: 200px;
  border-radius: 3px;
  border-top-left-radius: 5px;
  border-bottom-left-radius: 5px;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border: 1px solid #e5e5e5;
  padding: 0 32px 0 16px;

  &:hover {
    cursor: pointer;
  }
`;

const ClearButton = styled(Button)`
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
  height: 34px;
  width: 32px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const FilterComponent = ({ filterText, onFilter, onClear }) => (
    <>
        <TextField id="search" type="text" placeholder="Filter By Name" value={filterText} onChange={onFilter} />
        <ClearButton onClick={onClear}>X</ClearButton>
    </>
);

export default SMTestOverviewGrid;