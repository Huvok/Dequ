var mongoose = require('mongoose');

// Report Schema
var reportSchema = mongoose.Schema({
    user_id: {
        type: String,
        required: true
    },
    title: {
        type: String,
        required: true
    },
	type: {
		type: String, 
		required: true
	},
	level: {
		type: Number,
		required: true
    },
    description: {
        type: String,
        required: true
    },
    latitude: {
        type: Number,
        required: true
    },
    longitude: {
        type: Number,
        required: true
    },
    has_event: {
        type: Boolean,
        default: false
    },
	create_date: {
		type: Date,
		default: Date.now
	}
});

var Report = module.exports = mongoose.model('Report', reportSchema);

// Get Reports
module.exports.getReports = function(callback, limit) {
	Report.find(callback).limit(limit);
}

// Get Report by ID
module.exports.getReportById = function(id, callback) {
	Report.findById(id, callback);
}

// Add Report
module.exports.addReport = function(report, callback) {
	Report.create(report, callback);
}

// Update Report
module.exports.updateReport = function(id, report, options, callback) {
	var query = {_id: id};
    var update = {
        user_id: report.user_id,
        title: report.title,
        type: report.type,
        level: report.level,
        description: report.description,
        latitude: report.latitude,
        longitude: report.longitude,
<<<<<<< HEAD
=======
        create_date: report.create_date,
>>>>>>> d5d9a84ffb020f9759d3f4a35b8eafa432604734
        has_event: report.has_event
	}
	
	Report.findOneAndUpdate(query, update, options, callback);
}

// Delete Report
module.exports.removeReport = function(id, callback) {
	var query = {_id: id};
	Report.remove(query, callback);
}