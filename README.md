# Leader Engineering Services — Gestion de chantier

![Leader Engineering Services](assets/logo-leader-engineering.jpg)

Application web de **gestion du personnel et du pointage** pour une entreprise du BTP
(Construction Générale). Elle couvre le pointage quotidien, les heures supplémentaires
et le suivi mensuel du personnel de chantier — techniciens maçons, coffreurs,
ferrailleurs et main-d'œuvre — avec des montants en **FCFA**.

**Aucune connexion, aucun mot de passe** : l'application s'ouvre directement sur le
tableau de bord. Deux sections seulement :

- **Tableau de bord** — indicateurs du jour et de la période ;
- **Personnel** — le profil de chaque membre.

Toute l'application tient dans **un seul fichier** (`index.html`) : HTML + CSS + JavaScript
vanilla, sans build ni dépendance à installer. Les données sont conservées dans le
**navigateur** (`localStorage`), ce qui suffit pour un usage sur site ou en petit équipe.

## Fonctionnalités

### Tableau de bord

- **Indicateurs** : effectif actif, présents du jour (avec barre de progression),
  heures supplémentaires et masse salariale sur 30 jours ou le mois en cours.
- **Graphique de coût journalier** (base + heures sup., 14 derniers jours).
- **Synthèse par poste** : effectif, jours pointés, montant.
- **Heures sup. par employé** : classement avec montants.
- **Pointages du jour** : prise de service et sortie **éditables directement**
  (heure par heure, ou « Tous présents, horaires prévus » en un clic,
  ou « Horaires prévus » ligne par ligne).

### Personnel — le profil de chaque membre

- Fiches avec **photo**, nom, identifiant, poste, horaires prévus,
  tarif de la journée et de l'heure sup.
- **Créer / modifier / désactiver / supprimer** un profil (avec confirmation).
- Recherche par nom ou identifiant, filtrage par poste.
- **Détail du mois** de chaque membre : journée par journée (début, sortie,
  base, heures sup., total) et récapitulatif du mois.

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

### Accès

Aucune connexion : l'application ouvre directement le tableau de bord.
Les identifiants affichés (`LES-001` … `LES-008`, données d'exemple) servent
uniquement à reconnaître chaque membre dans les listes et le détail du mois.

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
- **Sans backend** : pour plusieurs appareils ou sauvegarde collective, il faudrait ajouter un serveur ; l'application est conçue pour rester déployable statiquement (GitHub Pages, nginx, clé USB…).

## Déploiement

Le dépôt est prêt pour **GitHub Pages** : après activation (Settings → Pages → Deploy from branch),
l'application est servie à la racine via `index.html`.

## Auteur

Développé pour **Leader Engineering Services — Construction Générale**.
