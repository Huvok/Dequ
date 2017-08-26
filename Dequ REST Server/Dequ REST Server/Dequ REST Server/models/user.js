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

// Add User
module.exports.addUser = function(user, callback) {
	User.create(user, callback);
}

// Update User
module.exports.updateUser = function(id, user, options, callback) {
	var query = {_id: id};
    var update = {
        user_id: user.user_id,
		name: user.name,
		lastname: user.lastname
	}

	User.findOneAndUpdate(query, update, options, callback);
}

// Delete User
module.exports.removeUser = function(id, callback) {
	var query = {_id: id};
	User.remove(query, callback);
}
