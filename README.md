# Leader Engineering Services — Gestion de chantier

![Leader Engineering Services](assets/logo-leader-engineering.jpg)

Application web de **gestion du personnel et du pointage** pour une entreprise du BTP
(Construction Générale). Elle couvre le pointage quotidien, les heures supplémentaires
et le suivi mensuel du personnel de chantier — techniciens maçons, coffreurs,
ferrailleurs et main-d'œuvre — avec des montants en **FCFA**.

## Deux espaces

### 1. Interface de base — le personnel (à l'ouverture)

Quand on ouvre l'application, on voit **les profils de toute l'équipe**. Chaque membre
n'a que deux champs à remplir :

- **Arrivée** — heure de prise de service ;
- **Départ** — heure de fin.

Rien d'autre (pas de tarif, pas de mot de passe). Les pointages enregistrés partent
automatiquement dans le tableau de bord administrateur.

Un bouton discret **« Administration »** (en bas à droite) fait apparaître la page
de connexion.

### 2. Espace administrateur (protégé)

La page de connexion offre deux accès, comme dans l'application d'origine :

| Rôle | Accès | Défaut |
| --- | --- | --- |
| **Administrateur** (code) | Tous les modules et toutes les données | `admin` |
| **Personnel** (identifiant) | Sa fiche « Mon mois » | `LES-001` … `LES-008` (exemples) |

L'administrateur retrouve les modules complets :

| Module | Ce qu'il permet |
| --- | --- |
| **Tableau de bord** | Effectif actif, présents du jour, heures sup., masse salariale, graphique de coût journalier, synthèse par poste, classement des heures sup., pointages du jour |
| **États généraux** (bouton du tableau de bord) | Aperçu et **téléchargement CSV** par profil : jours, **heures totales**, **heures supplémentaires**, **salaire normal**, **salaire avec heures supplémentaires** |
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
│   └── logo-leader-engineering.jpg   # logo de l'entreprise
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
