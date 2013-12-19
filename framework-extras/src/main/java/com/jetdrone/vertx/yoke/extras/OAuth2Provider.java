package com.jetdrone.vertx.yoke.extras;

class OAuth2Provider {

    private String cryptSecret;

    public OAuth2Provider(String signKey) {
        cryptSecret = signKey;
    }

    private String encodeUrlSaveBase64(String str) {
        // return str.replace(/\+/g, '-').replace(/\//g, '_').replace(/\=+$/, '');

        return null;
    }

    private String decodeUrlSaveBase64(String str) {
        // str = (str + '===').slice(0, str.length + (str.length % 4));
        //return str.replace(/-/g, '+').replace(/_/g, '/');
        return null;
    }

    private String encrypt(String data) {
//        var cipher = crypto.createCipher("aes256", this.cryptSecret);
//        var str = cipher.update(data, 'utf8', 'base64') + cipher.final('base64');
//        str = this._encodeUrlSaveBase64(str);
//        return str;
        return null;
    }

    private String decrypt(String data) {
//        var str = this._decodeUrlSaveBase64(data);
//        var decipher = crypto.createDecipher("aes256", this.cryptSecret);
//        str = decipher.update(str, 'base64', 'utf8') + decipher.final('base64');
//        return str;
        return null;
    }

    public void validateToken(String token, Object callback) {
//        var self = this,
//                tokenData;
//
//        try {
//                tokenData = self._decrypt(token);
//        } catch(e) {
//                return callback(new Error("decrypting token failed"));
//        }
//
//        callback(null, tokenData);
    }

    private void get_oauth(Object req, Object res, Object next) {
//        var self = this,
//                clientId = req.query.client_id,
//                redirectUri = req.query.redirect_uri,
//                responseType = req.query.response_type || 'token';
//
//        if(!clientId || !redirectUri) {
//                return self.emit('authorizeParamMissing', req, res, next);
//        }
//
//        var authorizeUrl = req.url;
//
//        self.emit('enforceLogin', req, res, authorizeUrl, function(userId) {
//                self.emit('shouldSkipAllow', userId, clientId, function(skip, tokenDataStr) {
//                        if(skip) {
//                                self._validateThings(req, res, clientId, redirectUri, responseType, function(){
//                                        if(tokenDataStr) {
//                                                self._redirectWithToken(tokenDataStr, redirectUri, res);
//                                        }else{
//                                                self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                                                        self._redirectWithToken(tokenDataStr, redirectUri, res);
//                                                });
//                                        }
//                                });
//                        }else{
//                                authorizeUrl += '&x_user_id=' + self._encrypt(userId);
//                                self.emit('authorizeForm', req, res, clientId, authorizeUrl);
//                        }
//                });
//        });
    }

    private void post_oauth(Object req, Object res, Object next) {
//        var self = this;
//
//        var clientId = req.query.client_id,
//                url = req.query.redirect_uri,
//                responseType = req.query.response_type || 'token',
//                state = req.query.state,
//                xUserId = req.query.x_user_id;
//
//        self._validateThings(req, res, clientId, url, responseType, function(){
//
//                if(!req.body.allow) {
//                        return self._redirectError(res, responseType, url, "access_denied");
//                }
//
//                if('token' === responseType) {
//                        var userId;
//                        try {
//                                userId = self._decrypt(xUserId);
//                        } catch(e) {
//                                return self.emit('parameterError', req, res);
//                        }
//
//                        self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                                var atok = self._encrypt(tokenDataStr);
//                                url += "#access_token=" + atok;
//                                res.writeHead(303, {Location: url});
//                                res.end();
//                        });
//                } else {
//                        self.emit('createGrant', req, clientId, function(codeStr) {
//                                codeStr = self._encrypt(codeStr);
//                                url += "?code=" + codeStr;
//
//                                // pass back anti-CSRF opaque value
//                                if(state) {
//                                        url += "&state=" + state;
//                                }
//
//                                res.writeHead(303, {Location: url});
//                                res.end();
//                        });
//                }
//        });
    }

    private void redirectWithToken(String tokenDataStr, String redirectUri, Object callback) {
//        var atok = this._encrypt(tokenDataStr);
//        redirectUri += "#access_token=" + atok;
//        res.writeHead(303, {Location: redirectUri});
//        return res.end();
    }

    private void validateThings(Object req, Object res, String clientId, String redirectUri, Object responseType, Object callback) {
//    var self = this;
//            if(responseType !== "code" && responseType !== "token") {
//                    return self.emit('responseTypeError', req, res);
//            }
//            self.emit('validateClientIdAndRedirectUri', clientId, redirectUri, req, res, callback);
    }

