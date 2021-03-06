﻿var mongoose = require('mongoose');

// Event Schema
var eventSchema = mongoose.Schema({
    report: {
        type: String,
        unique: true,
        required: true
    },
    user_id: {
        type: String,
        required: true
    },
    title: {
        type: String,
        required: true
    },
    people_needed: {
        type: Number,
        required: true
    },
    people_count: {
        type: Number,
        required: true
    },
    due_date: {
        type: Date,
        required: true
    },
    event_origin: {
        type: String,
        required: true
    },
    FB_id: {
        type: String,
        default: "N/A"
    },
    attending: {
        type: Array,
        default: []
    },
    create_date: {
        type: Date,
        default: Date.now
    }
});

var Event = module.exports = mongoose.model('Event', eventSchema);

// Get Event by ID
module.exports.getEventById = function (id, callback) {
    Event.findById(id, callback);
}

module.exports.getEventByReport = function (id, callback) {
    Event.findOne({ report: id }, callback);
}

// Add Event
module.exports.addEvent = function (event, callback) {
    event.due_date = new Date(event.due_date);
    event.attending = [event.user_id];
    Event.create(event, callback);
}

// Update User
module.exports.updateEvent = function (old_id, user_id, options, callback) {
    var query = { _id: old_id };

    Event.findOne(query, function (err, event) {
        if (err) {
            console.log(err);
        }
        else {
            event.people_count = event.people_count + 1;
            event.attending.push(user_id);
            event.markModified('object');
            event.save(callback);
        }
    });
}

// Delete User
module.exports.removeEvent = function (id, callback) {
    var query = { _id: id };
    Event.remove(query, callback);
}