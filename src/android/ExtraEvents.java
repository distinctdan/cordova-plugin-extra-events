package org.apache.cordova.extraEvents;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;
import android.view.Window;

public class ExtraEvents extends CordovaPlugin {
    private CallbackContext eventsCtx;

    @Override
    protected void pluginInitialize() {
        // Intercept window events
        Window win = this.cordova.getActivity().getWindow();
        Window.Callback existingCallback = win.getCallback();

        win.setCallback(new CdvExtraEventsWindowCallback(existingCallback, this::onWindowFocusChanged));
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext ctx) {
        try {
            if (action.equals("registerForEvents")) {
                this.eventsCtx = ctx;
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                ctx.sendPluginResult(result);
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

    private void onWindowFocusChanged(boolean hasFocus) {
        if (this.eventsCtx != null) {
            try {
                JSONObject event = new JSONObject();
                event.put("hasFocus", hasFocus);
                event.put("eventName", "android_onWindowFocusChanged");

                PluginResult result = new PluginResult(PluginResult.Status.OK, event);
                result.setKeepCallback(true);
                this.eventsCtx.sendPluginResult(result);
            } catch (Exception e) {
                e.printStackTrace();
                this.fail(this.eventsCtx, "onWindowFocusChanged", e.toString(), true);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Utils
    //--------------------------------------------------------------------------

    // Sends an error back to JS
    private void fail(CallbackContext ctx, String method, String message) {
        this.fail(ctx, method, message, false);
    }

    private void fail(CallbackContext ctx, String method, String message, boolean keepCallback) {
        Log.e("ExtraEvents", method + " error - " +message);
        if (ctx != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,
                    "ExtraEvents " + method + " error - " + message);

            if (keepCallback) result.setKeepCallback(true);

            ctx.sendPluginResult(result);
        }
    }
}
