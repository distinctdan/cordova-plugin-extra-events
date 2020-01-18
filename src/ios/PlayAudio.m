#import "PlayAudio.h"

@implementation PlayAudio

bool hasRegisteredForEvents = false;
NSMutableDictionary* players;
NSString* eventCallbackId;

- (void)pluginInitialize
{
    players = [[NSMutableDictionary alloc] init];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(onInterruption:) name:AVAudioSessionInterruptionNotification object:nil];
}

- (void)dealloc
{

}

- (void)playSong:(CDVInvokedUrlCommand*)command
{
    dispatch_queue_t queue = dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0);
    dispatch_async(queue, ^{
        @try {
            NSError* err;
            NSDictionary* props = command.arguments[0];
            NSString* songId = props[@"songId"];
            NSString* songURL = props[@"songURL"];
            double offsetSecs = props[@"startOffset"] ? [props[@"startOffset"] doubleValue] : -1;
            float volume = props[@"volume"] ? [props[@"volume"] floatValue] : -1;
            float fadeInLen = props[@"fadeInLen"] ? [props[@"fadeInLen"] floatValue] : 0;

            // Create player for this song if we haven't already
            if (!players[songId]) {
                NSString* basePath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www"];
                NSString* pathFromWWW = [NSString stringWithFormat:@"%@/%@", basePath, songURL];

                if ([[NSFileManager defaultManager] fileExistsAtPath : pathFromWWW]) {
                    NSURL *pathURL = [NSURL fileURLWithPath : pathFromWWW];
                    players[songId] = [[AVAudioPlayer alloc] initWithContentsOfURL:pathURL error:&err];

                    if (err) {
                        NSString* msg = [NSString stringWithFormat:@"Player init error: %ld - %@", (long)err.code, err.description];
                        [self onErrorWithMethodName:@"playSong" msg:msg  callbackId:command.callbackId];
                        return;
                    }
                } else {
                    NSString* msg = [NSString stringWithFormat:@"Song not found at path: %@", pathFromWWW];
                    [self onErrorWithMethodName:@"playSong" msg:msg callbackId:command.callbackId];
                    return;
                }
            }

            AVAudioPlayer* player = players[songId];
            if (fadeInLen > 0) {
                player.volume = 0;
            } else if (volume != -1) {
                player.volume = fmaxf(0, fminf(1, volume));
            }

            [player setDelegate:self];
            if (offsetSecs != -1) {
                player.currentTime = offsetSecs;
            }
            bool success = [player play];

            if (volume != -1) {
                volume = fmaxf(0, fminf(1, volume));
                [player setVolume:volume fadeDuration:fadeInLen];
            }
            if (success) {
                CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            } else {
                [self onErrorWithMethodName:@"playSong" msg:@"playing failed" callbackId:command.callbackId];
                return;
            }
        } @catch (NSException *exception) {
            [self onErrorWithMethodName:@"playSong" exception:exception callbackId:command.callbackId];
        }
    });

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)pauseSongs:(CDVInvokedUrlCommand*)command
{
    dispatch_queue_t queue = dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0);
    dispatch_async(queue, ^{
        @try {
            NSArray* songIds = command.arguments[0];
            for (NSString* songId in songIds) {
                AVAudioPlayer* player = players[songId];
                if (player) {
                    [player pause];
                } else {
//                    NSLog(@"PlayAudio pauseSongs - no player found for songId \"%@\", ignoring.", songId);
                }
            }

            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

        } @catch (NSException *exception) {
            [self onErrorWithMethodName:@"pauseSongs" exception:exception callbackId:command.callbackId];
        }
    });

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setVolumes:(CDVInvokedUrlCommand*)command
{
    @try {
        NSArray* volOptions = command.arguments[0];
        for (NSDictionary* volOption in volOptions) {
            NSString* songId = volOption[@"songId"];
            AVAudioPlayer* player = players[songId];
            if (player) {
                float vol = fmaxf(0, fminf(1, [volOption[@"volume"] floatValue]));
                [player setVolume:vol];
            } else {
//                NSLog(@"PlayAudio setVolumes - no player found for songId \"%@\", ignoring.", songId);
            }
        }

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } @catch (NSException *exception) {
       [self onErrorWithMethodName:@"setVolumes" exception:exception callbackId:command.callbackId];
   }
}

- (void)registerForEvents:(CDVInvokedUrlCommand*)command
{
    @try {
        hasRegisteredForEvents = true;
        eventCallbackId = command.callbackId;

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } @catch (NSException *exception) {
        [self onErrorWithMethodName:@"registerForEvents" exception:exception callbackId:command.callbackId];
   }
}

// ----------------------------------------------------
//                  Delegate Methods
// ----------------------------------------------------
- (void) audioPlayerDidFinishPlaying:(AVAudioPlayer*)player successfully:(BOOL)success
{
    if (hasRegisteredForEvents) {
        // Find the player that finished so we can send its songId.
        for (NSString* songId in players) {
            if (players[songId] == player) {
                NSMutableDictionary* props = [NSMutableDictionary dictionaryWithCapacity:2];
                props[@"eventName"] = @"SongEnded";
                props[@"songId"] = songId;

                CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:props];
                [result setKeepCallbackAsBool:true];
                [self.commandDelegate sendPluginResult:result callbackId:eventCallbackId];
                return;
            }
        }

        NSLog(@"PlayAudio ERROR: couldn't find player that supposedly just finished a song");
    }
}

- (void)onInterruption:(NSNotification*)notification
{
    NSLog(@"PlayAudio Interrupt. notification: %@", notification);

    if (hasRegisteredForEvents) {
        NSMutableDictionary* props = [NSMutableDictionary dictionaryWithCapacity:2];

        int interruptionType = (int)[[notification.userInfo valueForKey:AVAudioSessionInterruptionTypeKey] integerValue];
        if (interruptionType == AVAudioSessionInterruptionTypeBegan) {
            props[@"eventName"] = @"AudioInterruptionBegan";
        } else if (interruptionType == AVAudioSessionInterruptionTypeEnded) {
            props[@"eventName"] = @"AudioInterruptionEnded";
        }

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:props];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:eventCallbackId];
        return;
    }
}

// ----------------------------------------------------
//                  Error Handling
// ----------------------------------------------------
- (void) onErrorWithMethodName:(NSString*)method
                     exception:(NSException*)e
                    callbackId:(NSString*)callbackId
{
    NSString* msg = [NSString stringWithFormat:@"%@ - %@", e.name, e.reason];
    [self onErrorWithMethodName:method msg:msg callbackId:callbackId];
}

- (void) onErrorWithMethodName:(NSString*)method
                           msg:(NSString*)msg
                    callbackId:(NSString*)callbackId
{
    NSString* err = [NSString stringWithFormat:@"PlayAudio - %@ ERROR: %@", method, msg];
    NSLog(@"%@", err);

    if (callbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:err];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

@end
