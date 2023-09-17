import React, {Component} from 'react';

import style from './CssModuleCCom.module.css'

class CssModuleCCom extends Component {
    render() {
        return (
            <div>
                <div className={style.aqua}>CSS Module防止css污染</div>
            </div>
        );
    }
}

export default CssModuleCCom;
