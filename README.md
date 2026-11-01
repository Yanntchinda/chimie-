# Leader Engineering Services — Gestion de chantier

![Leader Engineering Services](assets/logo-leader-engineering.jpg)

Application web de **gestion du personnel et de la paie** pour une entreprise du BTP
(Construction Générale). Elle couvre le pointage quotidien, les heures supplémentaires
et la paie mensuelle du personnel de chantier — techniciens maçons, coffreurs,
ferrailleurs et main-d'œuvre — avec des montants en **FCFA**.

Toute l'application tient dans **un seul fichier** (`index.html`) : HTML + CSS + JavaScript
vanilla, sans build ni dépendance à installer. Les données sont conservées dans le
**navigateur** (`localStorage`), ce qui suffit pour un usage sur site ou en petit équipe.

## Fonctionnalités

### Espace administrateur

| Module | Ce qu'il permet |
| --- | --- |
| **Tableau de bord** | Effectif actif, présents du jour, coût journalier, heures sup. du mois ; pointages du jour en direct |
| **Personnel** | Fiches employés (photo, poste, identifiant, horaires prévus, tarif du jour et de l'heure sup.), création / modification / désactivation |
| **Pointages** | Saisie du début et de la fin de service par jour (navigation jour précédent / suivant, remplissage des horaires prévus en un clic) |
| **Paie** | Synthèse mensuelle par employé : jours travaillés, heures, heures sup., gains ; détail et export copiable |
| **Paramètres** | Horaires standard, tarifs par poste (jour + heure sup.), mode de calcul des heures sup. (intégrale ou au prorata), code administrateur, simulateur de coût, données d'exemple |

### Espace personnel

Chaque employé se connecte avec son **identifiant** (ex. `LES-001`) et accède à :

- la prise de **service** (pointage entrée / sortie) depuis son téléphone ;
- **son mois** : jours présents, heures effectuées, heures sup. et gains en cours.

### Confort d'usage

- Thème clair / sombre (automatique ou forcé).
- Interface responsive : barre de navigation basse sur mobile, tableaux empilés.
- Données d'exemple (8 profils + 3 semaines de pointages) pour tester, supprimables en un clic.
- Persistance locale : rien ne part sur un serveur, les photos restent dans le navigateur.

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

### Connexions

| Rôle | Accès | Identifiant par défaut |
| --- | --- | --- |
| Administrateur | Tous les modules | code `admin` (modifiable dans Paramètres) |
| Personnel | Pointage et relevé de mois | `LES-001` … `LES-008` (données d'exemple) |

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
- **Export paie** : copie presse-papier au format texte (tableau prêt à coller).
- **Sans backend** : pour plusieurs appareils ou sauvegarde collective, il faudrait ajouter un serveur ; l'application est conçue pour rester déployable statiquement (GitHub Pages, nginx, clé USB…).

## Déploiement

Le dépôt est prêt pour **GitHub Pages** : après activation (Settings → Pages → Deploy from branch),
l'application est servie à la racine via `index.html`.

## Auteur

Développé pour **Leader Engineering Services — Construction Générale**.
