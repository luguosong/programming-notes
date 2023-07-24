import UseStateExample from "./UseStateExample";
import UseEffectExample from "./UseEffectExample";
import UseCallbackExample from "./UseCallbackExample";
import UseMemoExample from "./UseMemoExample";
import UseRefExample from "./UseRefExample";
import UseContextExample from "./UseContextExample";
import UseReducerExample from "./UseReducerExample";
import UseReducerAndUseContextExample from "./UseReducerAndUseContextExample";

function App() {
    return (
        <div>
            {/*useState-状态*/}
            <UseStateExample/>
            {/*useEffect-副作用*/}
            <UseEffectExample/>
            {/*useCallback-记忆函数*/}
            <UseCallbackExample/>
            {/*计算属性*/}
            <UseMemoExample/>
            {/*引用*/}
            <UseRefExample/>
            {/*userContext*/}
            <UseContextExample/>
            {/*外部状态*/}
            <UseReducerExample/>
            {/*UseReducer和UseContext配合使用*/}
            <UseReducerAndUseContextExample/>
        </div>
    );
}

export default App;
