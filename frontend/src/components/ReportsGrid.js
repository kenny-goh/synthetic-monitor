import './../App.css'
import React, {Component} from 'react';
import DataTable,{ createTheme } from 'react-data-table-component';
import { GoCheck, GoX } from "react-icons/go";
import ReportDetailsGrid from "./ReportDetailsGrid";
import Moment from "react-moment";
import Humanizer from 'humanize-duration-es6';

const shortEnLocale = {
    h: () => "hour",
    m: () => "min",
    s: () => "sec",
    ms: () => "millis",
};

const h = new Humanizer(shortEnLocale);

createTheme('grey', {
    background: {
        default: '#F5F5F5',
    },
});

/**
 *
 * @param row
 * @returns {*}
 * @constructor
 */
const LastExecutedTimeColumn = ({row}) => (
    <div>
        <Moment fromNow>{row.timestamp}</Moment>
    </div>
);


const StatusColumn = ({ row }) => (
    <div>
        {(() => {
            if (row.passed) {
                return (
                    <div><h3><i className="green"><GoCheck/></i></h3></div>
                )
            } else {
                return (
                    <div><h3><i className="red"><GoX/></i></h3></div>
                )
            }
        })()}
    </div>
)


class ReportsGrid extends Component {

    constructor(props) {
        console.log('ReportsGrid Constructor')
        super(props)
        this.state = {
            data : props.data.reports
        }
    }

    render() {

        const columns = [
            {   name: "Status",
                selector: 'status',
                width: "70px",
                sortable: true,
                cell: row=><StatusColumn row={row} />},
            {   name: 'Last Executed Time',
                width: "200px",
                selector: 'lastExecutedTime',
                sortable: true,
                cell: row =><LastExecutedTimeColumn row={row} /> },
            {   name: '# Tests',
                width: "100px",
                selector: 'numberOfTests',
                sortable: true,
            },
            {   name: 'Sum. Time',
                width: "200px",
                selector: 'sumResponseTime',
                sortable: true,
                format: row => `${h.humanize(row.sumResponseTime, { units: ['h','s','ms'] })}`
            },
            {   name: 'Avg. Time',
                width: "200px",
                selector: 'averageResponseTime',
                sortable: true,
                format: row => `${h.humanize(row.averageResponseTime.toFixed(0), { units: ['h','s','ms'] })}`
            },
            {   name: 'Min. Time',
                width: "200px",
                selector: 'minResponseTime',
                sortable: true,
                format: row => `${h.humanize(row.minResponseTime, { units: ['h','s','ms'] })}`
            },
            {   name: 'Max. Time',
                selector: 'maxResponseTime',
                sortable: true,
                format: row => `${h.humanize(row.maxResponseTime, { units: ['h','s','ms'] })}`
            }
        ]

        return (
            <div>
                <DataTable
                    title="Activity"
                    theme={'grey'}
                    columns={columns}
                    data={this.state.data}
                    highlightOnHover={true}
                    expandableRows
                    expandableRowsComponent={<ReportDetailsGrid />}
                    expandOnRowClicked
                    pagination={true}
                    paginationPerPage={10}
                />
            </div>
        );
    }

}

export default ReportsGrid;