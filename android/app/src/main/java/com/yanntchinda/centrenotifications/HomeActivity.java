package com.yanntchinda.centrenotifications;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Écran d'accueil du centre, proposé comme lanceur Android.
 *
 * Choisi comme application d'accueil, il remplace l'écran d'accueil du téléphone :
 * on y voit l'heure et la pile des messages reçus, avec les réglages en bas.
 */
public class HomeActivity extends Activity {

    private static final long REFRESH_MS = 3000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresh = this::render;

    private LinearLayout list;
    private TextView empty;
    private ScrollView scroll;
    private int lastCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        list = findViewById(R.id.homeList);
        empty = findViewById(R.id.homeEmpty);
        scroll = findViewById(R.id.homeScroll);
        findViewById(R.id.homeSettings).setOnClickListener(view ->
                startActivity(new Intent(this, MainActivity.class)));
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
        handler.removeCallbacks(refresh);
        handler.postDelayed(refresh, REFRESH_MS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refresh);
        super.onPause();
    }

    private void render() {
        TextView date = findViewById(R.id.homeDate);
        date.setText(DateFormat.format("EEEE d MMMM", new Date()));

        final boolean wasAtBottom = scroll == null
                || scroll.getChildCount() == 0
                || scroll.getScrollY() + scroll.getHeight() >= scroll.getChildAt(0).getHeight() - 12;

        list.removeAllViews();
        List<RecentStore.Item> items = RecentStore.snapshot();
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Du plus ancien au plus récent : les nouveaux arrivent en dessous.
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
        }

        final int count = items.size();
        final boolean firstShow = lastCount == 0 || count > lastCount;
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
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(item.key, 1);
        NotificationRelay.cancelOriginal(item.key);
        card.animate().translationX(toX).alpha(0f).setDuration(160)
                .withEndAction(() -> {
                    list.removeView(card);
                    if (list.getChildCount() == 0) empty.setVisibility(View.VISIBLE);
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
            render();
        };
        if (keyguard != null && keyguard.isKeyguardLocked()) {
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
