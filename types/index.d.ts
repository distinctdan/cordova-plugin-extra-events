interface IPlayAudioPlayOptions {
    songId: string,
    songURL?: string,
    startOffset?: number,
    volume?: number;
    fadeInLen?: number,
}

interface IPlayAudioEvent {
    eventName: 'SongEnded';
    songId: string;
}

interface Window {
    plugins: {
        PlayAudio: {
            playSong: (playOptions: IPlayAudioPlayOptions,
                       success?: () => void,
                       error?: (err: any
                ) => void) => void;
            pauseSongs: (songIds: string[],
                         success?: () => void,
                         error?: (err: any) => void
                ) => void;
            setVolumes: (volOptions: {songId: string, volume: number}[],
                         success?: () => void,
                         error?: (err: any) => void
                ) => void;
            registerForEvents: (listener: (event: IPlayAudioEvent) => void,
                                error?: (err: any) => void
                ) => (() => void);
        }
    }
}
