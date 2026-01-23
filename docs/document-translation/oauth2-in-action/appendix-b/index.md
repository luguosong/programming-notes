# 附录 B：扩展代码清单

本附录收录了全书各个练习的扩展代码清单。在各章内容中，我们尽量把重点放在实现功能所必需的代码部分。我们不会把整个代码库重复贴出来，因为你随时都可以在
GitHub 上查看完整代码。不过，把某一章里提到的多个代码片段放到比章节篇幅允许的更大一些的上下文中来观察，往往会更有帮助。这里列出书中多处引用到的一些较大函数。

## 清单 1：授权请求函数（3-1）

```javascript title="清单 1：授权请求函数（3-1）"
app.get('/authorize', function (req, res) {

    access_token = null;

    state = randomstring.generate();

    var authorizeUrl = buildUrl(authServer.authorizationEndpoint, {
        response_type: 'code',
        client_id: client.client_id,
        redirect_uri: client.redirect_uris[0],
        state: state
    });

    console.log("redirect", authorizeUrl);
    res.redirect(authorizeUrl);
});
```

## 清单 2：回调与令牌请求（3-1）

```javascript title="清单 2：回调与令牌请求（3-1）"
app.get('/callback', function (req, res) {

    if (req.query.error) {
        res.render('error', {error: req.query.error});
        return;
    }

    if (req.query.state != state) {
        console.log('State DOES NOT MATCH: expected %s got %s', state, req.query.state);
        res.render('error', {error: 'State value did not match'});
        return;
    }

    var code = req.query.code;

    var form_data = qs.stringify({
        grant_type: 'authorization_code',
        code: code,
        redirect_uri: client.redirect_uris[0]
    });
    var headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
    };

    var tokRes = request('POST', authServer.tokenEndpoint, {
        body: form_data,
        headers: headers
    });

    console.log('Requesting access token for code %s', code);

    if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
        var body = JSON.parse(tokRes.getBody());

        access_token = body.access_token;
        console.log('Got access token: %s', access_token);

        res.render('index', {access_token: access_token, scope: scope});
    } else {
        res.render('error', {error: 'Unable to fetch access token, server response: ' + tokRes.statusCode})
    }
});
```

## 清单 3：获取受保护的资源（3-1）

```javascript title="清单 3：获取受保护的资源（3-1）"
app.get('/fetch_resource', function (req, res) {

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
```

## 清单 4：刷新访问令牌（3-2）

``` javascript title="清单 4：刷新访问令牌（3-2）"
app.get('/fetch_resource', function(req, res) {

  console.log('Making request with access token %s', access_token);

  var headers = {
      'Authorization': 'Bearer ' + access_token,
      'Content-Type': 'application/x-www-form-urlencoded'
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
      if (refresh_token) {
            refreshAccessToken(req, res);
            return;
      } else {
            res.render('error', {error: resource.statusCode});
            return;
      }
  }


});

var refreshAccessToken = function(req, res) {
  var form_data = qs.stringify({
      grant_type: 'refresh_token',
      refresh_token: refresh_token
  });
  var headers = {
      'Content-Type': 'application/x-www-form-urlencoded',
       'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
  };
  console.log('Refreshing token %s', refresh_token);
  var tokRes = request('POST', authServer.tokenEndpoint, {
                 body: form_data,
              headers: headers
  });
  if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
      var body = JSON.parse(tokRes.getBody());

      access_token = body.access_token;
      console.log('Got access token: %s', access_token);
      if (body.refresh_token) {
            refresh_token = body.refresh_token;
            console.log('Got refresh token: %s', refresh_token);
      }
      scope = body.scope;
      console.log('Got scope: %s', scope);

      res.redirect('/fetch_resource');
      return;
  } else {
      console.log('No refresh token, asking the user to get a new access
        token');
      refresh_token = null;
      res.render('error', {error: 'Unable to refresh token.'});
      return;
  }
};
```

## 清单 5：提取访问令牌（4-1）

