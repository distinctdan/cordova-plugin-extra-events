
## Installation
    cordova plugin add @spoonconsulting/cordova-plugin-extra-events
Tested platforms: Android 8+, iOS 11+
    
## Usage
This plugin provides access to extra lifecycle events that are useful for saving data before your app quits. This allows you to mostly work around the following issues with cordova:
- iOS: The `onPause` event is never fired if the user activates the app switcher, then swipes up to kill the app.
- Android: The `onPause` event is never fired if the user actives the "recent apps" switcher, then swipes to kill the app.

```
constructor() {
    this.unregister = window.plugins.ExtraEvents.registerForEvents(this.onPluginEvent, (err) => {
        console.error(err);
    });
}

private onPluginEvent(e: CdvExtraEvent) {
    // Save our state when the app starts to go to the background.
    switch (e.eventName) {
        case 'android_onWindowFocusChanged':
            if (!e.hasFocus) {
                this.saveState();
            }
            break;
        case 'iOS_appWillResignActive':
            this.saveState();
            break;
    }
}
```

## API and Events
    
### window.plugins.ExtraEvents.registerForEvents(eventCallback, error?)
Registers a callback function to receive events. Returns an unregister function. Can be called with the following events:

#### android_onWindowFocusChanged (Android only)
Emitted when the main activity gains or loses focus. This happens when your app launches, goes into the background, is interrupted by a dialog or alert, when the user opens the system UI, etc. See the android docs for more info: https://developer.android.com/reference/android/app/Activity.html#onWindowFocusChanged(boolean).

**Warning** If the user actives the app switcher quickly and immediately kills your app, this event might not have time to fire. Also, if the user kills the app and your callback is taking a long time to run, Android might kill it, the same way that this happens with the native `onStop` and `onDestroy` methods.
```
{
    eventName: 'android_onWindowFocusChanged',
    hasFocus: boolean,
}
```

#### iOS_appWillResignActive (iOS only)
Emitted when the app starts going to the background or when the app switcher is triggered. See the native docs for more info: https://developer.apple.com/documentation/uikit/uiapplicationdelegate/1622950-applicationwillresignactive?language=objc.

```
{
    eventName: 'iOS_appWillResignActive',
}
```

#### iOS_appWillEnterForeground (iOS only)
Emitted as the app transitions from background to foreground. See the native docs for more info: https://developer.apple.com/documentation/uikit/uiapplicationdelegate/1623076-applicationwillenterforeground?language=objc.

```
{
    eventName: 'iOS_appWillEnterForeground',
}
```
