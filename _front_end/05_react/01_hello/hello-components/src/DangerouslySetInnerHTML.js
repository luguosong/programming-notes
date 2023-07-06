import React, {Component} from 'react';

class DangerouslySetInnerHtml extends Component {
    state={
        richText:"<b>hello,react</b>"
    }

    render() {
        return (
            <div>
                <h1>富文本展示</h1>
                <span dangerouslySetInnerHTML={
                    {
                        __html:this.state.richText
                    }
                }>
                </span>
            </div>
        );
    }
}

export default DangerouslySetInnerHtml;
