package com.yanntchinda.centrenotifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Centre de notifications à l'écran : la page de la maquette, identique au rendu web
 * (îlot, pile, fils, gestes), alimentée par les notifications réelles du téléphone.
 *
 * Sert d'écran verrouillé à l'allumage et à l'arrivée d'un message.
 */
public class BoardActivity extends Activity implements CenterBridge.Sink {

    private static final String PAGE = "file:///android_asset/index.html?app=1";
    private static final String EXTRA_WAKE = "wake";
    private static final String EXTRA_DEMO = "demo";

    private WebView webView;
    private CenterBridge bridge;
    private boolean pageReady = false;

    /** Écran verrouillé ou éteint : la page s'allume et reste jusqu'au déverrouillage. */
    static boolean launchLock(Context context) {
        return start(context, false, 0);
    }

    /** Allumage de l'écran : même écran, qui attend le déverrouillage. */
    static boolean launchWake(Context context) {
        return start(context, true, 0);
    }

    /** Ouverture demandée par l'utilisateur, éventuellement avec des messages de démonstration. */
    static void launchDemo(Context context, int demoCount) {
        start(context, false, demoCount);
    }

    private static boolean start(Context context, boolean wake, int demoCount) {
        try {
            Intent intent = new Intent(context, BoardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    .putExtra(EXTRA_WAKE, wake)
                    .putExtra(EXTRA_DEMO, demoCount);
            context.startActivity(intent);
            return true;
        } catch (Exception err) {
            // Refusé par Android : la notification plein écran prend le relais.
            return false;
        }
    }

    /** Cible d'une notification plein écran, quand Android refuse le démarrage direct. */
    static PendingIntent launchIntent(Context context) {
        Intent intent = new Intent(context, BoardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 7001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showOverLockScreen();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        applyImmersiveMode();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setBlockNetworkLoads(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                view.evaluateJavascript("window.__centreRelance&&window.__centreRelance();", null);
                injectDemo();
            }
        });

        bridge = new CenterBridge(this);
        bridge.setSink(this);
        webView.addJavascriptInterface(bridge, "Centre");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                DisplayCutout cutout = insets.getDisplayCutout();
                int top = cutout != null ? cutout.getSafeInsetTop() : 0;
                if (view.getPaddingTop() != top) view.setPadding(0, top, 0, 0);
                return insets;
            });
        }

        setContentView(root);
        webView.loadUrl(PAGE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showOverLockScreen();
        if (pageReady) injectDemo();
    }

    /** Livraison d'une notification réelle dans la page. */
    @Override
    public void deliver(String jsonArray) {
        final WebView view = webView;
        if (view == null || jsonArray == null) return;
        final String script = "window.__centreRecoit&&window.__centreRecoit(" + JSONObject.quote(jsonArray) + ");";
        view.post(() -> {
            if (webView != null) webView.evaluateJavascript(script, null);
        });
    }

    /** Messages de démonstration : montre la pile et les gestes de la maquette. */
    private void injectDemo() {
        if (webView == null) return;
        Intent intent = getIntent();
        int count = intent == null ? 0 : intent.getIntExtra(EXTRA_DEMO, 0);
        if (count <= 0) return;
        intent.putExtra(EXTRA_DEMO, 0);

        String[] apps = {"WhatsApp", "Messages", "Telegram", "Mail", "Instagram", "Signal"};
        String[] senders = {"Équipe Logistique", "Sarah Martin", "Banque", "Karim", "Herlo Global",
                "Bot Trading XAU", "Maman", "Client Travex", "Nadia", "Chef de chantier"};
        String[] texts = {
                "Le colis vient d'arriver au port, on confirme la livraison ?",
                "Le dossier d'expédition a été mis à jour.",
                "Virement entrant de 450 000 FCFA crédité sur votre compte.",
                "Alerte structure H1 : cassure haussière confirmée sur l'or.",
                "Tu passes cet après-midi ?",
                "Nouvelle commande validée sur la boutique.",
                "Réunion déplacée à 16h, salle 3.",
                "Le bordereau est prêt, livraison confirmée.",
                "Confirme la présence du chef d'équipe sur le site.",
                "Dernier point avant le départ du camion."
        };

        JSONArray events = new JSONArray();
        long now = System.currentTimeMillis();
        try {
            for (int index = 0; index < count; index++) {
                JSONObject event = new JSONObject();
                event.put("key", "demo-" + index + "-" + now);
                event.put("pkg", "demo." + apps[index % apps.length].toLowerCase());
                event.put("app", apps[index % apps.length]);
                event.put("title", senders[index % senders.length]);
                event.put("text", texts[index % texts.length]);
                event.put("at", now - (count - index) * 45000L);
                event.put("canReply", index % 3 != 2);
                events.put(event);
            }
        } catch (Exception ignored) {
            return;
        }

        final String payload = events.toString();
        webView.post(() -> {
            if (webView != null) {
                webView.evaluateJavascript(
                        "window.__centreRecoit&&window.__centreRecoit(" + JSONObject.quote(payload) + ");",
                        null);
            }
        });
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

    private void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyImmersiveMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bridge != null) bridge.setSink(this);
        if (webView != null) {
            webView.onResume();
            webView.post(() -> {
                if (webView != null) {
                    webView.evaluateJavascript("window.__centreRelance&&window.__centreRelance();", null);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bridge != null) bridge.setSink(null);
        if (webView != null) webView.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (bridge != null) {
            bridge.setSink(null);
            bridge = null;
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /** Vrai quand le téléphone est verrouillé : la page sert d'écran verrouillé. */
    static boolean isLocked(Context context) {
        KeyguardManager keyguard = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguard != null && keyguard.isKeyguardLocked();
    }
}
