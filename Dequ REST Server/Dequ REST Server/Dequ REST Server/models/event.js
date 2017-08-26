var mongoose = require('mongoose');

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

// Add Event
module.exports.addEvent = function (event, callback) {
    Event.create(event, callback);
}

// Update User
module.exports.updateEvent = function (id, event, options, callback) {
    var query = { _id: id };
    var update = {
        report: event.report,
        title: event.title,
        people_needed: event.people_needed,
        people_count: event.people_count,
        due_date: event.due_date
    }

    Event.findOneAndUpdate(query, update, options, callback);
}

// Delete User
module.exports.removeEvent = function (id, callback) {
    var query = { _id: id };
    Event.remove(query, callback);
}