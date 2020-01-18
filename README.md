
## Installation
    cordova plugin add https://github.com/distinctdan/cordova-plugin-extra-events.git
Tested platforms: Android 8+, iOS 11+
    
## Usage
This plugin provides access to extra lifecycle events that are useful for saving data before your app quits. This allows you to mostly work around the following issues with cordova:
- iOS: The `onPause` event is never fired if the user activates the app switcher, then swipes up to kill the app.
- Android: The `onPause` event is never fired if the user actives the "recent apps" switcher, then swipes to kill the app.
    
### window.plugins.ExtraEvents.registerForEvents(eventCallback, error?)
Registers a callback function to receive events. Returns an unregister function. Can be called with the following events:

#### onWindowFocusChanged (Android only)
This is called whenever your main activity gains or loses focus. This happens when your app launches, goes into the background, is interrupted by a dialog or alert, when the user opens the system UI, etc. See the android docs for more info: https://developer.android.com/reference/android/app/Activity.html#onWindowFocusChanged(boolean).

**Warning** If the user actives the app switcher quickly and immediately kills your app, this event might not have time to fire. Also, if the user kills the app and your callback is taking a long time to run, Android might kill it, so try to keep it as short as possible.
```
{
    eventName: 'onWindowFocusChanged',
    hasFocus: boolean,
}
```
