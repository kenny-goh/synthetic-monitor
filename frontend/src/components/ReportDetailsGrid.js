import './../App.css'
import React, {Component} from 'react';
import DataTable, {createTheme} from 'react-data-table-component';
import { GoCheck, GoX } from "react-icons/go";
import {  IoMdCloudy, IoMdCog } from "react-icons/io";


createTheme('darkGrey', {
    background: {
        default: '#A9A9A9',
    },
});


const StatusColumn = ({ row }) => (
    <div>
        {(() => {
            if (row.statusSuccess) {
                return (
                    <div><h6><i className="light-green"><GoCheck/></i></h6></div>
                )
            } else {
                return (
                    <div><h5><i className="light-red"><GoX/></i></h5></div>
                )
            }
        })()}
    </div>
);

const StatusCodeMatchingColumn = ({ row }) => (
    <div>
        {(() => {
            if (row.statusCodeMatching) {
                return (
                    <div><i className="light-green">{row.status}</i></div>
                )
            } else {
                return (
                    <div><i className="light-red">{row.status}</i></div>
                )
            }
        })()}
    </div>
);



const TypeColumn = ({row}) => (
    <div>
        {(() => {
            if (row.type == "API") {
                return (<div><i style={{color:'white'}}><IoMdCloudy/></i>  {row.type}</div>)
            }
        })()}
    </div>
);

const MaxResponseTimeColumn = ({ row }) => (
    <div>
        {(() => {
            if (row.responseTime <= row.maximumResponseThreshold) {
                return (
                    <div><i className="light-green">{row.responseTime} {'ms <'} {row.maximumResponseThreshold} ms<GoCheck/></i></div>
                )
            } else {
                return (
                    <div><i className="light-red">{row.responseTime} {'ms >'} {row.maximumResponseThreshold} ms<GoX/></i></div>
                )
            }
        })()}
    </div>
)

const OptimalResponseTimeColumn = ({ row }) => (
    <div>
        {(() => {
            if (row.responseTime <= row.optimalResponseThreshold) {
                return (
                    <div><i className="light-green">{row.responseTime} {'ms <'} {row.optimalResponseThreshold} ms<GoCheck/></i></div>
                )
            } else {
                return (
                    <div><i className="warning">{row.responseTime} {'ms >'} {row.optimalResponseThreshold} ms<GoX/></i></div>
                )
            }
        })()}
    </div>
)

class ReportDetailsGrid extends Component {

    constructor(props) {
        console.log('ReportsGrid Constructor')
        super(props)
        this.state = {
            data: props.data.transactionReports
        }
    }

    render() {
        const columns = [
            {   name: 'Name',
                width: "250px",
                selector: 'name',
                sortable: true },
            {   name: 'Type',
                width: "80px",
                selector: 'type',
                sortable: true,
                cell: row=><TypeColumn row={row} /> },
            {   name: 'Details',
                width: "600px",
                selector: 'details',
                sortable: true },
            {   name: "Status",
                width: "70px",
                selector: 'status',
                sortable: true,
                cell: row=><StatusColumn row={row} />},
            {   name: "Status code",
                width: "100px",
                selector: 'statusCodeIsMatching',
                sortable: true,
                cell: row=><StatusCodeMatchingColumn row={row} />},
            {   name: "Optimal Response",
                width: "200px",
                selector: 'response',
                sortable: true, cell: row=><OptimalResponseTimeColumn row={row} />},
            {   name: "Max Response",
                width: "200px",
                selector: 'response',
                sortable: true,
                cell: row=><MaxResponseTimeColumn row={row} />},
        ]
        return (
            <div>
                <DataTable
                    title="Activity Details"
                    theme={'dark'}
                    columns={columns}
                    data={this.state.data}
                    highlightOnHover={true}
                />
            </div>
        );
    }

}

export default ReportDetailsGrid;