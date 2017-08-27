var mongoose = require('mongoose');
var bcrypt = require('bcrypt');

// User Schema
var userSchema = mongoose.Schema({
    user_id: {
        type: String,
        unique: true,
        required: true
    },
    name: {
		type: String, 
		required: true
	},
	lastname: {
		type: String,
		required: true
    },
    level: {
        type: Number,
        required: true
    },
    experience: {
        type: Number,
        required: true
    },
    created_events: {
        type: Array,
        default: []
    },
    events: {
        type: Array,
        default: []
    },
	create_date: {
		type: Date,
		default: Date.now
	}
});

var User = module.exports = mongoose.model('User', userSchema);

// Get Users
module.exports.getUsers = function(callback, limit) {
	User.find(callback).limit(limit);
}

module.exports.getUser = function (id, callback) {
    User.findOne({ 'user_id': id }, callback);
}

// Add User
module.exports.addUser = function(user, callback) {
	User.create(user, callback);
}

// Update User
module.exports.updateUser = function(old_id, created_event, options, callback) {
    var query = { user_id: old_id };

    User.findOne(query, function (err, user) {
        if (err)
        {
            console.log(err);
        }
        else
        {
            user.created_events.push(created_event.created_event);
            user.markModified('object');
            user.save(callback);
        }
    });
}

// Delete User
module.exports.removeUser = function(id, callback) {
	var query = {_id: id};
	User.remove(query, callback);
}
