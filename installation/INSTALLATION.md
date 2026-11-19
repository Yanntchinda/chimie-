# Installer le centre de notifications

Le fichier à installer est un APK Android : `app-release.apk`.

![QR vers l'APK](qr-android.png)

Scannez ce code avec l'appareil photo du téléphone : le téléchargement démarre
directement sur le fichier. Le lien écrit, si besoin :
`https://github.com/Yanntchinda/chimie-/releases/latest/download/app-release.apk`

## Android : installer

1. **Télécharger** en scannant le code ci-dessus, ou depuis la page des versions.
2. Chrome affiche « Ce type de fichier peut endommager votre appareil » : c'est
   l'avertissement habituel pour tout APK. Toucher **Télécharger quand même**.
3. Ouvrir le fichier téléchargé.
4. Android demande l'autorisation « Installer des applications inconnues » pour
   Chrome ou Fichiers : **Autoriser**, revenir, puis **Installer**.
5. Play Protect peut prévenir qu'il ne connaît pas l'application : **Installer
   quand même**, puis **Ouvrir**.

## Android : autoriser, une seule fois

L'application s'ouvre sur un écran de réglage. Elle n'a pas d'écran de consultation :
c'est le volet de notifications du téléphone qui devient le centre.

1. Bloc **Accès aux notifications** → **Autoriser l'accès aux notifications**.
   Android ouvre la liste des services d'accès : activer
   **Centre de notifications**, revenir en arrière.
2. Bloc **Affichage des notifications** → **Autoriser l'affichage**, puis accepter
   la demande d'Android (Android 13 et plus).
3. Les deux blocs affichent alors « Accordé » et « Autorisé ». L'application peut
   être refermée définitivement.

À partir de là, chaque message reçu — WhatsApp, Messages, Telegram, Gmail, autre —
apparaît sous forme de carte sombre dans les notifications du téléphone.

## Si Android affiche « paramètre restreint »

Depuis Android 13, un système protège les accès sensibles : une application
installée hors Play Store ne peut pas activer l'accès aux notifications tant que
vous ne l'avez pas autorisée explicitement. Le commutateur apparaît grisé, avec le
message « Paramètre restreint — pour votre sécurité, ce paramètre est actuellement
indisponible ».

La marche à suivre, une seule fois :

1. **Paramètres** → **Applications** → **Voir toutes les applications** →
   **Centre de notifications**.
2. En haut à droite de cet écran, les **trois points ⋮** → **Autoriser les
   paramètres restreints**.
3. Confirmer avec l'empreinte ou le code de déverrouillage.
4. Revenir à l'écran de l'application, puis **Autoriser l'accès aux notifications** :
   le commutateur **Centre de notifications** est maintenant actif.

Si les trois points n'apparaissent pas :

- vérifier que l'on est bien sur la fiche de l'application (nom, version, stockage),
  et non sur son écran de notifications ;
- Samsung : Applications → Centre de notifications → ⋮ → Autoriser les paramètres
  restreints ;
- Xiaomi / Redmi : Applications → Gérer les applications → Centre de notifications →
  ⋮ → Autoriser les paramètres restreints ;
- certains appareils n'affichent cette entrée qu'après avoir touché une première fois
  le commutateur grisé (le message peut proposer **Paramètres**, qui ouvre la fiche).

Dernier recours, avec un ordinateur et le câble USB : installer l'APK par ADB
(`adb install -r app-release.apk`). Une installation par ADB n'est pas marquée comme
« source inconnue » et échappe à cette restriction.

## Utiliser

- **Répondre** depuis la notification (bouton de réponse, ou réponse directe
  d'Android) : le texte part dans l'application d'origine.
- **Toucher la carte** : ouvre l'application qui a envoyé le message.
- **Effacer** : retire la carte et la notification d'origine.
- **Écran verrouillé** : les cartes y apparaissent comme toute autre notification ;
  leur contenu s'affiche selon le réglage Android « Afficher le contenu sensible ».

### Option de remplacement

Activée par défaut : seule la carte du centre reste dans le volet. Désactivée, la
notification d'origine reste visible à côté. Le réglage se trouve dans l'écran de
l'application.

## Si Play Protect bloque l'installation

L'application n'est pas signée par un développeur enregistré auprès de Google :
Play Protect la traite donc comme une source inconnue.

**1. Forcer l'installation sans rien désactiver** (essayer d'abord)

- Sur l'écran d'avertissement, toucher **Plus de détails**, puis **Installer quand
  même**.

**2. Désactiver Play Protect le temps de l'installation**

1. Ouvrir **Play Store**, toucher la photo de profil en haut à droite.
2. **Play Protect** → icône **Paramètres** (roue dentée, en haut à droite).
3. Désactiver **Analyser les applications avec Play Protect**.
4. Revenir à l'APK, le toucher, **Installer**.
5. Une fois l'application installée, **réactiver** l'option.

Sur certaines marques : **Paramètres → Sécurité → Play Protect** (Samsung : Sécurité
et confidentialité → autres paramètres de sécurité).

**3. Autoriser la source du fichier**

**Paramètres → Applications → Accès spéciaux → Installer des applications inconnues**
→ choisir Chrome (ou Fichiers) → **Autoriser depuis cette source**.

## Si l'installation échoue

- « Application non installée » alors qu'une ancienne version est présente :
  la désinstaller d'abord.
- « Application non installée » sans ancienne version : fichier tronqué au
  téléchargement. Le retélécharger ; sa taille est indiquée dans les notes de la
  version.
- Le fichier s'ouvre comme une archive : le navigateur l'a renommé en `.zip`.
  Le renommer en `app-release.apk`.

## Ce que l'application demande

- **Accès aux notifications** : lit les messages reçus pour les réafficher dans le
  style du centre et leur ajouter l'action de réponse.
- **Affichage des notifications** : nécessaire pour que ces cartes apparaissent
  dans le volet.
- **Aucun accès réseau** : la permission `INTERNET` n'est pas déclarée, rien ne peut
  sortir de l'appareil.

## iPhone

iOS n'accepte pas les APK et n'autorise aucune application à réécrire les
notifications des autres. Sur iPhone, seules les notifications de l'application
elle-même peuvent être personnalisées : ce centre n'existe donc que sur Android.
