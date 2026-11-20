package com.yanntchinda.centrenotifications;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
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
        CenterPrefs.noteConnected(getApplicationContext());
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
        // Android ou le constructeur a coupé l'écoute : on demande tout de suite à être reconnecté.
        try {
            requestRebind(new ComponentName(this, NotificationRelay.class));
        } catch (Exception ignored) {
            // Accès retiré par l'utilisateur : rien à reconnecter.
        }
    }

    static boolean isConnected() {
        return instance != null;
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
            if (isUntouchable(sbn.getPackageName(), notification)) return;

            Bundle extras = notification.extras;
            if (extras == null) return;

            String sender = textOf(extras.getCharSequence(Notification.EXTRA_TITLE));
            String message = textOf(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
            if (message.isEmpty()) message = textOf(extras.getCharSequence(Notification.EXTRA_TEXT));
            if (message.isEmpty()) message = textOf(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT));
            if (message.isEmpty()) return;
            String appLabel = labelOf(sbn.getPackageName());
            if (sender.isEmpty()) sender = appLabel;

            String key = sbn.getKey();
            if (key == null) return;
            if (message.equals(LAST_TEXT.get(key))) return;
            if (LAST_TEXT.size() > LAST_TEXT_LIMIT) LAST_TEXT.clear();
            LAST_TEXT.put(key, message);

            Context app = getApplicationContext();
            CenterPrefs.noteReceived(app);
            boolean wake = LockScreenActivity.deviceIsLockedOrOff(app);
            String status = StyledNotifier.post(app, sbn, sender, message, appLabel, wake);
            CenterPrefs.notePosted(app, status);
            if ("ok".equals(status)) {
                long when = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();
                present(app, new RecentStore.Item(key, sbn.getPackageName(), appLabel, sender, message, when,
                        StyledNotifier.dotFor(sbn.getPackageName()), notification.contentIntent), wake);
            }
            if ("ok".equals(status) && CenterPrefs.replaceOriginals(this)) {
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

    /**
     * Écran verrouillé ou éteint : l'écran verrouillé du centre s'allume et se superpose.
     * Écran allumé et déverrouillé : l'îlot descend du haut de l'écran.
     */
    static void present(Context context, RecentStore.Item item, boolean wake) {
        RecentStore.put(item);
        if (wake) {
            LockScreenActivity.launch(context);
        } else {
            IslandOverlay.show(context, item.sender, item.message, item.dotRes, item.open);
        }
    }

    /**
     * Notifications qu'on ne doit jamais retirer ni réécrire : un appel entrant perdrait ses boutons
     * Répondre/Refuser, une alarme ne sonnerait plus, un téléchargement ou un trajet GPS disparaîtrait.
     */
    private static boolean isUntouchable(String pkg, Notification notification) {
        if ("android".equals(pkg) || "com.android.systemui".equals(pkg)) return true;
        String category = notification.category;
        if (category == null) return false;
        switch (category) {
            case Notification.CATEGORY_CALL:
            case Notification.CATEGORY_ALARM:
            case Notification.CATEGORY_NAVIGATION:
            case Notification.CATEGORY_PROGRESS:
            case Notification.CATEGORY_SERVICE:
            case Notification.CATEGORY_STATUS:
            case Notification.CATEGORY_SYSTEM:
            case Notification.CATEGORY_TRANSPORT:
            case Notification.CATEGORY_STOPWATCH:
                return true;
            default:
                return false;
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
