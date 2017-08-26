/* TASK Dequ REST Server Main App */

//                                                          //AUTHOR: Hugo García
//                                                          //CO-AUTHOR: ()
//                                                          //DATE: 7/3/2017
//                                                          //PURPOSE: Main app for the server.

//======================================================================================================================
"use strict";

//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXTERNAL DEPENDENCIES
var express = require('express');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var morgan = require('morgan');
var passport = require('passport');
var jwt = require('jwt-simple');
var fs = require("fs");

//----------------------------------------------------------------------------------------------------------------------
//                                                          //INTERNAL DEPENDENCIES
var User = require('./models/user');
var Report = require('./models/report');
var Event = require('./models/event');
var config = require('./config');

//----------------------------------------------------------------------------------------------------------------------
//                                                          //MAIN
var app = express();
app.use(morgan('dev'));
app.use(passport.initialize());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));
app.use(bodyParser.json());

//                                                          //Connect to Mongoose.
mongoose.connect('mongodb://localhost/Dequ-DEV', function(err) {
    if (err)
    {
        throw err;
    }

	console.log("Connection to database successfull.");
});

var db = mongoose.connection;

//                                                          //Server Root.
app.get('/', function(req, res) {
	res.send('Please use /api/...');
});

app.get('/api/users', function (req, res) {
    User.getUsers(function (err, users) {
        if (err) {
            throw err;
        }
        else {
            res.json(users);
        }
    });
});

app.post('/api/users', function(req, res) {
	var user = req.body;
	User.addUser(user, function(err, user) {
		if (err)
		{
			throw err;
		}
		else
		{
			res.json(user);
		}
	});
});

app.put('/api/users/:_id', function(req, res) {
	var id = req.params._id;
	var user = req.body;
	User.updateUser(id, user, {}, function(err, user) {
		if (err)
		{
			throw err;
		}
		else
		{
			res.json(user);
		}
	});
});

app.delete('/api/users/:_id', function(req, res) {
	var id = req.params._id;
	User.removeUser(id, function(err, user) {
		if (err)
		{
			throw err;
		}
		else
		{
			res.json(user);
		}
	});
});

app.get('/api/reports', function(req, res){
	Report.getReports(function(err, reports) {
		if (err)
		{
			throw err;
		}
		else
		{
			res.json(reports);
		}
	});
});

app.get('/api/report', function (req, res) {
	Report.getReportById(req.query.id, function(err, report) {
		if (err)
		{
			throw err;
		}
		else
        {
            var response = {};
            response._id = report._id;
            response.user_id = report.user_id;
            response.title = report.title;
            response.type = report.type;
            response.level = report.level;
            response.description = report.description;
            response.latitude = report.latitude;
            response.longitude = report.longitude;
            response.create_date = report.create_date;
            response.has_event = report.has_event;
            response.image = new Buffer(fs.readFileSync(__dirname + "/uploads/" + report._id + ".jpg")).toString('base64');
			res.json(response);
		}
	});
});

app.post('/api/reports', function (req, res) {
    var report = {};
    report.user_id = req.body.user_id;
    report.title = req.body.title;
    report.type = req.body.type;
    report.level = req.body.level;
    report.description = req.body.description;
    report.latitude = req.body.latitude;
    report.longitude = req.body.longitude;

	Report.addReport(report, function(err, report) {
		if (err)
		{
			throw err;
		}
		else
        {
            fs.writeFile(__dirname + "/uploads/" + report._id.toString() + ".jpg", new Buffer(req.body.image, "base64"), function (err) {
                if (err)
                {
                    res.json(err);
                }
                else
                {
                    res.json(report);
                }
            });
		}
	});
});

app.put('/api/reports/:_id', function(req, res) {
	var id = req.params._id;
	var report = req.body;
	Report.updateReport(id, report, {}, function(err, report) {
		if (err)
		{
			throw err;
		}
		else
        {
			res.json(report);
		}
	});
});

app.delete('/api/reports/:_id', function(req, res) {
	var id = req.params._id;
	Report.removeReport(id, function(err, report) {
		if (err)
		{
			throw err;
		}
		else
		{
			res.json(report);
		}
	});
});

app.get('/api/event', function (req, res) {
    Event.getEventById(req.query.id, function (err, event) {
        if (err) {
            throw err;
        }
        else {
            res.json(event);
        }
    });
});

app.post('/api/event', function (req, res) {
    var event = req.body;
    Event.addEvent(event, function (err, event) {
        if (err) {
            throw err;
        }
        else {
            res.json(event);
        }
    });
});

app.listen(3000, '0.0.0.0');
console.log('Running on port 3000...');

app.get('/api/theorder', passport.authenticate('jwt', { session: false }), function (req, res) {
    var token = getToken(req.headers);
    if (token) {
        var decoded = jwt.decode(token, config.secret);
        User.findOne({
            user_id: decoded.user_id
        }, function (err, user) {
            if (err) throw err;

            if (!user) {
                return res.status(403).send({ success: false, msg: 'Authentication failed. User not found.' });
            } else {
                res.json({ success: true, msg: 'Welcome to The Order, ' + user.name + '.' });
            }
        });
    }
    else {
        return res.status(403).send({ success: false, msg: 'No token provided.' });
    }

});

var getToken = function (headers) {
    if (headers && headers.authorization) {
        var parted = headers.authorization.split(' ');
        if (parted.length === 2) {
            return parted[1];
        } else {
            return null;
        }
    } else {
        return null;
    }
};
var caca = 5;
//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXPORTS
var appExports = {};
appExports.passport = passport;
appExports.config = config;
appExports.app = app;
module.exports = appExports;
require('./Authentication/AuthURLS')(app);
require('./passport')(passport);

//======================================================================================================================
/* END-TASK */