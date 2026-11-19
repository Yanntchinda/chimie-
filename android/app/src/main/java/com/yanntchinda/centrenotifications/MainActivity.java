package com.yanntchinda.centrenotifications;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Écran de réglage, ouvert seulement quand on le demande.
 *
 * Le centre lui-même n'a pas d'écran : une fois l'accès accordé, les messages
 * apparaissent directement dans les notifications du téléphone, dans le style du
 * centre. Cet écran sert à accorder cet accès et à vérifier son état.
 */
public class MainActivity extends Activity {

    private static final int POST_NOTIFICATIONS_REQUEST = 1;

    private TextView accessState;
    private TextView postState;
    private TextView islandState;
    private TextView receiveState;
    private TextView diagnosticState;
    private Switch replaceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        accessState = findViewById(R.id.accessState);
        postState = findViewById(R.id.postState);
        islandState = findViewById(R.id.islandState);
        receiveState = findViewById(R.id.receiveState);
        diagnosticState = findViewById(R.id.diagnosticState);
        replaceSwitch = findViewById(R.id.replaceSwitch);
        Button accessButton = findViewById(R.id.accessButton);
        Button postButton = findViewById(R.id.postButton);
        Button islandButton = findViewById(R.id.islandButton);

        accessButton.setOnClickListener(view -> openListenerSettings());
        postButton.setOnClickListener(view -> requestPosting());
        islandButton.setOnClickListener(view -> openOverlaySettings());

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view -> {
            String status = StyledNotifier.postTest(this);
            CenterPrefs.notePosted(this, status);
            if ("ok".equals(status)) {
                IslandOverlay.show(this, "Carte de test", "Le centre affiche correctement.", R.drawable.dot_blue);
            }
            refresh();
        });
        replaceSwitch.setOnCheckedChangeListener((button, checked) ->
                CenterPrefs.setReplaceOriginals(this, checked));

        StyledNotifier.ensureChannel(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refresh();
    }

    private void refresh() {
        boolean access = isAccessGranted();
        accessState.setText(access
                ? "Accordé. Chaque notification reçue s'affiche dans le style du centre."
                : "Non accordé. Sans cet accès, le centre ne reçoit rien et aucune notification n'est réécrite.");

        boolean posting = StyledNotifier.canPost(this);
        postState.setText(posting
                ? "Autorisé. Les cartes du centre peuvent s'afficher."
                : "Refusé. Android bloque les notifications de cette application.");

        boolean island = IslandOverlay.allowed(this);
        islandState.setText(island
                ? "Accordé. Une pastille descend du haut de l'écran à chaque message, par-dessus l'application en cours."
                : "Non accordé. Sans cet accès, les cartes restent dans les notifications, sans îlot par-dessus les applications.");

        int read = CenterPrefs.receivedCount(this);
        int posted = CenterPrefs.postedCount(this);
        String status = CenterPrefs.lastStatus(this);
        String meaning;
        if (read == 0) {
            meaning = "aucun message n'est encore arrivé jusqu'au centre : l'accès aux notifications n'est pas actif.";
        } else if (posted == 0) {
            meaning = "des messages sont lus, mais aucune carte n'a pu être publiée : Android refuse l'affichage.";
        } else if ("ok".equals(status)) {
            meaning = "tout fonctionne : les messages sont lus et réécrits.";
        } else {
            meaning = "la dernière tentative a échoué (" + status + ").";
        }
        diagnosticState.setText("Messages lus : " + read + " · cartes publiées : " + posted + "\n" + meaning);

        int count = CenterPrefs.receivedCount(this);
        long last = CenterPrefs.receivedLast(this);
        if (count == 0) {
            receiveState.setText("Aucun message reçu pour l'instant. Si l'accès est accordé et que rien n'arrive, "
                    + "désactiver l'optimisation de la batterie pour cette application.");
        } else {
            receiveState.setText(count + " message" + (count > 1 ? "s" : "") + " reçu" + (count > 1 ? "s" : "")
                    + ", le dernier à " + android.text.format.DateFormat.getTimeFormat(this).format(new java.util.Date(last)) + ".");
        }

        replaceSwitch.setChecked(CenterPrefs.replaceOriginals(this));
    }

    private void openOverlaySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {
            // Rien à ouvrir sur cet appareil.
        }
    }

    private boolean isAccessGranted() {
        try {
            String enabled = Settings.Secure.getString(getContentResolver(),
                    "enabled_notification_listeners");
            return enabled != null && enabled.contains(getPackageName());
        } catch (Exception err) {
            return false;
        }
    }

    private void openListenerSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception ignored) {
            // Certains appareils n'exposent pas cet écran.
        }
    }

    private void requestPosting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST);
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        } catch (Exception ignored) {
            // Rien à ouvrir : l'état reste affiché à l'écran.
        }
    }
}
