interface Person {
    name: string;
    age: number;
    location?: string; //可选属性
}

const tom: Person = {
    name: 'Tom',
    age: 25
};
