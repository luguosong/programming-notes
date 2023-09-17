import React, {Component} from 'react';
import {Button} from "antd";

class CCom extends Component {

    a = 100

    render() {
        return (
            <div>
                {/*❌*/}
                <Button onClick={function () {
                    alert("普通函数中是谁调用this就指向谁,React中采用了事件代理,所以this指向undefined")
                }}>属性中定义普通函数(❌this为undefined)</Button>

                {/*💀这里不要加括号，否则会直接执行函数得到函数执行结果*/}
                {/*❌*/}
                <Button onClick={this.clickHandler1}>直接调用普通函数(❌this为undefined)</Button>

                {/*✅解决普通函数中的this指向问题*/}
                <Button onClick={this.clickHandler1.bind(this)}>使用bind将普通函数中的this指向CCom</Button>

                <hr/>

                {/*✅*/}
                <Button onClick={this.clickHandler2}>直接调用箭头函数</Button>

                {/*✅*/}
                <Button onClick={() => {
                    alert("当使用箭头函数时，this 指向箭头函数被定义的地方，" +
                        "也就是 FCom 类，因此可以在箭头函数中访问到 a。this.a=" + this.a)
                }}>属性中定义箭头函数</Button>

                {/*✅*/}
                <Button onClick={() => {
                    this.clickHandler1()
                    this.clickHandler2()
                }}>属性箭头函数中可以调用任意函数,因为当中的this指向CCom</Button>


            </div>
        );
    }

    clickHandler1() {
        alert("普通函数中是谁调用this就指向谁,React中采用了事件代理,所以this指向undefined,this=" + this)
    }

    clickHandler2 = () => {
        alert("箭头函数中this指向定义时的this,也就是CCom类,因此可以访问到a,a=" + this.a)
    }


}

export default CCom;
