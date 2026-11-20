# Installer le centre de notifications

Le fichier à installer est un APK Android : `app-release.apk`.

![QR vers l'APK](qr-android.png)

Scannez ce code avec l'appareil photo du téléphone : le téléchargement démarre
directement sur le fichier. Le lien écrit, si besoin :
`https://github.com/Yanntchinda/chimie-/releases/latest/download/app-release.apk`

## Attention : trois fichiers différents

| Ce que vous voyez | Ce que c'est | À faire |
|---|---|---|
| `app-release.apk` (page des versions) | l'application | **c'est celui-ci qu'il faut installer** |
| `centre-notifications-apk.zip` (onglet Actions) | l'archive produite par la compilation, qui contient les deux APK | la décompresser, puis installer `app-release.apk` de l'intérieur |
| le ZIP du code source (bouton vert **Code** → Download ZIP) | le projet, pas l'application | ne pas installer : Android le refuse |

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
3. Bloc **Îlot par-dessus les applications** → **Autoriser l'affichage par-dessus**,
   puis activer **Centre de notifications** dans la liste. C'est ce qui permet à la
   pastille de descendre du haut de l'écran pendant que vous utilisez une autre
   application. Facultatif : sans lui, les cartes restent dans les notifications.
4. Les trois blocs affichent alors leur état accordé. L'application peut être
   refermée définitivement.

Sous les blocs, une ligne indique combien de messages ont été reçus et à quelle
heure : c'est le moyen le plus rapide de savoir si l'écoute fonctionne réellement.

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

## Ce qui se passe une fois autorisé

1. L'écran de réglage affiche « Accordé » et « Autorisé ». On peut refermer
   l'application : elle n'a plus besoin d'être ouverte.
2. Les notifications déjà présentes dans le volet sont réécrites immédiatement.
   Ensuite, chaque nouveau message suit le même chemin :
3. Le téléphone sonne ou vibre **une fois**, comme d'habitude (le son vient de
   l'application d'origine au moment où elle publie son message).
4. Une pastille noire descend du haut de l'écran, par-dessus l'application en cours :
   pastille de couleur, expéditeur, texte. Elle se referme au bout de quelques
   secondes ; la toucher la referme tout de suite.
5. La notification d'origine est retirée et remplacée par la carte du centre :
   pastille, expéditeur, texte, heure. Elle apparaît en bannière en haut de l'écran
   et sur l'écran verrouillé.
6. Un nouveau message dans la même conversation **met à jour la même carte** au lieu
   d'en empiler une deuxième.
7. Les notifications sans contenu (lecteur de musique, téléchargement en cours,
   appels en cours) ne sont pas touchées.

### Dans la carte

- **Répondre** (bouton de la notification, ou réponse directe d'Android) : le texte
  part dans l'application d'origine.
- **Toucher la carte** : ouvre l'application qui a envoyé le message.
- **Effacer** : retire la carte et la notification d'origine.
- **Écran verrouillé** : les cartes y apparaissent comme toute autre notification ;
  leur contenu s'affiche selon le réglage Android « Afficher le contenu sensible ».

### Si plus rien n'apparaît

- Vérifier les états dans l'application : accès aux notifications accordé, affichage
  autorisé, et la ligne « messages reçus » qui doit augmenter quand un message arrive.
- Sur Xiaomi, Tecno, Infinix, Oppo : désactiver l'optimisation de la batterie pour
  **Centre de notifications** (Paramètres → Applications → Centre de notifications →
  Batterie → **Sans restriction**). Ces surcouches arrêtent sinon les services
  d'arrière-plan.
- Envoyer un message depuis un autre téléphone pour vérifier.

### Option de remplacement

Activée par défaut : seule la carte du centre reste dans le volet. Désactivée, la
notification d'origine reste visible à côté. Le réglage se trouve dans l'écran de
l'application.

## Prendre la main sur le système de notifications

**Ne désactivez pas les notifications des applications** (WhatsApp, Messages,
Telegram…). Le centre lit leurs notifications pour les réécrire : sans elles, il n'a
plus rien à afficher et votre téléphone devient muet. C'est le piège à éviter.

La prise de contrôle se fait à l'intérieur du centre :

1. Dans l'application, laisser **Remplacer les notifications d'origine** activé
   (c'est le réglage par défaut). Chaque message publié est retiré du volet et
   remplacé par la carte du centre, dans la seconde. Seules les cartes du centre
   restent visibles : c'est cela, prendre la main.
2. Pour alléger la barre d'état : **Paramètres → Notifications → Barre d'état**,
   désactiver l'affichage des icônes de notification (le libellé varie selon la
   marque). Les cartes restent dans le volet, les icônes disparaissent du haut.
3. Pour les sons : ils viennent de l'application d'origine au moment de la
   publication. Les couper application par application
   (Paramètres → Applications → WhatsApp → Notifications → catégories → Son) laisse
   le centre afficher les cartes en silence.

**Ce qu'Android interdit sans root** : supprimer le volet de notifications lui-même,
ou le remplacer par un volet maison. Ni une application normale ni un service
d'écoute n'ont ce droit ; seuls un appareil rooté ou un appareil en mode kiosque
(« device owner », configuré par ADB sur un appareil dédié) le permettent. Le centre
s'approche du résultat : le volet du téléphone ne contient plus que ses cartes.

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
- **Affichage par-dessus les autres applications** : dessine la pastille du haut de
  l'écran quand un message arrive. Facultatif.
- **Aucun accès réseau** : la permission `INTERNET` n'est pas déclarée, rien ne peut
  sortir de l'appareil.

## iPhone

iOS n'accepte pas les APK et n'autorise aucune application à réécrire les
notifications des autres. Sur iPhone, seules les notifications de l'application
elle-même peuvent être personnalisées : ce centre n'existe donc que sur Android.
