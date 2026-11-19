package com.yanntchinda.centrenotifications;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Îlot du centre : une pastille noire qui descend du haut de l'écran quand un message
 * arrive, par-dessus l'application en cours, puis se referme.
 *
 * Nécessite l'autorisation « Afficher par-dessus les autres applications ».
 */
final class IslandOverlay {

    private static final long VISIBLE_MS = 3600;

    private static WindowManager windowManager;
    private static View pill;
    private static TextView senderView;
    private static TextView messageView;
    private static ImageView dotView;
    private static WindowManager.LayoutParams params;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Runnable dismiss = IslandOverlay::hide;

    private IslandOverlay() {
    }

    static boolean allowed(Context context) {
        try {
            return Settings.canDrawOverlays(context);
        } catch (Exception err) {
            return false;
        }
    }

    static void show(Context context, String sender, String message, int dotRes) {
        if (context == null) return;
        if (!allowed(context)) return;
        if (Looper.myLooper() != Looper.getMainLooper()) return;

        try {
            if (pill == null) {
                windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager == null) return;
                pill = LayoutInflater.from(context).inflate(R.layout.island_pill, null);
                senderView = pill.findViewById(R.id.islandSender);
                messageView = pill.findViewById(R.id.islandMessage);
                dotView = pill.findViewById(R.id.islandDot);

                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                params.y = dp(context, 44);
                pill.setOnClickListener(view -> hide());
                windowManager.addView(pill, params);
            }

            senderView.setText(sender);
            messageView.setText(message);
            dotView.setImageResource(dotRes);

            pill.animate().cancel();
            pill.setScaleX(0.92f);
            pill.setAlpha(0f);
            pill.animate().scaleX(1f).alpha(1f).setDuration(180).start();

            handler.removeCallbacks(dismiss);
            handler.postDelayed(dismiss, VISIBLE_MS);
        } catch (Exception ignored) {
            // Autorisation retirée entre-temps ou fenêtre refusée : on n'affiche rien.
            pill = null;
        }
    }

    static void hide() {
        handler.removeCallbacks(dismiss);
        View view = pill;
        if (view == null) return;
        pill = null;
        try {
            view.animate().alpha(0f).setDuration(160).withEndAction(() -> {
                try {
                    if (windowManager != null) windowManager.removeView(view);
                } catch (Exception ignored) {
                    // Déjà retirée.
                }
            }).start();
        } catch (Exception ignored) {
            try {
                if (windowManager != null) windowManager.removeView(view);
            } catch (Exception ignoredToo) {
                // Déjà retirée.
            }
        }
    }

    static void release() {
        handler.removeCallbacks(dismiss);
        View view = pill;
        pill = null;
        if (view == null || windowManager == null) return;
        try {
            windowManager.removeView(view);
        } catch (Exception ignored) {
            // Déjà retirée.
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
