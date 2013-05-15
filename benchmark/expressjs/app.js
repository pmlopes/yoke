var express = require('express');
var app = express();

app.configure(function(){
    app.use(express.bodyParser());
});

app.get('/', function(req, res){
    res.send('Hello World\n');
});

app.get('/json', function(req, res){
    res.send({ name: 'Tobi', role: 'admin' });
});

function foo(req, res, next) {
    next();
}

app.get('/middleware', foo, foo, foo, foo, function(req, res){
    res.send('1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890');
});

app.listen(8000);