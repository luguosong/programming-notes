import React, {Component} from 'react';
import PropTypesCheck from 'prop-types';

/*
* 父组件
* */
class Father extends Component {
    render() {
        return (
            <div>
                <h1>属性</h1>
                <Child/>
                <Child name="张三"/>
                {/*向子组件传递布尔值*/}
                <Child name="李四" disabled={true}/>

                {/*函数式组件传递属性*/}
                <Child2 name="王五"/>
            </div>
        );
    }
}


/*
* 子组件
* */
class Child extends Component {

    /*
    * 子主键属性验证方式一
    * */
    static propTypes = {
        disabled: PropTypesCheck.bool
    }

    /*
    * 属性默认值
    * */
    static defaultProps = {
        name: "属性默认值"
    }

    render() {
        return (
            <div>
                {/*通过props获取父组件传来的属性*/}
                {this.props.name}
                {/*传递布尔值属性*/}
                <input type="button" value="点击" disabled={this.props.disabled}/>
            </div>
        );
    }
}

/*
* 子主键属性验证方式二
* */
Child.propTypes = {
    name: PropTypesCheck.string,
}

/*
* 函数式子组件属性接收
* 通过函数的参数props接收父组件传递的属性
* */
function Child2(props) {
    return (
        <div>
            {props.name}
        </div>
    )
}

export default Father;
