package com.yanntchinda.centrenotifications;

import android.content.Context;
import android.content.SharedPreferences;

/** Réglages du centre, partagés entre l'écran de réglage et le service d'écoute. */
final class CenterPrefs {

    private static final String FILE = "centre-settings";
    private static final String KEY_REPLACE = "replace-originals";

    private CenterPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean replaceOriginals(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE, true);
    }

    static void setReplaceOriginals(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_REPLACE, value).apply();
    }
}
