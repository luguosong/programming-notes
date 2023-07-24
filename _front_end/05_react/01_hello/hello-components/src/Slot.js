import React, {Component} from 'react';

/*
* 插槽
* */
class Slot extends Component {
    render() {
        return (
            <div>
                <h1>插槽</h1>
                <Child1>hello solt</Child1>
                <Child2>
                    <div>solt1</div>
                    <div>solt2</div>
                    <div>solt3</div>
                </Child2>
            </div>
        );
    }
}

class Child1 extends Component {
    render() {
        return (
            <div>
                {this.props.children}
            </div>
        );
    }
}

class Child2 extends Component {
    render() {
        return (
            <div>
                {/*
                通过角标获取插槽内容
                */}
                {this.props.children[0]}
                {this.props.children[1]}
                {this.props.children[2]}
            </div>
        );
    }
}

export default Slot;
