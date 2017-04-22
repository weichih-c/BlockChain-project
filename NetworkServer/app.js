var https = require('https')
// var http = require('http')
,	path = require('path')
,	fs = require('fs')
,	express = require('express')
,   request = require('request')
,	clientList = require('./app/clientList.js')()


var logger = require('morgan')
,	methodOverride = require('method-override')
,	bodyParser = require('body-parser')
,	errorHandler = require('errorhandler');

var app = express();
var serverPort = 3001;

// all environments
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(methodOverride());
app.use(express.static(path.join(__dirname, 'public')));
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

// development only
if ('development' == app.get('env')) {
  app.use(errorHandler());
};

var options = {
	key: fs.readFileSync('./server-key.pem'),
	cert: fs.readFileSync('./server-crt.pem')
};

// routing
require('./app/routes.js')(app, clientList);

var server = https.createServer(options, app);
// var server = http.createServer(app);
server.listen(serverPort, function() {
  console.log('server up and running at %s port', serverPort);
});

var io = require('socket.io')(server, { pingTimeout: 10000, pingInterval: 4000 });
/**
 * Socket.io event handling
 */
require('./app/socketHandler.js')(io, request, clientList);
