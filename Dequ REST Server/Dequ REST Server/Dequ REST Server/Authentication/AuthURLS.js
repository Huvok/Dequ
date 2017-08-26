/* TASK Authentication */

//                                                          //AUTHOR: Hugo García
//                                                          //CO-AUTHOR: ()
//                                                          //DATE: 7/6/2017
//                                                          //PURPOSE: Authorization for the use of the REST services.

//======================================================================================================================
'use strict';

//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXTERNAL DEPENDENCIES
var http = require('https');
var jwt = require('jwt-simple');

//----------------------------------------------------------------------------------------------------------------------
//                                                          //INTERNAL DEPENDENCIES
var app = require('../app');
var config = app.config;
var passport = app.passport;
var User = require('../models/user');

//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXPORTS

module.exports = function (app) {
    app.get('/api/tokenfactory', function (req, res) {
        if (!req.query.user_id)
        {
            res.json({ sucess: false, msg: 'Please give an user id.' });
        }
        else 
        {
            var accessToken = req.header('Authorization');
            var tokenValidationPath = 'https://graph.facebook.com/debug_token?input_token=' + accessToken + '&access_token=' + config['appToken'];
            var strAuthResult = '';
            http.request(tokenValidationPath, function (response) {
                var str = '';

                //                                          //Get all the response data.
                response.on('data', function (chunk) {
                    str += chunk;
                });

                //                                          //Extract the result from the data.
                response.on('end', function () {
                    strAuthResult = JSON.parse(str)['data']['is_valid'];
                });
            }).end();

            if (strAuthResult == 'false')
            {
                console.log('Received a non-valid user.');
            }
            else
            {
                var userFromToken;
                var boolUserCreated = false;

                //                                          //Check if the user already exists in our db.
                User.findOne({ user_id: req.query.user_id }, function (err, user) {
                    if (err) throw err;

                    //                                      //If not... create it.
                    if (!user)
                    {
                        user = {};
                        user.user_id = req.query.user_id;
                        user.name = req.query.name;
                        user.lastname = req.query.lastname;
                        User.addUser(user, function (err, user) {
                            if (err)
                            {
                                throw err;
                            }
                            else
                            {
                                var token = jwt.encode(user, config.secret);
                                res.json({ sucess: true, token: 'JWT ' + token, user_created: true });
                            }
                        });
                    }
                    else
                    {
                        var token = jwt.encode(user, config.secret);
                        res.json({ sucess: true, token: 'JWT ' + token, user_created: false });
                    }
                });

            }
        }
    });
}
//======================================================================================================================
/* END-TASK */