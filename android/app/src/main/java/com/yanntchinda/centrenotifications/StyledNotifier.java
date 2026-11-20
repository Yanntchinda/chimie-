package com.yanntchinda.centrenotifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
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

    /**
     * Android fige le son et la vibration d'un canal à sa création : l'ancien canal
     * « centre-messages » était muet. On en crée un nouveau, avec son et vibration.
     */
    static final String CHANNEL_ID = "centre-messages-v2";
    private static final String LEGACY_CHANNEL_ID = "centre-messages";
    private static final long[] VIBRATION = {0, 40, 70, 40, 70, 90};

    private StyledNotifier() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (manager.getNotificationChannel(LEGACY_CHANNEL_ID) != null) {
            manager.deleteNotificationChannel(LEGACY_CHANNEL_ID);
        }
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.notif_channel_description));
        Uri sound = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.getPackageName())
                .appendPath(String.valueOf(R.raw.centre_tri))
                .build();
        channel.setSound(sound, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATION);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);
        manager.createNotificationChannel(channel);
    }

    /** Vrai si Android autorise l'affichage plein écran sur l'écran verrouillé (Android 14 : réglage à part). */
    static boolean canFullScreen(Context context) {
        if (Build.VERSION.SDK_INT < 34) return true;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager != null && manager.canUseFullScreenIntent();
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
    static String post(Context context, StatusBarNotification sbn, String sender, String message,
                       String appLabel, boolean wakeScreen) {
        if (sbn == null || message == null || message.isEmpty()) return "vide";
        if (!canPost(context)) return "affichage-refuse";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return "echec";
        ensureChannel(context);

        String key = sbn.getKey();
        if (key == null) return "echec";

        Notification original = sbn.getNotification();
        long when = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();

        RemoteViews card = new RemoteViews(context.getPackageName(), R.layout.notification_card);
        card.setTextViewText(R.id.cardSender, sender);
        card.setTextViewText(R.id.cardMessage, message);
        card.setTextViewText(R.id.cardTime,
                DateFormat.getTimeFormat(context).format(new Date(when)));
        card.setImageViewResource(R.id.cardDot, dotFor(sbn.getPackageName()));
        if (appLabel != null && !appLabel.isEmpty()) {
            card.setTextViewText(R.id.cardSource, appLabel);
        }

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
                // Faux : chaque nouveau message d'une même conversation doit sonner et s'afficher.
                .setOnlyAlertOnce(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(0xFF1B1E2B)
                .addAction(new Notification.Action.Builder(
                        null, context.getString(R.string.notif_action_clear), clearIntent).build());

        if (appLabel != null && !appLabel.isEmpty()) builder.setSubText(appLabel);

        // Écran éteint ou verrouillé : Android allume l'écran et ouvre l'écran verrouillé du centre.
        if (wakeScreen && canFullScreen(context)) {
            builder.setFullScreenIntent(LockScreenActivity.launchIntent(context), true);
        }

        if (original != null) {
            if (original.category != null) builder.setCategory(original.category);
            if (original.getLargeIcon() != null) builder.setLargeIcon(original.getLargeIcon());
            if (original.contentIntent != null) builder.setContentIntent(original.contentIntent);
            Notification.Action reply = replyActionOf(original);
            if (reply != null) builder.addAction(reply);
        }

        try {
            manager.notify(key, 1, builder.build());
            return "ok";
        } catch (Exception err) {
            return "echec";
        }
    }

    /** Carte de démonstration, sans message réel : sert à vérifier l'affichage et l'îlot. */
    static String postTest(Context context) {
        if (!canPost(context)) return "affichage-refuse";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return "echec";
        ensureChannel(context);

        long now = System.currentTimeMillis();
        RemoteViews card = new RemoteViews(context.getPackageName(), R.layout.notification_card);
        card.setTextViewText(R.id.cardSender, "Carte de test");
        card.setTextViewText(R.id.cardMessage, "Si vous voyez cette carte dans vos notifications, le centre affiche correctement.");
        card.setTextViewText(R.id.cardTime, DateFormat.getTimeFormat(context).format(new Date(now)));
        card.setImageViewResource(R.id.cardDot, R.drawable.dot_blue);
        card.setTextViewText(R.id.cardSource, "Centre de notifications · carte de démonstration");

        Intent clear = new Intent(context, ActionReceiver.class)
                .setAction(ActionReceiver.ACTION_CLEAR)
                .putExtra(ActionReceiver.EXTRA_KEY, "centre-test");
        PendingIntent clearIntent = PendingIntent.getBroadcast(
                context, "centre-test".hashCode(), clear,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder testBuilder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setWhen(now)
                .setShowWhen(true)
                .setContentTitle("Carte de test")
                .setContentText("Le centre affiche correctement.")
                .setCustomContentView(card)
                .setCustomBigContentView(card)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(0xFF1B1E2B)
                .addAction(new Notification.Action.Builder(
                        null, context.getString(R.string.notif_action_clear), clearIntent).build())
                ;
        if (LockScreenActivity.deviceIsLockedOrOff(context) && canFullScreen(context)) {
            testBuilder.setFullScreenIntent(LockScreenActivity.launchIntent(context), true);
        }
        Notification notification = testBuilder.build();

        try {
            manager.notify("centre-test", 1, notification);
            return "ok";
        } catch (Exception err) {
            return "echec";
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
