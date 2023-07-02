import React, {Component} from 'react';

class Ref extends Component {

    myInput=React.createRef();
    render() {
        return (
            <div>
                <h1>Ref</h1>
                <input ref={this.myInput}/>
                <button onClick={this.handleClick}>测试</button>
            </div>
        );
    }

    handleClick = () => {
        alert(this.myInput.current.value);
    }
}

export default Ref;
