import React, {Component} from 'react';

// 列表渲染
class ListRender extends Component {
    state = {
        list: ["111", "222", "333"]
    }

    render() {
        return (
            <div>
                <h1>列表渲染</h1>
                <ul>
                    {
                        this.state.list.map(
                            (item, index) => <li key={index}>{item}</li>
                        )
                    }
                </ul>
            </div>
        );
    }
}

export default ListRender;
