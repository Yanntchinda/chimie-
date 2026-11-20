package com.yanntchinda.centrenotifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Passerelle entre les notifications reelles du telephone et la page du centre.
 *
 * Les evenements arrivent soit directement dans la page quand elle est ouverte,
 * soit dans une file locale (SharedPreferences) quand l'application est fermee,
 * file que la page vide a son demarrage.
 */
public class CenterBridge {

    /** Receveur d'evenements cote page : l'activite le branche sur la WebView. */
    public interface Sink {
        void deliver(String jsonArray);
    }

    private static final String PREFS = "centre-notifications";
    private static final String KEY_PENDING = "pending";
    private static final int LIMIT = 80;

    private static CenterBridge active;
    private static Context appContext;

    private final Context context;
    private Sink sink;

    public CenterBridge(Context context) {
        this.context = context.getApplicationContext();
        appContext = this.context;
        active = this;
    }

    static void setContext(Context context) {
        if (context != null && appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    static Context context() {
        return appContext;
    }

    void setSink(Sink sink) {
        this.sink = sink;
    }

    /**
     * Envoie un evenement a la page si elle est ouverte, sinon le met en attente.
     * Appele depuis le service d'ecoute des notifications.
     */
    static void publish(JSONObject event) {
        if (event == null) return;
        JSONArray single = new JSONArray();
        single.put(event);

        CenterBridge bridge = active;
        if (bridge != null && bridge.sink != null) {
            bridge.sink.deliver(single.toString());
            return;
        }

        Context ctx = appContext;
        if (ctx == null) return;

        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        JSONArray queue;
        try {
            queue = new JSONArray(prefs.getString(KEY_PENDING, "[]"));
        } catch (JSONException err) {
            queue = new JSONArray();
        }
        queue.put(event);
        while (queue.length() > LIMIT) {
            queue.remove(0);
        }
        prefs.edit().putString(KEY_PENDING, queue.toString()).apply();
    }

    /** Vide la file des notifications recues pendant que l'application etait fermee. */
    @JavascriptInterface
    public String pull() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_PENDING, "[]");
        prefs.edit().putString(KEY_PENDING, "[]").apply();
        if (raw == null || raw.isEmpty()) return "[]";
        return raw;
    }

    /** L'acces aux notifications est-il accorde dans les reglages Android ? */
    @JavascriptInterface
    public boolean listening() {
        return isEnabled(context);
    }

    /** Ouvre l'ecran Android ou l'on autorise l'acces aux notifications. */
    @JavascriptInterface
    public void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
            // Certains appareils n'exposent pas cet ecran : rien de plus a faire ici.
        }
    }

    /** Retire la notification d'origine quand la carte est effacee dans le centre. */
    @JavascriptInterface
    public void cancel(String key) {
        NotificationRelay.cancel(key);
    }

    /** Repond dans l'application d'origine. Renvoie ok, indisponible ou echec. */
    @JavascriptInterface
    public String reply(String key, String text) {
        return NotificationRelay.reply(key, text);
    }

    static boolean isEnabled(Context context) {
        if (context == null) return false;
        try {
            String enabled = Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners");
            return enabled != null && enabled.contains(context.getPackageName());
        } catch (Exception err) {
            return false;
        }
    }
}