``` javascript title="清单 5：提取访问令牌（4-1）"
var getAccessToken = function(req, res, next) {

  var inToken = null;
  var auth = req.headers['authorization'];
  if (auth && auth.toLowerCase().indexOf('bearer') == 0) {
      inToken = auth.slice('bearer '.length);
  } else if (req.body && req.body.access_token) {
      inToken = req.body.access_token;
  } else if (req.query && req.query.access_token) {
      inToken = req.query.access_token
  }
};
```

## 清单 6：查找令牌（4-1）

``` javascript title="清单 6：查找令牌（4-1）"
var getAccessToken = function(req, res, next) {

  var inToken = null;
  var auth = req.headers['authorization'];
  if (auth && auth.toLowerCase().indexOf('bearer') == 0) {
      inToken = auth.slice('bearer '.length);
  } else if (req.body && req.body.access_token) {
      inToken = req.body.access_token;
  } else if (req.query && req.query.access_token) {
      inToken = req.query.access_token
  }

  console.log('Incoming token: %s', inToken);
  nosql.one().make(function(builder) {
  builder.where('access_token', inToken);
  builder.callback(function(err, token) {
    if (token) {
      console.log("We found a matching token: %s", inToken);
    } else {
      console.log('No matching token was found.');
    };
    req.access_token = token;
    next();
    return;
  });
});
```

## 清单 7：授权端点（5-1）

```javascript title="清单 7：授权端点（5-1）"
app.get("/authorize", function (req, res) {

    var client = getClient(req.query.client_id);

    if (!client) {
        console.log('Unknown client %s', req.query.client_id);
        res.render('error', {error: 'Unknown client'});
        return;
    } else if (!__.contains(client.redirect_uris, req.query.redirect_uri)) {
        console.log('Mismatched redirect URI, expected %s got %s',
            client.redirect_uris, req.query.redirect_uri);
        res.render('error', {error: 'Invalid redirect URI'});
        return;
    } else {

        var reqid = randomstring.generate(8);

        requests[reqid] = req.query;

        res.render('approve', {client: client, reqid: reqid});
        return;
    }

});
```

## 清单 8：处理用户授权（5-1）

```javascript title="清单 8：处理用户授权（5-1）"
app.post('/approve', function (req, res) {

    var reqid = req.body.reqid;
    var query = requests[reqid];
    delete requests[reqid];

    if (!query) {
        res.render('error', {error: 'No matching authorization request'});
        return;
    }

    if (req.body.approve) {
        if (query.response_type == 'code') {
            var code = randomstring.generate(8);

            codes[code] = {request: query};

            var urlParsed = buildUrl(query.redirect_uri, {
                code: code,
                state: query.state
            });
            res.redirect(urlParsed);
            return;
        } else {
            var urlParsed = buildUrl(query.redirect_uri, {
                error: 'unsupported_response_type'
            });
            res.redirect(urlParsed);
            return;
        }
    } else {
        var urlParsed = buildUrl(query.redirect_uri, {
            error: 'access_denied'
        });
        res.redirect(urlParsed);
        return;
    }

});
```

## 清单 9：令牌端点（5-1）

``` javascript title="清单 9：令牌端点（5-1）"
app.post("/token", function(req, res){

  var auth = req.headers['authorization'];
  if (auth) {
      var clientCredentials = decodeClientCredentials(auth);
      var clientId = clientCredentials.id;
      var clientSecret = clientCredentials.secret;
  }

  if (req.body.client_id) {
      if (clientId) {
            console.log('Client attempted to authenticate with multiple methods');
            res.status(401).json({error: 'invalid_client'});
            return;
      }

      var clientId = req.body.client_id;
      var clientSecret = req.body.client_secret;
  }

  var client = getClient(clientId);
  if (!client) {
      console.log('Unknown client %s', clientId);
      res.status(401).json({error: 'invalid_client'});
      return;
  }

  if (client.client_secret != clientSecret) {
      console.log('Mismatched client secret, expected %s got %s',
      client.client_secret, clientSecret);
      res.status(401).json({error: 'invalid_client'});
      return;
  }

  if (req.body.grant_type == 'authorization_code') {

      var code = codes[req.body.code];

      if (code) {
            delete codes[req.body.code]; // burn our code, it's been used
            if (code.request.client_id == clientId) {

                  var access_token = randomstring.generate();
                  nosql.insert({ access_token: access_token, client_id: clientId });

                  console.log('Issuing access token %s', access_token);

                  var token_response = { access_token: access_token, token_type: 'Bearer' };

                  res.status(200).json(token_response);
                  console.log('Issued tokens for code %s', req.body.code);

                  return;
            } else {
                  console.log('Client mismatch, expected %s got %s', code.request.client_id, clientId);
                  res.status(400).json({error: 'invalid_grant'});
                  return;
            }
      } else {
            console.log('Unknown code, %s', req.body.code);
            res.status(400).json({error: 'invalid_grant'});
            return;
      }
  } else {
      console.log('Unknown grant type %s', req.body.grant_type);
      res.status(400).json({error: 'unsupported_grant_type'});
  }
});
```

