# Centre de notifications

Un centre de notifications façon iOS, avec trois blocs : réponse express, balayage
(supprimer / épingler) et aperçu par appui long, des fils regroupés par application
avec priorité, et un fil continu dans la Dynamic Island.

Depuis la version Android, le même écran est empaqueté dans une application native :
une fois installée, **c'est le seul canal par lequel une notification arrive**. Aucun
réseau, aucune notification système.

## Installer l'application

Guide détaillé, avec code à scanner : [installation/INSTALLATION.md](installation/INSTALLATION.md).

1. Ouvrir les [Releases](../../releases) du dépôt.
2. Télécharger `app-release.apk` sur le téléphone.
3. Ouvrir le fichier téléchargé et autoriser l'installation depuis cette source
   (Android demande cette confirmation pour toute application hors Play Store).
4. Lancer **Centre de notifications**.

L'application se présente en plein écran, sans barre d'adresse ni de navigation.
Fermée puis rouverte, elle retrouve les fils, les épingles et les rappels tels quels.

## Pourquoi aucun canal externe

Le manifeste Android ne déclare **ni `INTERNET` ni `POST_NOTIFICATIONS`** : le système
d'exploitation refuse par construction l'accès au réseau et l'envoi de notifications
système. Tout ce qui arrive dans le centre y arrive parce que l'application le reçoit
elle-même, et les données restent sur l'appareil (`localStorage` de la page embarquée).

L'application peut donc être installée hors Play Store sans risque de captation :
rien ne peut s'afficher dans la barre d'état à sa place.

## Utilisation

- **Double toucher** sur une carte : ouvrir la réponse et répondre sans quitter l'écran.
- **Glisser vers la gauche** : supprimer. **Vers la droite** : épingler en tête.
- **Appui long** (460 ms) : aperçu de l'application concernée.
- **Bouton +** : volet de contrôle, envoi de messages de démonstration, effacement.

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
android/app/src/main/res/             thème sombre, icône adaptative
.github/workflows/android.yml         compilation et publication des APK
```
