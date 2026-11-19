package com.yanntchinda.centrenotifications;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Ecoute les notifications reelles du telephone et les transmet au centre.
 *
 * Aucune permission reseau n'est demandee : les evenements viennent du systeme
 * Android et restent dans l'application.
 */
public class NotificationRelay extends NotificationListenerService {

    /** Notifications recentes, pour repondre et effacer a la bonne cible. */
    private static final Map<String, Notification> RECENT = new HashMap<>();
    private static final Map<String, String> LAST_TEXT = new HashMap<>();
    private static final int RECENT_LIMIT = 120;

    private static NotificationRelay instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CenterBridge.setContext(getApplicationContext());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        instance = this;
        CenterBridge.setContext(getApplicationContext());
        StatusBarNotification[] active = null;
        try {
            active = getActiveNotifications();
        } catch (Exception ignored) {
            // Acces retire entre-temps : rien a rattraper.
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
        instance = this;
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

            String title = textOf(extras.getCharSequence(Notification.EXTRA_TITLE));
            String text = textOf(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
            if (text.isEmpty()) text = textOf(extras.getCharSequence(Notification.EXTRA_TEXT));
            if (text.isEmpty()) text = textOf(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT));
            if (text.isEmpty()) return;

            String key = sbn.getKey();
            if (key == null) return;
            if (text.equals(LAST_TEXT.get(key))) return;
            LAST_TEXT.put(key, text);

            remember(key, notification);

            JSONObject event = new JSONObject();
            event.put("key", key);
            event.put("pkg", sbn.getPackageName());
            event.put("app", labelOf(sbn.getPackageName()));
            event.put("title", title.isEmpty() ? labelOf(sbn.getPackageName()) : title);
            event.put("text", text);
            event.put("at", sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis());
            event.put("canReply", hasReplyAction(notification));

            CenterBridge.publish(event);
        } catch (Exception ignored) {
            // Une notification mal formee ne doit jamais interrompre l'ecoute.
        }
    }

    private static void remember(String key, Notification notification) {
        if (RECENT.size() > RECENT_LIMIT) {
            RECENT.clear();
            LAST_TEXT.clear();
        }
        RECENT.put(key, notification);
    }

    private static String textOf(CharSequence value) {
        if (value == null) return "";
        return value.toString().trim();
    }

    private String labelOf(String pkg) {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            CharSequence label = pm.getApplicationLabel(info);
            if (label != null && label.length() > 0) return label.toString();
        } catch (Exception ignored) {
            // Application inconnue : on garde le nom technique.
        }
        return pkg;
    }

    private static boolean hasReplyAction(Notification notification) {
        Notification.Action[] actions = notification.actions;
        if (actions == null) return false;
        for (Notification.Action action : actions) {
            android.app.RemoteInput[] inputs = action.getRemoteInputs();
            if (inputs == null) continue;
            for (android.app.RemoteInput input : inputs) {
                if (input.getAllowFreeFormInput()) return true;
            }
        }
        return false;
    }

    /* ------------------------------------------------------------------ *
     *  Actions demandees par la page
     * ------------------------------------------------------------------ */

    static void cancel(String key) {
        NotificationRelay relay = instance;
        if (relay == null || key == null) return;
        try {
            relay.cancelNotification(key);
        } catch (Exception ignored) {
            // Notification deja retiree par le systeme.
        }
    }

    static String reply(String key, String text) {
        Notification notification = RECENT.get(key);
        if (notification == null || text == null || text.trim().isEmpty()) return "indisponible";

        Notification.Action action = null;
        android.app.RemoteInput input = null;
        Notification.Action[] actions = notification.actions;
        if (actions != null) {
            for (Notification.Action candidate : actions) {
                android.app.RemoteInput[] inputs = candidate.getRemoteInputs();
                if (inputs == null) continue;
                for (android.app.RemoteInput remote : inputs) {
                    if (remote.getAllowFreeFormInput()) {
                        action = candidate;
                        input = remote;
                        break;
                    }
                }
                if (action != null) break;
            }
        }
        if (action == null || input == null) return "indisponible";

        Context context = CenterBridge.context();
        if (context == null) return "echec";

        try {
            Intent fillIn = new Intent();
            Bundle results = new Bundle();
            results.putCharSequence(input.getResultKey(), text.trim());
            android.app.RemoteInput.addResultsToIntent(new android.app.RemoteInput[]{ input }, fillIn, results);
            action.actionIntent.send(context, 0, fillIn);
            return "ok";
        } catch (Exception err) {
            return "echec";
        }
    }
}
