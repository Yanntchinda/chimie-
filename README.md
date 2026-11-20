# Centre de notifications

Un système de notifications pour Android qui remplace le style d'affichage des
messages reçus : carte sombre translucide, bords fins, métadonnées discrètes.
**Aucun écran à ouvrir** : une fois installé et autorisé, chaque message reçu
apparaît dans le volet de notifications du téléphone avec ce style.

## Ce que fait l'application

1. Elle écoute les notifications du téléphone (service d'écoute Android).
2. Elle retire la notification d'origine et affiche la sienne, dans le style du
   centre : pastille de l'application, expéditeur, texte, heure.
3. Une pastille noire descend du haut de l'écran par-dessus l'application en cours,
   puis se referme (autorisation « Afficher par-dessus » facultative).
4. Téléphone verrouillé ou écran éteint : l'écran s'allume sur l'écran verrouillé du
   centre, avec la pile des derniers messages. Glisser une carte l'efface ; la
   toucher demande le déverrouillage avant d'ouvrir l'application d'origine.
5. Elle conserve l'action de réponse de l'application d'origine : répondre depuis
   la carte écrit directement dans WhatsApp, Messages, Telegram, Gmail…
6. Toucher la carte ouvre l'application qui a envoyé le message.
7. Le bouton **Effacer** retire la carte et la notification d'origine.

Rien ne quitte l'appareil : aucune permission `INTERNET` n'est déclarée, donc
l'application ne peut joindre aucun serveur.

## Installer

Guide détaillé, avec code à scanner et dépannage Play Protect :
[installation/INSTALLATION.md](installation/INSTALLATION.md).

1. Télécharger `app-release.apk` depuis les [versions](../../releases).
2. Ouvrir le fichier, autoriser l'installation depuis cette source.
3. Lancer **Centre de notifications** une seule fois : l'écran de réglage indique
   trois accès à accorder.
   - **Autoriser l'accès aux notifications** → activer **Centre de notifications**
     dans la liste Android.
   - **Autoriser l'affichage** → accepter les notifications (Android 13 et plus).
   - **Autoriser l'affichage par-dessus** → activer la superposition (îlot et écran verrouillé).
   - **Ignorer l'optimisation de la batterie** → garde l'écoute active écran éteint.
4. Refermer l'application : elle n'a plus besoin d'être ouverte.

L'écran de réglage reste accessible depuis l'icône, uniquement pour vérifier l'état
des accès et l'option de remplacement.

## Mode lanceur (écran d'accueil)

Comme les thèmes iPhone du Play Store, qui sont des **lanceurs** : le centre peut
devenir l'écran d'accueil du téléphone. Dans l'application, **Choisir l'application
d'accueil** → sélectionner **Centre de notifications**. L'appui sur la touche Accueil
affiche alors l'heure et la pile des messages, avec les réglages en bas. Pour revenir
en arrière, choisir un autre lanceur dans la même liste.

Ce que ce mode ne remplace pas : le volet de notifications système et l'écran
verrouillé d'Android. Aucune application du Play Store ne les remplace non plus —
les thèmes iPhone les imitent avec des activités par-dessus le verrouillage et des
fenêtres superposées, exactement ce que fait le centre.

## Réglages

- **Remplacer les notifications d'origine** (activé par défaut) : une seule carte
  reste dans le volet, celle du centre. Désactivé, le message apparaît deux fois,
  avec le style d'origine et avec celui du centre — utile pour comparer.
- L'importance, le son et l'affichage sur l'écran verrouillé se règlent dans
  Android : Paramètres → Applications → Centre de notifications → Notifications.

## Interface

`gemini-code-1795117363618.html` n'est pas une maquette à part : c'est **l'interface
de l'application**. La page est copiée dans les assets à chaque compilation et
affichée en plein écran par `BoardActivity`, avec les notifications réelles du
téléphone injectées par `CenterBridge`. Ce que vous voyez dans le navigateur est
donc exactement ce que vous voyez sur l'écran verrouillé, îlot et gestes compris.

Réglages → **Simuler 30 messages sur l'écran verrouillé** ouvre cette page avec
30 messages de démonstration.

## Construire soi-même

```bash
cd android
gradle wrapper --gradle-version 8.7   # une seule fois, si gradlew est absent
./gradlew assembleDebug               # APK de test
./gradlew assembleRelease             # APK release, signé avec la clé de debug
```

## Compilation continue

`.github/workflows/android.yml` compile les deux APK à chaque modification de
`android/`, vérifie le contenu réel de l'APK (signature, permission d'affichage
présente, permission réseau absente, service d'écoute présent), publie l'artefact
`centre-notifications-apk` et joint les fichiers à la Release quand un tag est poussé :

```bash
git tag v2.0.0
git push origin v2.0.0
```

## Structure

```
gemini-code-1795117363618.html          maquette web (référence de design)
android/app/src/main/AndroidManifest.xml   aucune permission réseau
android/.../NotificationRelay.java         écoute et réécriture des notifications
android/.../StyledNotifier.java            carte du centre dans le volet Android
android/.../IslandOverlay.java             pastille au-dessus des applications
android/.../LockScreenActivity.java        écran verrouillé du centre
android/.../RecentStore.java               derniers messages affichés sur l'écran verrouillé
android/.../BoardActivity.java             page du centre en plein écran (écran verrouillé)
android/.../CenterBridge.java              passerelle notifications réelles vers la page
android/.../HomeActivity.java              écran d'accueil (mode lanceur)
android/.../ActionReceiver.java            action « Effacer »
android/.../CenterPrefs.java               option de remplacement
android/.../MainActivity.java              écran de réglage (aucun écran de consultation)
android/app/src/main/res/layout/           carte de notification et écran de réglage
.github/workflows/android.yml              compilation et vérification de l'APK
```
