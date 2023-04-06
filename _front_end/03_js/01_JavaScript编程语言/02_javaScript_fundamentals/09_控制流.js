// if条件语句
let year = 2023;
if (year < 2015) {
    console.log('Too early...');
} else if (year > 2015) {
    console.log('Too late');
} else {
    console.log('Exactly!');
}

//三元表达式
year < 2015 ? console.log('Too early...') : console.log('Too late');

//switch语句
let a = 4;
switch (a) {
    case 3:
        console.log('Too small');
        break;
    case 4:
        console.log('Exactly!');
        break;
    case 5:
        console.log('Too big');
        break;
    default:
        console.log("I don't know such values");
}