## 清单 10：刷新访问令牌（5-2）

``` javascript title="清单 10：刷新访问令牌（5-2）"
} else if (req.body.grant_type == 'refresh_token') {
  nosql.one().make(function(builder) {
  builder.where('refresh_token', req.body.refresh_token);
  builder.callback(function(err, token) {
    if (token) {
      console.log("We found a matching refresh token: %s", req.body.refresh_token);
      if (token.client_id != clientId) {
                nosql.remove().make(function(builder) { builder.where('refresh_token', req.body.refresh_token); });
        res.status(400).json({error: 'invalid_grant'});
        return;
      }
      var access_token = randomstring.generate();
      nosql.insert({ access_token: access_token, client_id: clientId });
      var token_response = { access_token: access_token, token_type: 'Bearer',  refresh_token: token.refresh_token };
      res.status(200).json(token_response);
      return;
    } else {
      console.log('No matching token was found.');
      res.status(400).json({error: 'invalid_grant'});
      return;
    };
  })
});
```

## 清单 11：自省端点（11-4）

``` javascript title="清单 11：自省端点（11-4）"
app.post('/introspect', function(req, res) {
  var auth = req.headers['authorization'];
  var resourceCredentials = decodeClientCredentials(auth);
  var resourceId = resourceCredentials.id;
  var resourceSecret = resourceCredentials.secret;

  var resource = getProtectedResource(resourceId);
  if (!resource) {
      console.log('Unknown resource %s', resourceId);
      res.status(401).end();
      return;
  }

  if (resource.resource_secret != resourceSecret) {
      console.log('Mismatched secret, expected %s got %s', resource.resource_secret, resourceSecret);
      res.status(401).end();
      return;
  }

  var inToken = req.body.token;
  console.log('Introspecting token %s', inToken);
  nosql.one().make(function(builder) {
    builder.where('access_token', inToken);
    builder.callback(function(err, token) {
      if (token) {
        console.log("We found a matching token: %s", inToken);

        var introspectionResponse = {
          active: true,
          iss: 'http://localhost:9001/',
          aud: 'http://localhost:9002/',
          sub: token.user ? token.user.sub : undefined,
          username: token.user ? token.user.preferred_username : undefined,
          scope: token.scope ? token.scope.join(' ') : undefined,
          client_id: token.client_id
        };
                                                
        res.status(200).json(introspectionResponse);
          return;
      } else {
          console.log('No matching token was found.');

          var introspectionResponse = {
            active: false
          };
           res.status(200).json(introspectionResponse);
          return;
        }
      });
  });


});
```

## 清单 12：令牌撤销端点（11-5）

``` javascript title="清单 12：令牌撤销端点（11-5）"
app.post('/revoke', function(req, res) {
  var auth = req.headers['authorization'];
  if (auth) {
      // check the auth header
      var clientCredentials = decodeClientCredentials(auth);
      var clientId = clientCredentials.id;
      var clientSecret = clientCredentials.secret;
  }

  // otherwise, check the post body
  if (req.body.client_id) {
      if (clientId) {
            // if we've already seen the client's credentials in the authorization header, this is an error
            console.log('Client attempted to authenticate with multiple methods');
            res.status(401).json({error: 'invalid_client'});
            return;
      }

      var clientId = req.body.client_id;
      var clientSecret = req.body.client_secret;
  }

  var client = getClient(clientId);
  if (!client) {
      console.log('Unknown client %s', clientId);
      res.status(401).json({error: 'invalid_client'});
      return;
  }

  if (client.client_secret != clientSecret) {
      console.log('Mismatched client secret, expected %s got %s', client.client_secret, clientSecret);
      res.status(401).json({error: 'invalid_client'});
      return;
  }

  var inToken = req.body.token;
  nosql.remove().make(function(builder) {
    builder.and();
    builder.where('access_token', inToken);
    builder.where('client_id', clientId);
    builder.callback(function(err, count) {
      console.log("Removed %s tokens", count);
      res.status(204).end();
      return;
    });
  });

});
```

