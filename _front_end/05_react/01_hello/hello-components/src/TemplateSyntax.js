import React, {Component} from 'react';
import './TemplateSyntax.css'

class TemplateSyntax extends Component {
    render() {
        const style1 = {
            color: 'red',
            fontSize: '30px'
        };
        return (
            <div>
                <h1>模板语法</h1>
                <div>10 + 20 = {10 + 20}</div>
                <div style={style1}>样式设置(React推荐这种将css写在内部的做法)</div>
                <div className="active">引入外部css样式</div>
            </div>
        );
    }
}

export default TemplateSyntax;
