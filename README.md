# Leader Engineering Services — Gestion de chantier

![Leader Engineering Services](assets/logo-leader-engineering.png)

> Logo bleu et jaune « Logo d’ingénierie et construction en bleu et jaune.png » déposé sur GitHub, détouré (fond transparent) et optimisé pour le web.

Application web de **gestion du personnel et du pointage** pour une entreprise du BTP
(Construction Générale). Elle couvre le pointage quotidien, les heures supplémentaires
et le suivi mensuel du personnel de chantier — techniciens maçons, coffreurs,
ferrailleurs et main-d'œuvre — avec des montants en **FCFA**.

## Deux espaces

### 1. Interface de base — le personnel (à l'ouverture, sans code)

À l'ouverture, l'application affiche un **tableau moderne du pointage du jour** avec
**tous les profils** de l'équipe. Chaque membre retrouve sa ligne et n'a que deux champs :

- **Arrivée** — saisie libre à tout moment ;
- **Départ** — la saisie **s'ouvre à 17h30** (fin de journée standard du profil) ;
  avant, un message rappelle « Départ à partir de 17h30 ». Impossible de partir
  sans avoir pointé son arrivée.

Les pointages enregistrés partent **automatiquement** dans le tableau de bord
administrateur. Un bouton discret **« Administration »** (en bas à droite) fait
apparaître la page de connexion.

Design moderne : typographie Space Grotesk / Manrope, palette indigo, **logo de
l'entreprise** (`assets/logo-leader-engineering.png`, fond transparent) affiché en grand (72 px, 60 px dans la barre latérale) dans tous les écrans, éclairci pour rester lisible sur fond sombre
(base, connexion, espace admin), cartes arrondies avec ombres douces, thème
clair/sombre, responsive mobile (tableau empilé, navigation en barre basse). La page
de connexion a pour fond une **photo de chantier BTP** (`assets/chantier-btp.jpg`).

### 2. Espace administrateur (protégé)

La page de connexion offre un seul accès — **Administrateur** — et tout se gère depuis
cet espace (y compris la fiche « Mon mois » de chaque employé) :

| Rôle | Accès | Défaut |
| --- | --- | --- |
| **Administrateur** (code) | Tous les modules et toutes les données | `admin` |

L'administrateur retrouve les modules complets :

| Module | Ce qu'il permet |
| --- | --- |
| **Tableau de bord** | Effectif actif, présents du jour, heures sup., masse salariale, graphique de coût journalier, synthèse par poste, classement des heures sup., pointages du jour, et **statistiques par employé : nombre total de jours travaillés et montant à verser (salaire de base uniquement, hors coût des heures supplémentaires)** — avec bouton **Imprimer** (le PDF ne contient que l'état récapitulatif : Nom, Poste, Jours, Heures sup., Base FCFA, Suppl. FCFA, Total FCFA + totaux) et **États généraux** |
| **États généraux** (bouton du tableau de bord) | Aperçu, **impression (PDF)** et **téléchargement CSV** par profil : **Nom, Poste, Jours, Heures sup., Base FCFA, Suppl. FCFA, Total FCFA** + ligne de **totaux** |
| **Personnel** | Fiches complètes (photo, horaires prévus, tarifs), création / modification / désactivation |
| **Pointages** | Saisie et correction des pointages jour par jour |
| **Paie** | Synthèse mensuelle par employé + détail |
| **Paramètres** | Horaires standard, tarifs, règles de calcul des heures sup. |

Un bouton « Retour » sur la page de connexion ramène au personnel ; « Quitter »
depuis l'admin ramène également à l'interface de base.

Toute l'application tient dans **un seul fichier** (`index.html`) : HTML + CSS + JavaScript
vanilla, sans build ni dépendance à installer. Les données sont conservées dans le
**navigateur** (`localStorage`), ce qui suffit pour un usage sur site ou en petite équipe.

## Démarrage

Aucune installation requise :

```bash
# option 1 : ouvrir directement le fichier
open index.html            # macOS
xdg-open index.html        # Linux

# option 2 : servir en local (recommandé pour les photos / mobile)
python3 -m http.server 8000
# puis http://localhost:8000
```

## Structure du projet

```
.
├── index.html                        # application complète (HTML/CSS/JS unique)
├── assets/
│   ├── logo-leader-engineering.png   # logo bleu/jaune de l'entreprise (dépôt GitHub, fond transparent) — aussi favicon
│   └── chantier-btp.jpg              # photo de chantier (fond de la page de connexion)
└── README.md
```

## Notes techniques

- **Persistance** : `localStorage`, clés `les-personnel-v1` (données) et `les-theme` (thème).
- **Postes** : `macon`, `coffreur`, `ferrailleur`, `mo` (main-d'œuvre), chacun avec son tarif jour et heure sup.
- **Règles de calcul** : journée standard 07:30 → 17:30 par défaut ; heures au-delà de la fin de service comptées en heures sup. ; deux modes (intégrale `full` ou `prorate`).
- **Export États généraux** : fichier CSV (séparateur `;`, compatible Excel FR) nommé `etats-generaux-AAAA-MM.csv`.
- **Sans backend** : pour plusieurs appareils ou sauvegarde collective, il faudrait ajouter un serveur ; l'application est conçue pour rester déployable statiquement (GitHub Pages, nginx, clé USB…).

## Déploiement

Le dépôt est prêt pour **GitHub Pages** : après activation (Settings → Pages → Deploy from branch),
l'application est servie à la racine via `index.html`.

## Auteur

Développé pour **Leader Engineering Services — Construction Générale**.
