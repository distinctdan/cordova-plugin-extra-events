interface ICdvEventWindowFocusChanged {
    eventName: 'android_onWindowFocusChanged';
    hasFocus: boolean;
}

interface ICdvEventAppWillResignActive {
    eventName: 'iOS_appWillResignActive';
}

interface ICdvEventAppWillEnterForeground {
    eventName: 'iOS_appWillEnterForeground';
}

type CdvExtraEvent = ICdvEventWindowFocusChanged
    | ICdvEventAppWillResignActive
    | ICdvEventAppWillEnterForeground;

interface Window {
    plugins: {
        ExtraEvents: {
            registerForEvents: (listener: (event: CdvExtraEvent) => void,
                                error?: (err: any) => void
                ) => (() => void);
        }
    }
}
