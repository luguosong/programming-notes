import React, {Component} from 'react';
import Swiper from "swiper";
import "swiper/css";
import 'swiper/css/pagination';

import {Pagination} from "swiper/modules";

Swiper.use([Pagination])

class SwiperExample extends Component {


    render() {
        return (
            <div>
                <h1>轮播示例</h1>
                <MySwiper>
                    <MySwiperItem>1111</MySwiperItem>
                    <MySwiperItem>2222</MySwiperItem>
                    <MySwiperItem>3333</MySwiperItem>
                </MySwiper>
            </div>
        );
    }
}

class MySwiper extends Component {

    render() {
        return (
            <div>
                <div className="swiper" style={{height: "200px", background: "#aaaaaa"}}>
                    <div className="swiper-wrapper">
                        {this.props.children}
                    </div>

                    <div className="swiper-pagination"></div>
                </div>
            </div>
        );
    }

    componentDidMount() {
        new Swiper(".swiper", {
            // 如果需要分页器
            pagination: {
                el: '.swiper-pagination',
            },
        })
    }
}

class MySwiperItem extends Component {
    render() {
        return (
            <div className="swiper-slide">
                {this.props.children}
            </div>
        );
    }
}

export default SwiperExample;
