# Installer l'application

Le fichier à installer est un APK Android : `app-release.apk`, joint à la version
[v1.2.0](../../releases/tag/v1.2.0).

![QR vers l'APK](qr-android.png)

Scannez ce code avec l'appareil photo du téléphone : le téléchargement démarre
directement sur le fichier. Le lien écrit, si besoin :
`https://github.com/Yanntchinda/chimie-/releases/download/v1.2.0/app-release.apk`

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
6. Toucher le bouton **+** en bas à droite, puis **Autoriser l'accès aux
   notifications** : Android ouvre la liste des services d'accès. Activer
   **Centre de notifications**, revenir en arrière. Le volet affiche alors
   « accès accordé ».

L'application apparaît ensuite dans le tiroir d'applications sous le nom
**Centre de notifications**, avec l'icône cloche.

### Si Play Protect bloque l'installation

L'application n'est pas signée par un développeur enregistré auprès de Google :
Play Protect la traite donc comme une source inconnue. Deux chemins, du plus doux au
plus radical.

**1. Forcer l'installation sans rien désactiver** (essayer d'abord)

- Sur l'écran d'avertissement, toucher **Plus de détails**, puis **Installer quand
  même**. Ce bouton existe sur la plupart des téléphones récents.
- S'il est absent, ou si l'écran affiche « Application bloquée par Play Protect »,
  passer à l'étape 2.

**2. Désactiver Play Protect le temps de l'installation**

1. Ouvrir **Play Store**, toucher la photo de profil en haut à droite.
2. **Play Protect** → icône **Paramètres** (roue dentée, en haut à droite).
3. Désactiver **Analyser les applications avec Play Protect**.
4. Revenir à l'APK, le toucher, **Installer**.
5. Une fois l'application installée et lancée, **réactiver** l'option : elle protège
   le reste du téléphone.

Selon la marque, le même réglage est aussi ici : **Paramètres → Sécurité → Play
Protect** (Samsung : Sécurité et confidentialité → autres paramètres de sécurité).

**3. Autoriser la source du fichier**

Indépendamment de Play Protect, Android exige d'autoriser l'application qui ouvre
l'APK (Chrome, Fichiers, Gestionnaire de fichiers) :

**Paramètres → Applications → Accès spéciaux → Installer des applications inconnues**
→ choisir Chrome (ou Fichiers) → **Autoriser depuis cette source**.

### Si Android refuse l'installation

- « Application non installée » alors qu'une ancienne version est présente :
  désinstaller l'ancienne d'abord (les données du centre seront perdues).
- « Application non installée » sans ancienne version : le fichier a souvent été
  tronqué au téléchargement. Retélécharger, en vérifiant la taille : 38 119 octets.
- Le fichier s'ouvre comme une archive au lieu de s'installer : il a été renommé en
  `.zip` par le navigateur ; le renommer en `app-release.apk`.

## iPhone

iOS n'accepte pas les APK. La même page s'installe comme application web, sans rien
perdre des fonctions du centre :

1. Ouvrir la page du projet dans **Safari** (pas Chrome).
2. Bouton **Partager**, puis **Sur l'écran d'accueil**.
3. Lancer l'icône créée : la page s'ouvre en plein écran, sans barre d'adresse, et
   conserve les notifications entre deux ouvertures.

## Après l'activation

Les notifications des autres applications apparaissent dans le centre, avec le nom de
l'application, l'expéditeur et le texte. Elles restent groupées en fils, et :

- **répondre** dans une carte envoie la réponse à l'application d'origine ;
- **effacer** une carte retire la notification du téléphone ;
- un message reçu pendant que l'application est fermée est conservé et s'affiche à
  la réouverture.

## Ce que l'application demande

Un seul accès : lire les notifications. Pas d'accès réseau (aucune permission
`INTERNET`), donc rien ne peut partir de l'appareil, et pas de publication dans la
barre d'état : le centre intégré reste le seul endroit où l'on lit et traite ces
notifications.
