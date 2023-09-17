import React, {useRef} from 'react';
import Compact from "antd/es/space/Compact";
import {Button, Input} from "antd";

function FCom(props) {

    const myRef = useRef();

    return (
        <div>
            <Compact>
                <Input ref={myRef}/>
                <Button onClick={() => {
                    alert(myRef.current.input.value)
                }}>获取input内容
                </Button>
            </Compact>
        </div>
    );
}

export default FCom;
