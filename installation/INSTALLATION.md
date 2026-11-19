# Installer l'application

Le fichier à installer est un APK Android : `app-release.apk`, joint à la version
[v1.1.0](../../releases/tag/v1.1.0).

![QR vers l'APK](qr-android.png)

Scannez ce code avec l'appareil photo du téléphone : le téléchargement démarre
directement sur le fichier. Le lien écrit, si besoin :
`https://github.com/Yanntchinda/chimie-/releases/download/v1.1.0/app-release.apk`

## Android : cinq étapes

1. **Télécharger** en scannant le code ci-dessus, ou en ouvrant la page des versions
   dans Chrome sur le téléphone, puis en touchant `app-release.apk`.
2. Chrome affiche « Ce type de fichier peut endommager votre appareil ». C'est
   l'avertissement habituel pour tout APK : toucher **Télécharger quand même**.
3. Ouvrir le fichier téléchargé (notification de téléchargement, ou
   Paramètres → Téléchargements → `app-release.apk`).
4. Android demande l'autorisation « Installer des applications inconnues » pour
   Chrome ou Fichiers : **Autoriser**, revenir en arrière, puis **Installer**.
5. Play Protect peut prévenir qu'il ne connaît pas l'application : toucher
   **Installer quand même**. Puis **Ouvrir**.

L'application apparaît ensuite dans le tiroir d'applications sous le nom
**Centre de notifications**, avec l'icône cloche.

### Si Android refuse l'installation

- « Application non installée » alors qu'une ancienne version est présente :
  désinstaller l'ancienne d'abord (les données du centre seront perdues).
- « Application non installée » sans ancienne version : le fichier a souvent été
  tronqué au téléchargement. Retélécharger, en vérifiant la taille : 32 431 octets.
- Le fichier s'ouvre comme une archive au lieu de s'installer : il a été renommé en
  `.zip` par le navigateur ; le renommer en `app-release.apk`.

## iPhone

iOS n'accepte pas les APK. La même page s'installe comme application web, sans rien
perdre des fonctions du centre :

1. Ouvrir la page du projet dans **Safari** (pas Chrome).
2. Bouton **Partager**, puis **Sur l'écran d'accueil**.
3. Lancer l'icône créée : la page s'ouvre en plein écran, sans barre d'adresse, et
   conserve les notifications entre deux ouvertures.

## Ce que l'application installe

Aucune permission : ni accès réseau, ni notifications système. Une fois installée,
le centre intégré est le seul canal par lequel une notification peut arriver.