    private void redirectError(Object res, Object responseType, String url, Object error) {
//var sep = responseType === "token" ? "#" : "?";
//        res.writeHead(303, {Location: url + sep + "error=" + error});
//        return res.end();
    }

    private void post_access_token(Object req, Object res, Object next) {
//        var self = this,
//                clientId = req.body.client_id,
//                clientSecret = req.body.client_secret,
//                redirectUri = req.body.redirect_uri,
//                code = req.body.code;
//
//        try {
//                code = self._decrypt(code);
//        } catch(e) {
//                return self.emit('accessDenied', req, res);
//        }
//
//        self.emit('lookupGrant', clientId, clientSecret, code, res, function(userId) {
//                self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                        var atok = self._encrypt(tokenDataStr);
//                        res.json({access_token:atok});
//                });
//        });
    }

    public void oauth() {
//        var self = this;
//        return function(req, res, next) {
//                var uri = ~req.url.indexOf('?') ? req.url.substr(0, req.url.indexOf('?')) : req.url;
//                if(req.method === 'GET' && uri === '/oauth/authorize') {
//                        self._get_oauth(req, res, next);
//                } else if(req.method === 'POST' && uri === '/oauth/authorize') {
//                        self._post_oauth(req, res, next);
//                } else if(req.method === 'POST' && uri === '/oauth/access_token') {
//                        self._post_access_token(req, res, next);
//                } else {
//                        next();
//                }
//        };
    }
}

/*
var OAuth2Provider = require('../index'),
	express = require('express'),
	MemoryStore = express.session.MemoryStore;

var oauthProvider = new OAuth2Provider("signing-secret");

oauthProvider.on('authorizeParamMissing', function(req, res, callback) {
	res.writeHead(400);
	res.end("missing param");
});

oauthProvider.on('enforceLogin', function(req, res, authorizeUrl, callback) {
	if(req.session.user) {
		callback(req.session.user);
	} else {
		res.writeHead(303, {Location: '/login?next=' + encodeURIComponent(authorizeUrl)});
		res.end();
	}
});

oauthProvider.on('shouldSkipAllow', function(userId, clientId, callback){
	callback();
});

oauthProvider.on('validateClientIdAndRedirectUri', function(clientId, redirectUri, req, res, callback) {
	callback();
});

oauthProvider.on('authorizeForm', function(req, res, clientId, authorizeUrl) {
	res.end('<html>this app wants to access your account... <form method="post" action="' + authorizeUrl + '"><button name="allow" value="true">Allow</button></form>');
});

oauthProvider.on('invalidResponseType', function(req, res, callback) {
	res.writeHead(400);
	res.end("invalid response type");
});

oauthProvider.on('accessDenied', function(req, res, callback) {
	res.json(401, {error:"access denied"});
});

oauthProvider.on('createAccessToken', function(userId, clientId, callback) {
	callback("test-tooken");
});

oauthProvider.on('createGrant', function(req, clientId, callback) {
	callback("ABC123");
});

oauthProvider.on('lookupGrant', function(clientId, clientSecret, code, res, callback) {
	callback("userId");
});

var app = express();

// app.use(express.logger());
app.use(express.bodyParser());
app.use(express.query());
app.use(express.cookieParser());
app.use(express.session({store: new MemoryStore({reapInterval: 5 * 60 * 1000}), secret: 'abracadabra'}));
app.use(oauthProvider.oauth());

app.get('/', function(req, res, next) {
	console.dir(req.session);
	res.end('home, logged in? ' + !!req.session.user);
});

app.get('/login', function(req, res, next) {
	if(req.session.user) {
		res.writeHead(303, {Location: '/'});
		return res.end();
	}

	var next_url = req.query.next ? req.query.next : '/';
	res.end('<html><form method="post" action="/login"><input type="hidden" name="next" value="' + next_url + '"><input type="text" placeholder="username" name="username"><input type="password" placeholder="password" name="password"><button type="submit">Login</button></form>');
});

app.post('/login', function(req, res, next) {
	req.session.user = req.body.username;
	res.writeHead(303, {Location: req.body.next || '/'});
	res.end();
});

app.get('/logout', function(req, res, next) {
	req.session.destroy(function(err) {
		res.writeHead(303, {Location: '/'});
		res.end();
	});
});

app.get('/protected_resource', function(req, res, next) {
	if(req.query.access_token) {
		var accessToken = req.query.access_token;
		res.json(accessToken);
	} else {
		res.writeHead(403);
		res.end('no token found');
	}
});

app.listen(8081);
*/