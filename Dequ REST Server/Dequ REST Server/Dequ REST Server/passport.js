/* TASK Dequ REST Server Passport Strategy */

//                                                          //AUTHOR: Hugo García
//                                                          //CO-AUTHOR: ()
//                                                          //DATE: 7/3/2017
//                                                          //PURPOSE: Strategy implementation for passport.

//======================================================================================================================
"use strict";

//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXTERNAL DEPENDENCIES
var JwtStrategy = require('passport-jwt').Strategy;
var ExtractJwt = require('passport-jwt').ExtractJwt;

//----------------------------------------------------------------------------------------------------------------------
//                                                          //INTERNAL DEPENDENCIES
var app = require('./app');
var config = app.config;
var User = require('./models/user');

//----------------------------------------------------------------------------------------------------------------------
//                                                          //EXPORTS
module.exports = function (passport) {
    var JWTOptions = {};
    JWTOptions.secretOrKey = config['secret'];
    JWTOptions.jwtFromRequest = ExtractJwt.fromAuthHeader();
    passport.use(new JwtStrategy(JWTOptions, function (jwt_payload, done) {
        User.findOne( { id: jwt_payload.id }, function (err, user) {
            if (err)
            {
                return done(err, false);
            }

            if (user)
            {
                done(null, user);
            }
            else
            {
                done(null, false);
            }
        });
    }));
};

//======================================================================================================================
/* END-TASK */