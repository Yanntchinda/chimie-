package com.yanntchinda.centrenotifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.json.JSONObject;

/**
 * Enveloppe native du centre de notifications.
 *
 * La page est chargee depuis les assets, sans acces reseau. Les notifications
 * reelles du telephone arrivent par le service d'ecoute (NotificationRelay) et
 * sont poussees dans la page, qui reste le seul endroit ou elles sont affichees.
 */
public class MainActivity extends Activity implements CenterBridge.Sink {

    private static final String PAGE = "file:///android_asset/index.html?app=1";

    private WebView webView;
    private CenterBridge bridge;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setTextZoom(100);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WebViewClient());

        bridge = new CenterBridge(this);
        bridge.setSink(this);
        webView.addJavascriptInterface(bridge, "Centre");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // L'encoche ne doit pas recouvrir le haut de l'ecran : on decale la page
        // de la hauteur utile de la decoupe, et de rien du tout ailleurs.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                DisplayCutout cutout = insets.getDisplayCutout();
                int top = cutout != null ? cutout.getSafeInsetTop() : 0;
                if (view.getPaddingTop() != top) {
                    view.setPadding(0, top, 0, 0);
                }
                return insets;
            });
        }

        setContentView(root);
        webView.loadUrl(PAGE);
    }

    /** Livraison d'une notification reelle dans la page. */
    @Override
    public void deliver(String jsonArray) {
        final WebView view = webView;
        if (view == null || jsonArray == null) return;
        final String script = "window.__centreRecoit&&window.__centreRecoit(" + JSONObject.quote(jsonArray) + ");";
        view.post(() -> {
            if (webView != null) webView.evaluateJavascript(script, null);
        });
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
    protected void onPause() {
        super.onPause();
        if (bridge != null) bridge.setSink(null);
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bridge != null) bridge.setSink(this);
        if (webView != null) {
            webView.onResume();
            // Rattrape les notifications arrivees pendant l'absence.
            webView.post(() -> {
                if (webView != null) {
                    webView.evaluateJavascript("window.__centreRelance&&window.__centreRelance();", null);
                }
            });
        }
    }

    /** Le bouton retour ferme d'abord le volet ouvert, puis quitte l'application. */
    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        webView.evaluateJavascript(
                "(function(){var b=document.body;"
                        + "if(b&&b.classList.contains('sheet-open')){"
                        + "var a=document.getElementById('sheetBackdrop');"
                        + "if(a){a.click();}return 'ferme';}return 'libre';})()",
                value -> {
                    if (value == null || !value.contains("ferme")) {
                        if (webView != null && webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    }
                });
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
}
