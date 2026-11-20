package com.yanntchinda.centrenotifications;

import android.content.Context;
import android.content.SharedPreferences;

/** Réglages du centre, partagés entre l'écran de réglage et le service d'écoute. */
final class CenterPrefs {

    private static final String FILE = "centre-settings";
    private static final String KEY_REPLACE = "replace-originals";
    private static final String KEY_COUNT = "received-count";
    private static final String KEY_LAST = "received-last";
    private static final String KEY_POSTED = "posted-count";
    private static final String KEY_STATUS = "last-status";
    private static final String KEY_CONNECTED = "listener-connected";
    private static final String KEY_WAKE = "show-on-wake";

    private CenterPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean showOnWake(Context context) {
        return prefs(context).getBoolean(KEY_WAKE, true);
    }

    static void setShowOnWake(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_WAKE, value).apply();
    }

    static boolean replaceOriginals(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE, true);
    }

    static void setReplaceOriginals(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_REPLACE, value).apply();
    }

    /** Trace le passage d'un message : sert a verifier que l'ecoute fonctionne. */
    static void noteReceived(Context context) {
        SharedPreferences store = prefs(context);
        store.edit()
                .putInt(KEY_COUNT, store.getInt(KEY_COUNT, 0) + 1)
                .putLong(KEY_LAST, System.currentTimeMillis())
                .apply();
    }

    static int receivedCount(Context context) {
        return prefs(context).getInt(KEY_COUNT, 0);
    }

    static long receivedLast(Context context) {
        return prefs(context).getLong(KEY_LAST, 0L);
    }

    /** Enregistre le resultat de la publication d'une carte. */
    static void notePosted(Context context, String status) {
        SharedPreferences store = prefs(context);
        SharedPreferences.Editor editor = store.edit().putString(KEY_STATUS, status);
        if ("ok".equals(status)) {
            editor.putInt(KEY_POSTED, store.getInt(KEY_POSTED, 0) + 1);
        }
        editor.apply();
    }

    static int postedCount(Context context) {
        return prefs(context).getInt(KEY_POSTED, 0);
    }

    static void noteConnected(Context context) {
        prefs(context).edit().putLong(KEY_CONNECTED, System.currentTimeMillis()).apply();
    }

    static long connectedAt(Context context) {
        return prefs(context).getLong(KEY_CONNECTED, 0L);
    }

    static String lastStatus(Context context) {
        return prefs(context).getString(KEY_STATUS, "");
    }
}
