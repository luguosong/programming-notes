import React, {Component} from 'react';
import axios from "axios";
import "./Cinema.css"

class Cinema extends Component {

    state = {
        // 电影院列表
        cinemaList: [],
        cinemaListBack: []
    }

    constructor() {
        super();

        //通过axios库请求数据
        axios({
            url: "https://m.maizuo.com/gateway?cityId=320500&ticketFlag=1&k=2795643",
            method: "get",
            headers: {
                'X-Client-Info': '{"a":"3000","ch":"1002","v":"5.2.1","e":"1688604964156877975453697","bc":"320500"}',
                'X-Host': `mall.film-ticket.cinema.list`
            }
        }).then(res => {
            // 将请求到的数据存储到state中
            this.setState({
                cinemaList: res.data.data.cinemas,
                cinemaListBack: res.data.data.cinemas
            })
        })
    }

    render() {
        return (
            <div>
                <input onInput={this.handleInput}/>
                {
                    this.state.cinemaList.map(item => {
                        return <div key={item.cinemaId}>
                            <h2>{item.name}</h2>
                            <p>{item.address}</p>
                        </div>
                    })
                }
            </div>
        );
    }

    handleInput = (e) => {
        let filter = this.state.cinemaListBack.filter(item => {
            return item.name.toUpperCase().includes(e.target.value.toUpperCase()) || item.address.toUpperCase().includes(e.target.value.toUpperCase())
        });

        this.setState({
            cinemaList: filter
        })
    }
}

export default Cinema;
