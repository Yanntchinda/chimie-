package com.yanntchinda.centrenotifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Traite le bouton « Effacer » des cartes du centre. */
public class ActionReceiver extends BroadcastReceiver {

    static final String ACTION_CLEAR = "com.yanntchinda.centrenotifications.action.CLEAR";
    static final String EXTRA_KEY = "key";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_CLEAR.equals(intent.getAction())) return;
        String key = intent.getStringExtra(EXTRA_KEY);
        if (key == null) return;
        RecentStore.remove(key);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(key, 1);
        }
        // Retire aussi la notification d'origine restée sur le téléphone.
        NotificationRelay.cancelOriginal(key);
    }
}
