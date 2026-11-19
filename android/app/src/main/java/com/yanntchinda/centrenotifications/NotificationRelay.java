package com.yanntchinda.centrenotifications;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

/**
 * Écoute les notifications du téléphone et les réécrit dans le style du centre.
 *
 * Aucune permission réseau n'est demandée : les messages sont lus sur l'appareil,
 * affichés à leur place dans les notifications, et rien n'en sort.
 */
public class NotificationRelay extends NotificationListenerService {

    /** Dernier texte transmis pour une notification, pour éviter les doublons. */
    private static final Map<String, String> LAST_TEXT = new HashMap<>();
    private static final int LAST_TEXT_LIMIT = 200;

    private static NotificationRelay instance;

    private void rememberInstance() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rememberInstance();
        StyledNotifier.ensureChannel(getApplicationContext());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        rememberInstance();
        StatusBarNotification[] active = null;
        try {
            active = getActiveNotifications();
        } catch (Exception ignored) {
            // Accès retiré entre-temps : rien à rattraper.
        }
        if (active != null) {
            for (StatusBarNotification sbn : active) {
                forward(sbn);
            }
        }
    }

    @Override
    public void onListenerDisconnected() {
        instance = null;
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        rememberInstance();
        forward(sbn);
    }

    private void forward(StatusBarNotification sbn) {
        try {
            Notification notification = sbn.getNotification();
            if (notification == null) return;
            if (getPackageName().equals(sbn.getPackageName())) return;
            if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) return;

            Bundle extras = notification.extras;
            if (extras == null) return;

            String sender = textOf(extras.getCharSequence(Notification.EXTRA_TITLE));
            String message = textOf(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
            if (message.isEmpty()) message = textOf(extras.getCharSequence(Notification.EXTRA_TEXT));
            if (message.isEmpty()) message = textOf(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT));
            if (message.isEmpty()) return;
            if (sender.isEmpty()) sender = labelOf(sbn.getPackageName());

            String key = sbn.getKey();
            if (key == null) return;
            if (message.equals(LAST_TEXT.get(key))) return;
            if (LAST_TEXT.size() > LAST_TEXT_LIMIT) LAST_TEXT.clear();
            LAST_TEXT.put(key, message);

            CenterPrefs.noteReceived(getApplicationContext());
            boolean posted = StyledNotifier.post(getApplicationContext(), sbn, sender, message);
            if (posted) {
                IslandOverlay.show(getApplicationContext(), sender, message,
                        StyledNotifier.dotFor(sbn.getPackageName()));
            }
            if (posted && CenterPrefs.replaceOriginals(this)) {
                // Une seule carte reste dans les notifications : celle du centre.
                try {
                    cancelNotification(key);
                } catch (Exception ignored) {
                    // Notification déjà retirée par le système.
                }
            }
        } catch (Exception ignored) {
            // Une notification mal formée ne doit jamais interrompre l'écoute.
        }
    }

    private static String textOf(CharSequence value) {
        if (value == null) return "";
        return value.toString().trim();
    }

    private String labelOf(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            CharSequence label = pm.getApplicationLabel(info);
            if (label != null && label.length() > 0) return label.toString();
        } catch (Exception ignored) {
            // Application inconnue : on garde le nom technique.
        }
        return pkg;
    }

    /** Retire la notification d'origine, si le service est encore connecté. */
    static void cancelOriginal(String key) {
        NotificationRelay relay = instance;
        if (relay == null || key == null) return;
        try {
            relay.cancelNotification(key);
        } catch (Exception ignored) {
            // Déjà retirée.
        }
    }
}
