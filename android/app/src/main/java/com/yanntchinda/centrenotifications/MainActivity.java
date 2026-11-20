package com.yanntchinda.centrenotifications;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
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
    private TextView lockState;
    private TextView batteryState;
    private Switch replaceSwitch;
    private Switch wakeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        accessState = findViewById(R.id.accessState);
        postState = findViewById(R.id.postState);
        islandState = findViewById(R.id.islandState);
        receiveState = findViewById(R.id.receiveState);
        diagnosticState = findViewById(R.id.diagnosticState);
        lockState = findViewById(R.id.lockState);
        batteryState = findViewById(R.id.batteryState);
        replaceSwitch = findViewById(R.id.replaceSwitch);
        wakeSwitch = findViewById(R.id.wakeSwitch);
        Button accessButton = findViewById(R.id.accessButton);
        Button postButton = findViewById(R.id.postButton);
        Button islandButton = findViewById(R.id.islandButton);
        Button homeButton = findViewById(R.id.homeButton);

        accessButton.setOnClickListener(view -> openListenerSettings());
        findViewById(R.id.appInfoButton).setOnClickListener(view -> openAppInfo());
        postButton.setOnClickListener(view -> requestPosting());
        islandButton.setOnClickListener(view -> openOverlaySettings());
        homeButton.setOnClickListener(view -> openHomeSettings());

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view -> {
            String status = StyledNotifier.postTest(this);
            CenterPrefs.notePosted(this, status);
            if ("ok".equals(status)) {
                NotificationRelay.present(getApplicationContext(), testItem(), false);
            }
            refresh();
        });

        findViewById(R.id.burstTestButton).setOnClickListener(view -> simulateBurst());
        findViewById(R.id.lockButton).setOnClickListener(view -> openFullScreenSettings());
        findViewById(R.id.lockTestButton).setOnClickListener(view -> {
            // Laisse le temps d'éteindre l'écran, puis simule un message écran éteint.
            android.widget.Toast.makeText(this,
                    "Éteignez l'écran maintenant : le test arrive dans 8 secondes.",
                    android.widget.Toast.LENGTH_LONG).show();
            final android.content.Context app = getApplicationContext();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String status = StyledNotifier.postTest(app);
                CenterPrefs.notePosted(app, status);
                if ("ok".equals(status)) {
                    NotificationRelay.present(app, testItem(), LockScreenActivity.deviceIsLockedOrOff(app));
                }
            }, 8000);
        });
        findViewById(R.id.batteryButton).setOnClickListener(view -> openBatterySettings());
        replaceSwitch.setOnCheckedChangeListener((button, checked) ->
                CenterPrefs.setReplaceOriginals(this, checked));
        wakeSwitch.setOnCheckedChangeListener((button, checked) ->
                CenterPrefs.setShowOnWake(this, checked));

        StyledNotifier.ensureChannel(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Accès accordé mais service non connecté (arrêté par le téléphone) : on force la reconnexion.
        if (isAccessGranted() && !NotificationRelay.isConnected()) rebindListener();
        refresh();
    }

    /** Remplit l'écran verrouillé de 30 messages pour vérifier le défilement de la pile. */
    private void simulateBurst() {
        String[] apps = {"WhatsApp", "Messages", "Telegram", "Mail", "Instagram", "Signal"};
        String[] senders = {"Équipe Logistique", "Sarah Martin", "Banque", "Karim", "Herlo Global",
                "Bot Trading XAU", "Maman", "Client Travex", "Nadia", "Chef de chantier"};
        int[] dots = {R.drawable.dot_green, R.drawable.dot_green, R.drawable.dot_blue,
                R.drawable.dot_amber, R.drawable.dot_purple, R.drawable.dot_blue};
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

        RecentStore.clear();
        long now = System.currentTimeMillis();
        for (int index = 0; index < 30; index++) {
            int app = index % apps.length;
            String sender = senders[index % senders.length];
            String message = texts[index % texts.length];
            RecentStore.put(new RecentStore.Item(
                    "simulation-" + index,
                    "simulation",
                    apps[app],
                    sender,
                    message,
                    now - index * 60000L,
                    dots[app],
                    null));
        }

        Toast.makeText(this, "30 messages simulés : écran verrouillé ouvert.", Toast.LENGTH_SHORT).show();
        LockScreenActivity.launchWake(this);
    }

    private RecentStore.Item testItem() {
        return new RecentStore.Item("centre-test", getPackageName(), "Centre de notifications",
                "Carte de test", "Le centre affiche correctement.", System.currentTimeMillis(),
                R.drawable.dot_blue, null);
    }

    /** Coupe puis rétablit le service d'écoute : méthode connue pour relancer un service que le téléphone a arrêté. */
    private void rebindListener() {
        try {
            ComponentName name = new ComponentName(this, NotificationRelay.class);
            android.content.pm.PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(name,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(name,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP);
        } catch (Exception ignored) {
            // Le téléphone refuse : l'utilisateur peut réactiver l'accès à la main.
        }
    }

    private boolean batteryUnrestricted() {
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        return power != null && power.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void openBatterySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + getPackageName())));
        } catch (Exception err) {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ignored) {
                // Rien à ouvrir sur cet appareil.
            }
        }
    }

    private void openFullScreenSettings() {
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:" + getPackageName())));
            } else {
                startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
            }
        } catch (Exception ignored) {
            // Rien à ouvrir sur cet appareil.
        }
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

        boolean fullScreen = StyledNotifier.canFullScreen(this);
        lockState.setText(fullScreen
                ? "Accordé. Écran éteint ou verrouillé, le centre allume l'écran et affiche les messages par-dessus le verrouillage."
                : "Non accordé. Sans cet accès, l'écran ne s'allume pas à l'arrivée d'un message et l'écran verrouillé du centre ne s'affiche pas.");
        batteryState.setText(batteryUnrestricted()
                ? "Accordé. Le téléphone ne coupe plus l'écoute en arrière-plan."
                : "Non accordé. Le téléphone peut arrêter l'écoute écran éteint : les messages n'arrivent plus au centre.");

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
        long connected = CenterPrefs.connectedAt(this);
        String service = connected == 0
                ? "service jamais connecté"
                : "service connecté à " + android.text.format.DateFormat.getTimeFormat(this).format(new java.util.Date(connected));
        diagnosticState.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "Messages lus : " + read + " · cartes publiées : " + posted
                + "\n" + service + "\n" + meaning);

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
        wakeSwitch.setChecked(CenterPrefs.showOnWake(this));
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception ignored) {
            // Certains appareils n'exposent pas cet ecran.
        }
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

    /** Fiche de l'application dans les reglages Android : c'est la que se trouve le menu ⋮. */
    private void openAppInfo() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
        } catch (Exception ignored) {
            // Certains appareils renvoient vers une liste d'applications.
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
