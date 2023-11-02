//{% raw %}
import React, {useState} from 'react';

function FCom(props) {
    const [answer, setAnswer] = useState('');

    return (
        <div>
            <form>
                <textarea onChange={(e)=>{
                    setAnswer(e.target.value)
                }}/>
                <br/>
                <button disabled={answer.length===0}>提交</button>
            </form>
        </div>
    );
}

export default FCom;
//{% endraw %}
