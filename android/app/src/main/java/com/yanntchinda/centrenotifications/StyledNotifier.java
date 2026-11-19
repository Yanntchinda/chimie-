package com.yanntchinda.centrenotifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import java.util.Date;

/**
 * Réécrit une notification du téléphone dans le style du centre : carte sombre,
 * métadonnées discrètes, action de réponse de l'application d'origine conservée.
 */
final class StyledNotifier {

    static final String CHANNEL_ID = "centre-messages";

    private StyledNotifier() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.notif_channel_description));
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);
        manager.createNotificationChannel(channel);
    }

    static boolean canPost(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return false;
        return manager.areNotificationsEnabled();
    }

    /**
     * Affiche la carte du centre pour une notification d'origine.
     * Renvoie false si rien n'a pu être affiché : l'originale ne doit alors pas être retirée.
     */
    static boolean post(Context context, StatusBarNotification sbn, String sender, String message) {
        if (sbn == null || message == null || message.isEmpty()) return false;
        if (!canPost(context)) return false;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return false;
        ensureChannel(context);

        String key = sbn.getKey();
        if (key == null) return false;

        Notification original = sbn.getNotification();
        long when = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();

        RemoteViews card = new RemoteViews(context.getPackageName(), R.layout.notification_card);
        card.setTextViewText(R.id.cardSender, sender);
        card.setTextViewText(R.id.cardMessage, message);
        card.setTextViewText(R.id.cardTime,
                DateFormat.getTimeFormat(context).format(new Date(when)));
        card.setImageViewResource(R.id.cardDot, dotFor(sbn.getPackageName()));

        Intent clear = new Intent(context, ActionReceiver.class)
                .setAction(ActionReceiver.ACTION_CLEAR)
                .putExtra(ActionReceiver.EXTRA_KEY, key);
        PendingIntent clearIntent = PendingIntent.getBroadcast(
                context,
                key.hashCode(),
                clear,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setWhen(when)
                .setShowWhen(true)
                .setContentTitle(sender)
                .setContentText(message)
                .setCustomContentView(card)
                .setCustomBigContentView(card)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(0xFF1B1E2B)
                .addAction(new Notification.Action.Builder(
                        null, context.getString(R.string.notif_action_clear), clearIntent).build());

        if (original != null) {
            if (original.category != null) builder.setCategory(original.category);
            if (original.getLargeIcon() != null) builder.setLargeIcon(original.getLargeIcon());
            if (original.contentIntent != null) builder.setContentIntent(original.contentIntent);
            Notification.Action reply = replyActionOf(original);
            if (reply != null) builder.addAction(reply);
        }

        try {
            manager.notify(key, 1, builder.build());
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    /** Reprend l'action de réponse de l'application d'origine : la réponse lui parvient directement. */
    private static Notification.Action replyActionOf(Notification original) {
        Notification.Action[] actions = original.actions;
        if (actions == null) return null;
        for (Notification.Action action : actions) {
            RemoteInput[] inputs = action.getRemoteInputs();
            if (inputs == null || inputs.length == 0) continue;
            for (RemoteInput input : inputs) {
                if (input.getAllowFreeFormInput()) return action;
            }
        }
        return null;
    }

    static int dotFor(String pkg) {
        String name = pkg == null ? "" : pkg.toLowerCase();
        if (name.contains("whatsapp") || name.contains("messaging") || name.contains("mms")) {
            return R.drawable.dot_green;
        }
        if (name.contains("telegram") || name.contains("signal") || name.contains("messenger")) {
            return R.drawable.dot_blue;
        }
        if (name.contains("gmail") || name.contains("mail") || name.contains("outlook")) {
            return R.drawable.dot_amber;
        }
        if (name.contains("instagram") || name.contains("snapchat")) {
            return R.drawable.dot_purple;
        }
        return R.drawable.dot_grey;
    }
}
