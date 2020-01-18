var exec = require('cordova/exec');

var errorTag = 'ExtraEvents ERROR: ';
var isListeningForEvents = false;
var listeners = [];

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
        }, 'ExtraEvents', 'registerForEvents', []);
    }
    
    // Return unregister function - doesn't currently stop plugin callbacks. Can be added if needed.
    return function() {
        var i = listeners.indexOf(listener);
        if (i !== -1) listeners.splice(i, 1);
    }
}