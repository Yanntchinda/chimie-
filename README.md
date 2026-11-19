# Centre de notifications

Un centre de notifications façon iOS, avec trois blocs : réponse express, balayage
(supprimer / épingler) et aperçu par appui long, des fils regroupés par application
avec priorité, et un fil continu dans la Dynamic Island.

Depuis la version Android, le même écran est empaqueté dans une application native qui
lit les **notifications réelles du téléphone** : WhatsApp, Messages, Telegram, Gmail et
les autres apparaissent dans le centre, où l'on peut y répondre, les épingler ou les
effacer — la réponse part dans l'application d'origine. Aucun accès réseau n'est demandé.

## Installer l'application

Guide détaillé, avec code à scanner : [installation/INSTALLATION.md](installation/INSTALLATION.md).

1. Ouvrir les [Releases](../../releases) du dépôt.
2. Télécharger `app-release.apk` sur le téléphone.
3. Ouvrir le fichier téléchargé et autoriser l'installation depuis cette source
   (Android demande cette confirmation pour toute application hors Play Store).
4. Lancer **Centre de notifications**, puis, dans le volet ouvert par le bouton **+**,
   toucher **Autoriser l'accès aux notifications** et activer **Centre de notifications**
   dans la liste Android. Sans cette étape, le centre reste vide : il ne reçoit que ce
   que ce service lui transmet.

L'application se présente en plein écran, sans barre d'adresse ni de navigation.
Fermée puis rouverte, elle retrouve les fils, les épingles et les rappels tels quels.

## D'où viennent les notifications

Le service d'écoute Android (`NotificationListenerService`) transmet au centre les
notifications émises par les autres applications, avec le nom de l'application, le
titre et le texte. Elles sont ensuite affichées, regroupées en fils, exactement comme
les notifications de démonstration de la version web.

Le manifeste ne déclare **ni `INTERNET` ni `POST_NOTIFICATIONS`** : l'application ne
peut ni joindre un serveur, ni publier dans la barre d'état. Ce qui est affiché vient
du téléphone et ne sort pas de l'appareil (état conservé dans le `localStorage` de la
page embarquée).

Actions réelles, dans les deux sens :

- **Répondre** dans une carte envoie le texte à la notification d'origine, quand elle
  propose une réponse directe ; sinon le centre le signale au lieu de faire semblant.
- **Effacer** une carte retire aussi la notification correspondante du téléphone.

## Utilisation

- **Double toucher** sur une carte : ouvrir la réponse et répondre sans quitter l'écran.
- **Glisser vers la gauche** : supprimer. **Vers la droite** : épingler en tête.
- **Appui long** (460 ms) : aperçu de l'application concernée.
- **Bouton +** : volet de contrôle, accès aux notifications, envoi de messages de
  démonstration, effacement.

Les explications d'usage restent dans ce volet, jamais dans le téléphone lui-même.

## Construire soi-même

```bash
cd android
gradle wrapper --gradle-version 8.7   # une seule fois, si gradlew est absent
./gradlew assembleDebug               # APK de test
./gradlew assembleRelease             # APK release, signé avec la clé de debug
```

La page web est copiée dans les assets avant la compilation par la tâche
`syncWebAssets` (`app/build.gradle`) : la version web et la version Android ne peuvent
pas diverger.

## Compilation continue

`.github/workflows/android.yml` compile les deux APK à chaque modification de
`android/` ou de la page, les publie comme artefact `centre-notifications-apk`, et
les joint à la Release quand un tag est poussé :

```bash
git tag v1.1.0
git push origin v1.1.0
```

## Structure

```
gemini-code-1795117363618.html        écran complet (HTML/CSS/JS, aucune dépendance)
android/settings.gradle               modules et dépôts
android/app/build.gradle              compilation, tâche syncWebAssets
android/app/src/main/AndroidManifest.xml   aucune permission
android/app/src/main/java/.../MainActivity.java   WebView plein écran, fichier local
android/app/src/main/java/.../NotificationRelay.java   écoute des notifications réelles
android/app/src/main/java/.../CenterBridge.java   passerelle entre Android et la page
android/app/src/main/res/             thème sombre, icône adaptative
.github/workflows/android.yml         compilation et publication des APK
```
