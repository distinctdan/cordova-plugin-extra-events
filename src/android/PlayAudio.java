package org.apache.cordova.playAudio;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class PlayAudio extends CordovaPlugin {
    private CallbackContext eventsCtx;
    private ConcurrentHashMap<String, Timer> fadeTimers = new ConcurrentHashMap<String, Timer>();
    private ConcurrentHashMap<String, MediaPlayer> players = new ConcurrentHashMap<String, MediaPlayer>();

    @Override
    protected void pluginInitialize() {

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext ctx) {
        try {
            if (action.equals("playSong")) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        playSong(args, ctx);
                    }
                });
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                ctx.sendPluginResult(result);
                return true;
            }
            else if (action.equals("pauseSongs")) {
                this.pauseSongs(args, ctx);
                return true;
            }
            else if (action.equals("setVolumes")) {
                this.setVolumes(args, ctx);
                return true;
            }
            else if (action.equals("registerForEvents")) {
                this.registerForEvents(args, ctx);
                return true;
            }
            else {
                this.fail(ctx, "execute", "Invalid action: " + action);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.fail(ctx,"execute", e.toString());
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // AUDIO METHODS
    //--------------------------------------------------------------------------
    private synchronized void playSong(JSONArray args, CallbackContext ctx) {
        try {
            JSONObject props = args.getJSONObject(0);
            String songId = props.getString("songId");
            String songURL = props.optString("songURL", null);
            double startOffset = props.optDouble("startOffset", -1);
            double volume = props.optDouble("volume", -1);
            double fadeInLen = props.optDouble("fadeInLen", 0);
            boolean hasVolume = volume != -1;
            volume = Math.min(1.0, Math.max(0.0, volume));

            if (fadeInLen > 0 && !hasVolume) {
                this.fail(ctx, "playSong", "ERROR: when fading in, you must pass 'volume'.");
                return;
            }

            String fullPath = "www/" + songURL;

            Context appContext = cordova.getActivity().getApplicationContext();
            AssetManager am = appContext.getResources().getAssets();
            AssetFileDescriptor afd = am.openFd(fullPath);

            MediaPlayer player = this.players.get(songId);
            // Create new player if we don't have a cached one.
            if (player == null) {
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();

                player.setOnCompletionListener((MediaPlayer p) -> {
                    onSongComplete(songId);
                });

                this.players.put(songId, player);
            }
            else {
                // Cancel existing timer if necessary
                Timer timer = this.fadeTimers.get(songId);
                if (timer != null) timer.cancel();
            }

            if (startOffset != -1) {
                if (Build.VERSION.SDK_INT >= 26) {
                    player.seekTo((long) (startOffset * 1000), MediaPlayer.SEEK_CLOSEST);
                } else {
                    player.seekTo((int) (startOffset * 1000));
                }
            }

            // If fading in, start volume at 0.
            double startVolume = volume;
            if (fadeInLen > 0) startVolume = 0.0;

            if (hasVolume) player.setVolume((float)startVolume, (float)startVolume);

            player.start();

            // Start a timer to do the fade
            if (fadeInLen > 0) {
                // Cancel existing timer for this player
                Timer oldTimer = this.fadeTimers.get(songId);
                if (oldTimer != null) oldTimer.cancel();

                // Make everything final because timers only work with final.
                final Timer timer = new Timer();
                final long startTime = System.currentTimeMillis();
                final long fadeInLenMs = (long)(fadeInLen*1000);
                final double startVolFinal = startVolume;
                final double endVolFinal = volume;
                final MediaPlayer playerFinal = player;

                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        double t = (System.currentTimeMillis() - startTime)/(double)fadeInLenMs;
                        double clippedT = Math.min(1.0, Math.max(0.0, t));

                        // Do lerp
                        double vol = (startVolFinal * (1.0 - clippedT)) + (endVolFinal * clippedT);
                        playerFinal.setVolume((float)vol, (float)vol);

                        if (t >= 1) {
                            timer.cancel();
                        }
                    }
                }, 0, 1000/60);
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            ctx.sendPluginResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            this.fail(ctx, "playSong", e.toString());
        }
    }

    private synchronized void pauseSongs(JSONArray args, CallbackContext ctx) {
        try {
            JSONArray songIds = args.getJSONArray(0);

            int len = songIds.length();
            for (int i = 0; i < len; i++) {
                String songId = songIds.getString(i);

                MediaPlayer player = this.players.get(songId);
                if (player != null) {
                    Timer timer = this.fadeTimers.get(songId);
                    if (timer != null) timer.cancel();

                    player.pause();
                }
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            ctx.sendPluginResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            this.fail(ctx, "pauseSongs", e.toString());
        }
    }

    private synchronized void setVolumes(JSONArray args, CallbackContext ctx) {
        try {
            JSONArray volObjs = args.getJSONArray(0);

            int len = volObjs.length();
            for (int i = 0; i < len; i++) {
                JSONObject volObj = volObjs.getJSONObject(i);
                String songId = volObj.getString("songId");
                double volume = Math.min(1.0, Math.max(0.0, volObj.getDouble("volume")));

                MediaPlayer player = this.players.get(songId);
                if (player != null) {
                    // Cancel existing fade timer because we're setting the volume.
                    Timer timer = this.fadeTimers.get(songId);
                    if (timer != null) timer.cancel();

                    player.setVolume((float)volume, (float)volume);
                }
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            ctx.sendPluginResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            this.fail(ctx, "setVolumes", e.toString());
        }
    }

    private synchronized void registerForEvents(JSONArray args, CallbackContext ctx) {
        try {
            this.eventsCtx = ctx;

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            ctx.sendPluginResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            this.fail(ctx, "registerForEvents", e.toString());
        }
    }

    private void onSongComplete(String songId) {
        if (this.eventsCtx != null) {
            try {
                JSONObject event = new JSONObject();
                event.put("songId", songId);
                event.put("eventName", "SongEnded");

                PluginResult result = new PluginResult(PluginResult.Status.OK, event);
                result.setKeepCallback(true);
                this.eventsCtx.sendPluginResult(result);
            } catch (Exception e) {
                e.printStackTrace();
                this.fail(this.eventsCtx, "onSongComplete", e.toString(), true);
            }
        }
    }

    //--------------------------------------------------------------------------
    // LIFECYCLE METHODS
    //--------------------------------------------------------------------------
    public synchronized void onDestroy() {
        this.fadeTimers.forEach((String songId, Timer timer) -> {
            timer.cancel();
        });
    }

    @Override
    public synchronized void onReset() {
        this.fadeTimers.forEach((String songId, Timer timer) -> {
            timer.cancel();
        });

        this.players.forEach((String songId, MediaPlayer player) -> {
            player.pause();
        });
    }

    // Sends an error back to JS
    private void fail(CallbackContext ctx, String method, String message) {
        this.fail(ctx, method, message, false);
    }

    private void fail(CallbackContext ctx, String method, String message, boolean keepCallback) {
        Log.e("PlayAudio", method + " error - " +message);
        if (ctx != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,
                    "PlayAudio " + method + " error - " + message);

            if (keepCallback) result.setKeepCallback(true);

            ctx.sendPluginResult(result);
        }
    }
}
