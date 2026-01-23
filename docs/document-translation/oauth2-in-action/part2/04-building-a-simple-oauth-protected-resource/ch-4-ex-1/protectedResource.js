var express = require("express");
var bodyParser = require('body-parser');
var cons = require('consolidate');
var nosql = require('nosql').load('database.nosql');
var __ = require('underscore');
var cors = require('cors');

var app = express();

app.use(bodyParser.urlencoded({extended: true})); // support form-encoded bodies (for bearer tokens)

app.engine('html', cons.underscore);
app.set('view engine', 'html');
app.set('views', 'files/protectedResource');
app.set('json spaces', 4);

app.use('/', express.static('files/protectedResource'));
app.use(cors());

var resource = {
    "name": "Protected Resource",
    "description": "This data has been protected by OAuth 2.0"
};

var getAccessToken = function (req, res, next) {
    /*
     * 没有刷新令牌，需要让用户重新获取新的访问令牌
     */
    var inToken = null;
    var auth = req.headers['authorization'];
    if (auth && auth.toLowerCase().indexOf('bearer') == 0) {
        // 从授权头中提取令牌
        inToken = auth.slice('bearer '.length);
    } else if (req.body && req.body.access_token) {
        // 从请求体中提取令牌
        inToken = req.body.access_token;
    } else if (req.query && req.query.access_token) {
        // 从查询参数中提取令牌
        inToken = req.query.access_token
    }

    //验证令牌
    console.log('传入的令牌：%s', inToken);
    nosql.one().make(function (builder) {
        builder.where('access_token', inToken);
        builder.callback(function (err, token) {
            if (token) {
                console.log("我们找到了匹配的令牌：%s", inToken);
            } else {
                console.log('未找到匹配的令牌。');
            }
            req.access_token = token;
            next();
            return;
        });
    });
};

app.options('/resource', cors());


/*
 * 把 getAccessToken 函数添加到这个处理器中
 */
app.post("/resource", cors(), getAccessToken, function (req, res) {

    /*
     * 检查是否找到了访问令牌
     */
    if (req.access_token) {
        res.json(resource);
    } else {
        res.status(401).end();
    }

});

var server = app.listen(9002, 'localhost', function () {
    var host = server.address().address;
    var port = server.address().port;

    console.log('OAuth Resource Server is listening at http://%s:%s', host, port);
});

