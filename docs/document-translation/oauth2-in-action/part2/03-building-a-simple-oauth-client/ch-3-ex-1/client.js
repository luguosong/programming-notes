var express = require("express");
var request = require("sync-request");
var url = require("url");
var qs = require("qs");
var querystring = require('querystring');
var cons = require('consolidate');
var randomstring = require("randomstring");
var __ = require('underscore');
__.string = require('underscore.string');

var app = express();

app.engine('html', cons.underscore);
app.set('view engine', 'html');
app.set('views', 'files/client');

// authorization server information
var authServer = {
	authorizationEndpoint: 'http://localhost:9001/authorize',
	tokenEndpoint: 'http://localhost:9001/token'
};

// client information


/*
 * 在这里添加客户端信息
 */
var client = {
	"client_id": "oauth-client-1",
	"client_secret": "oauth-client-secret-1",
	"redirect_uris": ["http://localhost:9000/callback"]
};

var protectedResource = 'http://localhost:9002/resource';

var state = null;

var access_token = null;
var scope = null;

app.get('/', function (req, res) {
	res.render('index', {access_token: access_token, scope: scope});
});

app.get('/authorize', function(req, res){

	access_token = null;

	state = randomstring.generate();

	/*
	 * 将用户重定向到授权服务器
	 */
	var authorizeUrl = buildUrl(authServer.authorizationEndpoint, {
		response_type: 'code',
		client_id: client.client_id,
		redirect_uri: client.redirect_uris[0],
		state: state
	});

	console.log("redirect", authorizeUrl);

	res.redirect(authorizeUrl);

});

app.get('/callback', function(req, res){

	/*
	 * 解析授权服务器的响应并获取令牌
	 *
	 * 流程：
	 * 1. 检查是否有 error 参数（用户或授权服务器返回的错误）
	 * 2. 验证 state 是否匹配以防止 CSRF 攻击
	 * 3. 使用收到的 authorization code 向授权服务器的 tokenEndpoint 发送 POST 请求换取 access token
	 * 4. 处理授权服务器的响应，保存 access_token 并渲染页面；若失败则渲染错误页面
	 */

	// 如果授权服务器返回错误（如用户拒绝授权），直接显示错误页面
	if (req.query.error) {
		res.render('error', {error: req.query.error});
		return;
	}

	// 验证 state 值以防止 CSRF 攻击：请求中的 state 必须与之前保存的一致
	if (req.query.state != state) {
		// 日志记录不匹配的 state，便于调试
		console.log('State DOES NOT MATCH: expected %s got %s', state, req.query.state);
		res.render('error', {error: 'State value did not match'});
		return;
	}

	// Authorization Code 授权成功后，授权服务器会返回 code 参数
	var code = req.query.code;

	// 构建请求体，使用 x-www-form-urlencoded 格式按 OAuth2 标准传递参数
	var form_data = qs.stringify({
		grant_type: 'authorization_code',
		code: code,
		redirect_uri: client.redirect_uris[0]
	});

	// 设置请求头：
	// - Content-Type 表明请求体格式
	// - Authorization 使用 HTTP Basic，包含客户端 id 和 secret（先进行 URL 转义，再 base64 编码）
	var headers = {
		'Content-Type': 'application/x-www-form-urlencoded',
		'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
	};

	// 向授权服务器的 tokenEndpoint 发起同步 POST 请求以换取 access token
	var tokRes = request('POST', authServer.tokenEndpoint, {
		body: form_data,
		headers: headers
	});

	// 日志：记录用于换取 token 的 code，便于问题定位
	console.log('正在为代码 %s 请求访问令牌', code);

	// 检查授权服务器响应状态码，2xx 视为成功
	if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
		// 解析响应体（通常为 JSON），提取 access_token 等信息
		var body = JSON.parse(tokRes.getBody());

		// 保存 access_token，后续请求受保护资源时将使用该令牌
		access_token = body.access_token;
		console.log('获取到访问令牌： %s', access_token);

		// 渲染主页面并显示已取得的 access_token（仅用于演示）
		res.render('index', {access_token: access_token, scope: scope});
	} else {
		// 非 2xx 响应视为失败，渲染错误页面并显示状态码以便排查
		res.render('error', {error: 'Unable to fetch access token, server response: ' + tokRes.statusCode})
	}
});

app.get('/fetch_resource', function(req, res) {

	/*
	 * 使用访问令牌调用资源服务器
	 */
	// 先确认是否真的拿到了 access token。要是没有，就给用户提示错误，然后直接退出。
	if (!access_token) {
		res.render('error', {error: 'Missing Access Token'});
	}

	console.log('Making request with access token %s', access_token);

	var headers = {
		'Authorization': 'Bearer ' + access_token
	};

	var resource = request('POST', protectedResource,
		{headers: headers}
	);

	if (resource.statusCode >= 200 && resource.statusCode < 300) {
		var body = JSON.parse(resource.getBody());
		res.render('data', {resource: body});
		return;
	} else {
		access_token = null;
		res.render('error', {error: resource.statusCode});
		return;
	}

});

var buildUrl = function(base, options, hash) {
	var newUrl = url.parse(base, true);
	delete newUrl.search;
	if (!newUrl.query) {
		newUrl.query = {};
	}
	__.each(options, function(value, key, list) {
		newUrl.query[key] = value;
	});
	if (hash) {
		newUrl.hash = hash;
	}

	return url.format(newUrl);
};

var encodeClientCredentials = function(clientId, clientSecret) {
	return Buffer.from(querystring.escape(clientId) + ':' + querystring.escape(clientSecret)).toString('base64');
};

app.use('/', express.static('files/client'));

var server = app.listen(9000, 'localhost', function () {
  var host = server.address().address;
  var port = server.address().port;
  console.log('OAuth Client is listening at http://%s:%s', host, port);
});

