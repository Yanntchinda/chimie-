package com.yanntchinda.centrenotifications;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Écran verrouillé du centre : s'allume et s'affiche par-dessus le verrouillage d'Android
 * à chaque message, avec la pile des derniers messages, puis se referme tout seul.
 *
 * L'appareil reste verrouillé : toucher une carte demande le déverrouillage avant d'ouvrir
 * l'application d'origine.
 */
public class LockScreenActivity extends Activity {

    private static final long VISIBLE_MS = 9000;
    private static final String EXTRA_WAKE = "wake";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable close = this::finish;

    private LinearLayout list;
    private TextView empty;
    private ScrollView scroll;
    private int lastCount = 0;

    /** Allume l'écran et affiche la pile de messages. Peut être appelé depuis le service. */
    static boolean launch(Context context) {
        return start(context, false);
    }

    /** Affiché à l'allumage de l'écran : reste en place jusqu'au déverrouillage. */
    static boolean launchWake(Context context) {
        return start(context, true);
    }

    private static boolean start(Context context, boolean wake) {
        try {
            Intent intent = new Intent(context, LockScreenActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    .putExtra(EXTRA_WAKE, wake);
            context.startActivity(intent);
            return true;
        } catch (Exception err) {
            // Démarrage refusé par Android : la notification plein écran prend le relais.
            return false;
        }
    }

    /** Le téléphone est déverrouillé : l'écran du centre n'a plus de raison de rester. */
    private final BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private boolean waitingForUnlock = false;

    static PendingIntent launchIntent(Context context) {
        Intent intent = new Intent(context, LockScreenActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 7001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Vrai quand l'écran est éteint ou verrouillé : l'îlot ne serait pas visible. */
    static boolean deviceIsLockedOrOff(Context context) {
        android.os.PowerManager power = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean off = power != null && !power.isInteractive();
        boolean locked = keyguard != null && keyguard.isKeyguardLocked();
        return off || locked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOverLockScreen();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lockscreen);

        list = findViewById(R.id.lockList);
        empty = findViewById(R.id.lockEmpty);
        scroll = findViewById(R.id.lockScroll);
        findViewById(R.id.lockRoot).setOnClickListener(view -> finish());

        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_WAKE, false)) {
            // Allumage de l'écran : on reste affiché jusqu'au déverrouillage.
            watchUnlock();
        } else {
            scheduleClose();
        }

        render();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showOverLockScreen();
        render();
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_WAKE, false)) {
            watchUnlock();
        } else {
            scheduleClose();
        }
    }

    private void watchUnlock() {
        handler.removeCallbacks(close);
        if (waitingForUnlock) return;
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(unlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(unlockReceiver, filter);
            }
            waitingForUnlock = true;
        } catch (Exception ignored) {
            // Sans ce signal, l'écran se referme tout seul.
            scheduleClose();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(close);
        if (waitingForUnlock) {
            try {
                unregisterReceiver(unlockReceiver);
            } catch (Exception ignored) {
                // Déjà retiré.
            }
            waitingForUnlock = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Une interaction repousse la fermeture : l'écran ne se referme pas pendant la lecture.
        if (event.getAction() == MotionEvent.ACTION_DOWN) scheduleClose();
        return super.dispatchTouchEvent(event);
    }

    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void scheduleClose() {
        handler.removeCallbacks(close);
        handler.postDelayed(close, VISIBLE_MS);
    }

    private void render() {
        TextView clockHint = findViewById(R.id.lockDate);
        clockHint.setText(DateFormat.format("EEEE d MMMM", new Date()));

        final boolean wasAtBottom = scroll == null
                || scroll.getChildCount() == 0
                || scroll.getScrollY() + scroll.getHeight() >= scroll.getChildAt(0).getHeight() - 12;

        list.removeAllViews();
        List<RecentStore.Item> items = RecentStore.snapshot();
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Du plus ancien au plus récent : les nouveaux messages s'empilent en dessous,
        // comme dans la maquette, et on remonte la liste pour relire les précédents.
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int index = items.size() - 1; index >= 0; index--) {
            RecentStore.Item item = items.get(index);
            View card = inflater.inflate(R.layout.lock_item, list, false);
            ((TextView) card.findViewById(R.id.lockSender)).setText(item.sender);
            ((TextView) card.findViewById(R.id.lockMessage)).setText(item.message);
            ((TextView) card.findViewById(R.id.lockSource)).setText(item.appLabel);
            ((TextView) card.findViewById(R.id.lockTime)).setText(
                    DateFormat.getTimeFormat(this).format(new Date(item.when)));
            ((ImageView) card.findViewById(R.id.lockDot)).setImageResource(item.dotRes);
            attach(card, item);
            list.addView(card);
            if (index == 0 && lastCount > 0) {
                // Dernier arrivé : il apparaît en fondu depuis le bas.
                card.setAlpha(0f);
                card.setTranslationY(24f);
                card.animate().alpha(1f).translationY(0f).setDuration(220).start();
            }
        }

        final int count = items.size();
        final boolean firstShow = lastCount == 0;
        if (scroll != null && (firstShow || wasAtBottom)) {
            scroll.post(() -> {
                if (scroll != null) scroll.fullScroll(View.FOCUS_DOWN);
            });
        }
        lastCount = count;
    }

    /** Toucher ouvre le message ; glisser vers la gauche ou la droite l'efface. */
    private void attach(View card, RecentStore.Item item) {
        final float[] start = new float[2];
        final boolean[] moved = new boolean[1];
        final boolean[] horizontal = new boolean[1];
        card.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    start[0] = event.getRawX();
                    start[1] = event.getRawY();
                    moved[0] = false;
                    horizontal[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getRawX() - start[0];
                    float dy = event.getRawY() - start[1];
                    if (!horizontal[0]) {
                        // Geste vertical : c'est la pile qui défile, la carte ne bouge pas.
                        if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 10) {
                            view.setTranslationX(0f);
                            view.setAlpha(1f);
                            return true;
                        }
                        if (Math.abs(dx) > 14) horizontal[0] = true;
                    }
                    if (horizontal[0]) {
                        moved[0] = true;
                        view.setTranslationX(dx);
                        view.setAlpha(1f - Math.min(1f, Math.abs(dx) / (view.getWidth() * 0.8f)));
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    float dx = event.getRawX() - start[0];
                    if (horizontal[0] && Math.abs(dx) > view.getWidth() * 0.35f) {
                        dismiss(view, item, dx > 0 ? view.getWidth() : -view.getWidth());
                    } else if (!moved[0]) {
                        open(item);
                    } else {
                        view.animate().translationX(0f).alpha(1f).setDuration(140).start();
                    }
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                    view.animate().translationX(0f).alpha(1f).setDuration(140).start();
                    return true;
                default:
                    return false;
            }
        });
    }

    private void dismiss(View card, RecentStore.Item item, float toX) {
        RecentStore.remove(item.key);
        android.app.NotificationManager manager =
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(item.key, 1);
        NotificationRelay.cancelOriginal(item.key);
        card.animate().translationX(toX).alpha(0f).setDuration(160)
                .withEndAction(() -> {
                    list.removeView(card);
                    if (list.getChildCount() == 0) finish();
                }).start();
    }

    private void open(RecentStore.Item item) {
        final PendingIntent target = item.open;
        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Runnable go = () -> {
            try {
                if (target != null) target.send();
            } catch (PendingIntent.CanceledException ignored) {
                // L'application d'origine a retiré son action.
            }
            RecentStore.remove(item.key);
            finish();
        };
        if (keyguard != null && keyguard.isKeyguardLocked()) {
            // Le téléphone reste verrouillé : on demande le code avant d'ouvrir le message.
            keyguard.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissSucceeded() {
                    go.run();
                }
            });
        } else {
            go.run();
        }
    }
}
