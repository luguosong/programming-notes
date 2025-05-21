const initialState = {
    /*
    * 当前待办事项的实际列表
    *
    * text:用户输入的文本
    * completed:表示是否完成的布尔值
    * id:唯一的 ID 值
    * color:颜色类别（如果已选择）
    * */
    todos: [
        {id: 0, text: 'Learn React', completed: true},
        {id: 1, text: 'Learn Redux', completed: false, color: 'purple'},
        {id: 2, text: 'Build something fun!', completed: false, color: 'blue'},
    ],
    // 当前的过滤选项
    filters: {
        // “全部（All）”、“活动（Active）” 和 “已完成（Completed）”
        status: 'All',
        // ” Red “、“ Yellow ”、“ Green ”、“ Blue ”、“ Orange ”、“ Purple ”
        colors: [],
    },
}

// 使用 initialState 作为默认值
export default function appReducer(state = initialState, action) {
    // reducer 通常根据 action.type 字段来决定发生什么
    switch (action.type) {
        // 根据不同的 action 类型在这里做出响应
        case 'todos/todoAdded': {
            // 需要返回一个新的 state 对象
            return {
                // 具有所有现有 state 数据
                ...state,
                // 但有一个用于 `todos` 字段的新数组
                todos: [
                    // 所有旧待办事项
                    ...state.todos,
                    // 新的对象
                    {
                        // 在此示例中使用自动递增的数字 ID
                        id: nextTodoId(state.todos),
                        text: action.payload,
                        completed: false
                    }
                ]
            }
        }
        default:
            // 如果这个 reducer 不识别该 action 类型，或者不关心这个特定的 action，
            // 就返回原有的 state，不做任何改变
            return state
    }
}