## 清单 13：注册端点（12-1）

``` javascript title="清单 13：注册端点（12-1）"
app.post('/register', function (req, res){

  var reg = {};

  if (!req.body.token_endpoint_auth_method) {
      reg.token_endpoint_auth_method = 'secret_basic';
  } else {
      reg.token_endpoint_auth_method = req.body.token_endpoint_auth_method;
  }

  if (!__.contains(['secret_basic', 'secret_post', 'none'], reg.token_endpoint_auth_method)) {
      res.status(400).json({error: 'invalid_client_metadata'});
      return;
  }

  if (!req.body.grant_types) {
      if (!req.body.response_types) {
            reg.grant_types = ['authorization_code'];
            reg.response_types = ['code'];
      } else {
            reg.response_types = req.body.response_types;
            if (__.contains(req.body.response_types, 'code')) {
                  reg.grant_types = ['authorization_code'];
            } else {
                  reg.grant_types = [];
            }
      }
  } else {
      if (!req.body.response_types) {
            reg.grant_types = req.body.grant_types;
            if (__.contains(req.body.grant_types, 'authorization_code')) {
                  reg.response_types =['code'];
            } else {
                  reg.response_types = [];
            }
      } else {
            reg.grant_types = req.body.grant_types;
            reg.reponse_types = req.body.response_types;
            if (__.contains(req.body.grant_types, 'authorization_code') && !__.contains(req.body.response_types, 'code')) {
                  reg.response_types.push('code');
            }
            if (!__.contains(req.body.grant_types, 'authorization_code') && __.contains(req.body.response_types, 'code')) {
                  reg.grant_types.push('authorization_code');
            }
      }
  }

  if (!__.isEmpty(__.without(reg.grant_types, 'authorization_code', 'refresh_token')) ||
      !__.isEmpty(__.without(reg.response_types, 'code'))) {
      res.status(400).json({error: 'invalid_client_metadata'});
      return;
  }

  if (!req.body.redirect_uris || !__.isArray(req.body.redirect_uris) || __.isEmpty(req.body.redirect_uris)) {
      res.status(400).json({error: 'invalid_redirect_uri'});
      return;
  } else {
      reg.redirect_uris = req.body.redirect_uris;
  }

  if (typeof(req.body.client_name) == 'string') {
      reg.client_name = req.body.client_name;
  }

  if (typeof(req.body.client_uri) == 'string') {
      reg.client_uri = req.body.client_uri;
  }

  if (typeof(req.body.logo_uri) == 'string') {
      reg.logo_uri = req.body.logo_uri;
  }

  if (typeof(req.body.scope) == 'string') {
      reg.scope = req.body.scope;
  }

  reg.client_id = randomstring.generate();
  if (__.contains(['client_secret_basic', 'client_secret_post']), reg.token_endpoint_auth_method) {
      reg.client_secret = randomstring.generate();
  }

  reg.client_id_created_at = Math.floor(Date.now() / 1000);
  reg.client_secret_expires_at = 0;

  clients.push(reg);

  res.status(201).json(reg);
  return;
});
```

## 清单 14：UserInfo 端点（13-1）

