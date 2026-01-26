import {makeAutoObservable} from "mobx";

class Timer {
    count = 0

    constructor() {
        makeAutoObservable(this)
    }

    increase() {
        this.count += 1
    }

    reset() {
        this.count = 0
    }
}

// 到处Timer类对象
export const myTimer = new Timer();
