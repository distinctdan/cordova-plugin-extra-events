interface ICdvWindowFocusChangedEvent {
    eventName: 'android:onWindowFocusChanged';
    hasFocus: boolean;
}

type CdvExtraEvent = ICdvWindowFocusChangedEvent;

interface Window {
    plugins: {
        ExtraEvents: {
            registerForEvents: (listener: (event: CdvExtraEvent) => void,
                                error?: (err: any) => void
                ) => (() => void);
        }
    }
}