``` javascript title="清单 14：UserInfo 端点（13-1）"
var userInfoEndpoint = function(req, res) {

  if (!__.contains(req.access_token.scope, 'openid')) {
      res.status(403).end();
      return;
  }

  var user = req.access_token.user;
  if (!user) {
      res.status(404).end();
      return;
  }

  var out = {};
  __.each(req.access_token.scope, function (scope) {
      if (scope == 'openid') {
            __.each(['sub'], function(claim) {
                  if (user[claim]) {
                        out[claim] = user[claim];
                  }
            });
      } else if (scope == 'profile') {
             __.each(['name', 'family_name', 'given_name', 'middle_name',
             'nickname', 'preferred_username', 'profile', 'picture',
             'website', 'gender', 'birthdate', 'zoneinfo', 'locale',
             'updated_at'], function(claim) {
                    if (user[claim]) {
                        out[claim] = user[claim];
                  }
            });
      } else if (scope == 'email') {
            __.each(['email', 'email_verified'], function(claim) {
                  if (user[claim]) {
                        out[claim] = user[claim];
                  }
            });
      } else if (scope == 'address') {
            __.each(['address'], function(claim) {
                  if (user[claim]) {
                        out[claim] = user[claim];
                  }
            });
      } else if (scope == 'phone') {
             __.each(['phone_number', 'phone_number_verified'], function(claim) {
                  if (user[claim]) {
                        out[claim] = user[claim];
                  }
            });
      }
  });

  res.status(200).json(out);
  return;
};
```

## 清单 15：处理 ID 令牌（13-1）

``` javascript title="清单 15：处理 ID 令牌（13-1）"
if (body.id_token) {
  userInfo = null;
  id_token = null;

  console.log('Got ID token: %s', body.id_token);

  var pubKey = jose.KEYUTIL.getKey(rsaKey);
  var tokenParts = body.id_token.split('.');
  var payload = JSON.parse(base64url.decode(tokenParts[1]));
  console.log('Payload', payload);
  if (jose.jws.JWS.verify(body.id_token, pubKey, [rsaKey.alg])) {
      console.log('Signature validated.');
      if (payload.iss == 'http://localhost:9001/') {
            console.log('issuer OK');
            if ((Array.isArray(payload.aud) && __.contains(payload.aud, client.client_id)) ||
                  payload.aud == client.client_id) {
                  console.log('Audience OK');

                  var now = Math.floor(Date.now() / 1000);

                  if (payload.iat <= now) {
                        console.log('issued-at OK');
                        if (payload.exp >= now) {
                              console.log('expiration OK');

                              console.log('Token valid!');

                              id_token = payload;

                        }
                  }
            }
      }
  }
  res.render('userinfo', {userInfo: userInfo, id_token: id_token});
  return;
}
```

## 清单 16：对 PoP 令牌进行自省与验证（15-1）

``` javascript title="清单 16：对 PoP 令牌进行自省与验证（15-1）"
var getAccessToken = function(req, res, next) {
  var auth = req.headers['authorization'];
  var inToken = null;
  if (auth && auth.toLowerCase().indexOf('pop') == 0) {
      inToken = auth.slice('pop '.length);
  } else if (req.body && req.body.pop_access_token) {
      inToken = req.body.pop_access_token;
  } else if (req.query && req.query.pop_access_token) {
      inToken = req.query.pop_access_token
  }

  console.log('Incoming PoP: %s', inToken);
  var tokenParts = inToken.split('.');
  var header = JSON.parse(base64url.decode(tokenParts[0]));
  var payload = JSON.parse(base64url.decode(tokenParts[1]));

  console.log('Payload', payload);

  var at = payload.at;
  console.log('Incmoing access token: %s', at);

  var form_data = qs.stringify({
      token: at
  });
  var headers = {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic ' +
      encodeClientCredentials(protectedResource.resource_id,
      protectedResource.resource_secret)
  };

  var tokRes = request('POST', authServer.introspectionEndpoint, {
      body: form_data,
      headers: headers
  });

  if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
      var body = JSON.parse(tokRes.getBody());

      console.log('Got introspection response', body);
      var active = body.active;
      if (active) {
            var pubKey = jose.KEYUTIL.getKey(body.access_token_key);
            if (jose.jws.JWS.verify(inToken, pubKey, [header.alg])) {
                  console.log('Signature is valid');

                  if (!payload.m || payload.m == req.method) {
                         if (!payload.u || payload.u ==
                                'localhost:9002') {
                               if (!payload.p || payload.p == req.path)
                                     {
                                     console.log('All components
                                        matched');


                                    req.access_token = {
                                          access_token: at,
                                          scope: body.scope
                                    };

                              }
                        }
                  }

            }

      }
  }
  next();
  return;

};
```
