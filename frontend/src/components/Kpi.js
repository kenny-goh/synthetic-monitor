import React from 'react';

export default class Kpi extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            label: props.label,
            className : props.className,
            value : props.value,
            colorClassName: this.getColor()
        };
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            value: nextProps.value,
            colorClassName: this.getColor(nextProps.value)
        });
    }

    getColor=(value)=> {
        if (value >= 0 && value < 95) {
            return  'kpi-bad';
        }
        else if (value >= 95 && value < 99) {
            return 'kpi-warning';
        }
        return 'kpi-good';
    }

    render() {
        return ( <>
                    <span className={this.state.className}>{this.state.label} </span>
                    <span className={this.state.colorClassName}>[{this.state.value}%]</span>
                </> );
    }
}