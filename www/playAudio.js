var exec = require('cordova/exec');

var errorTag = 'PlayAudio ERROR: ';
var isListeningForEvents = false;
var listeners = [];

exports.playSong = function (playOptions, success, error) {
    // Do basic validation
    if (!playOptions.songId) {
        var msg = errorTag + 'songId is a required property.';
        console.error(msg);
        if (error) error(msg);
        return;
    }
    
    exec(success, error, "PlayAudio", "playSong", [playOptions]);
};

exports.pauseSongs = function (songIds, success, error) {
    // Do basic validation
    if (!songIds || !songIds.length) {
        var msg = errorTag + 'expected an array of song ids.';
        console.error(msg);
        if (error) error(msg);
        return;
    }
    
    exec(success, error, "PlayAudio", "pauseSongs", [songIds]);
};

exports.setVolumes = function (volOptions, success, error) {
    // Do basic validation
    if (!volOptions || !volOptions.length) {
        var msg = errorTag + 'expected an array of volume options.';
        console.error(msg);
        if (error) error(msg);
        return;
    }
    
    exec(success, error, "PlayAudio", "setVolumes", [volOptions]);
};

exports.registerForEvents = function(listener, error) {
    if (typeof listener !== 'function') throw errorTag + 'expected a function as a listener.';
    
    listeners.push(listener);
    
    // If we haven't told the plugin to send us events yet, register now.
    if (!isListeningForEvents) {
        isListeningForEvents = true;
        exec(function(e) {
            // We got an event, call the listeners.
            for (var i = 0; i < listeners.length; i++) {
                listeners[i](e);
            }
        }, function(e) {
            var msg = errorTag + 'Registering for plugin events failed: ' + e;
            console.error(msg);
            if (error) error(msg);
        }, 'PlayAudio', 'registerForEvents', []);
    }
    
    // Return unregister function
    return function() {
        var i = listeners.indexOf(listener);
        if (i !== -1) listeners.splice(i, 1);
    }
}