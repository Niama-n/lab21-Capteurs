# Lab 21 : Capteurs embarqués Android

> **AppSENSOR** est une application Android conçue pour découvrir, analyser et expérimenter avec l'ensemble des périphériques matériels embarqués sur un smartphone.  

---
## Voila la demonstration


https://github.com/user-attachments/assets/b7d36de6-3d68-4539-a915-38d53d84aba2


## Table des matières

1. [Présentation](#présentation)
2. [Objectifs pédagogiques](#objectifs-pédagogiques)
3. [Fonctionnalités principales](#fonctionnalités-principales)
4. [Identité visuelle](#identité-visuelle)
5. [Arborescence du projet](#arborescence-du-projet)
6. [Architecture logicielle](#architecture-logicielle)
7. [Prérequis techniques](#prérequis-techniques)
8. [Guide d'installation pas à pas](#guide-dinstallation-pas-à-pas)
9. [Lancement et premiers tests](#lancement-et-premiers-tests)
10. [Tests sur émulateur](#tests-sur-émulateur)
11. [Composants techniques détaillés](#composants-techniques-détaillés)
12. [Permissions Android](#permissions-android)
13. [Dépannage courant](#dépannage-courant)

---

## Présentation

### Contexte du laboratoire

Ce laboratoire (**Lab 21 — Capteurs embarqués**) montre comment une application Android peut exploiter les capteurs intégrés d'un smartphone pour observer, mesurer et interpréter le monde physique. **AppSENSOR** en est la démonstration concrète : une interface unique regroupe l'ensemble des modules pédagogiques dans une navigation latérale (drawer), avec affichage en temps réel sous forme de fiches techniques, de courbes interactives ou de classifications de mouvement.

L'objectif n'est pas de produire une application commerciale, mais de **comprendre le chaînage complet** entre le matériel (capteur), le système Android (`SensorManager`), le traitement logiciel (filtrage, norme, fusion) et la restitution visuelle.

### Progression pédagogique en cinq parties

Le parcours proposé suit une montée en complexité progressive, chaque partie s'appuyant sur la précédente :

| Partie | Module AppSENSOR | Ce que l'étudiant apprend |
|--------|------------------|---------------------------|
| **1 — Découverte** | Device Peripherals | Lister tous les capteurs disponibles sur l'appareil, lire leurs propriétés techniques (résolution, plage, consommation, fabricant) |
| **2 — Visualisation** | Ambient Heat, Air Moisture, Proximity Range, Magnetic Field | Acquérir un flux de mesures, les tracer en courbe temps réel et gérer l'absence de capteur via une simulation interne |
| **3 — Mouvement linéaire** | Acceleration Axes, Gravity Vector, Rotation Rate | Manipuler des vecteurs 3D (X, Y, Z), calculer une norme et interpréter accélération, gravité et vitesse angulaire |
| **4 — Orientation & déplacement** | Step Tracker, Digital Heading | Mesurer les pas parcourus, fusionner accéléromètre et magnétomètre pour obtenir un azimut et une direction cardinale |
| **5 — Classification** | Motion Classifier | Appliquer une logique de reconnaissance d'activité (marche, saut, posture stable) à partir du signal accélérométrique |

Cette structuration reflète la logique du TP : on commence par **observer** ce que le téléphone possède, puis on **visualise** des grandeurs scalaires, on **exploite** les capteurs de mouvement, et enfin on **interprète** le comportement de l'utilisateur.

### Capteurs couverts

**AppSENSOR** interroge les périphériques suivants :

- **Environnement** : température ambiante, humidité relative, proximité, champ magnétique
- **Cinématique** : accéléromètre, gravité, gyroscope
- **Locomotion** : compteur de pas (`TYPE_STEP_COUNTER`)
- **Orientation** : boussole numérique (fusion accéléromètre + magnétomètre)
- **Activité** : classification heuristique à partir de l'accéléromètre

### Une solution volontairement pédagogique

La reconnaissance d'activité implémentée dans **Motion Classifier** repose sur des **seuils simples** (écart-type, pic d'amplitude, orientation des axes) et un filtre passe-bas pour isoler la composante gravitationnelle. Cette approche est adaptée à un contexte d'apprentissage : elle est lisible, déboguable et ne nécessite pas de jeu de données pré-entraîné.

> **Vers une application industrielle ou scientifique**, il faudrait aller plus loin :
> - collecter des **données réelles** sur plusieurs utilisateurs et plusieurs modèles de téléphones ;
> - **annoter** manuellement les activités (marche, course, assis, debout, véhicule…) ;
> - **entraîner** un modèle d'apprentissage automatique (réseau de neurones, forêt aléatoire, etc.) ;
> - **évaluer** la précision (rappel, F1-score) sur un jeu de test indépendant, en conditions variées (poche, main, brassard).
>
> AppSENSOR pose ainsi les fondations — acquisition, filtrage, fenêtre glissante — sur lesquelles un pipeline ML complet pourrait s'appuyer.

---

## Objectifs pédagogiques

| # | Objectif | Compétence visée |
|---|----------|------------------|
| 1 | **Cartographier le matériel** | Identifier chaque périphérique disponible et lire ses propriétés (résolution, consommation, plage) |
| 2 | **Acquérir des flux temps réel** | Enregistrer des `SensorEvent`, gérer le cycle de vie (`onResume` / `onPause`) |
| 3 | **Visualiser des signaux** | Tracer des courbes dynamiques via un composant `Canvas` personnalisé |
| 4 | **Traiter des vecteurs 3D** | Calculer des normes, filtrer la gravité, fusionner accéléromètre + magnétomètre |
| 5 | **Simuler l'absence matérielle** | Générer des données synthétiques quand un capteur est indisponible |
| 6 | **Gérer les permissions** | Demander `ACTIVITY_RECOGNITION` pour le compteur de pas (API 29+) |
| 7 | **Classifier un comportement** | Appliquer un filtre passe-bas et une fenêtre glissante pour détecter marche, saut, posture |

---

## Fonctionnalités principales

| Écran | Capteur Android | Rôle |
|-------|-----------------|------|
| Device Peripherals | `TYPE_ALL` | Catalogue complet avec fiche technique par périphérique |
| Ambient Heat | `TYPE_AMBIENT_TEMPERATURE` | Courbe de température + mode simulation |
| Air Moisture | `TYPE_RELATIVE_HUMIDITY` | Courbe d'humidité relative |
| Proximity Range | `TYPE_PROXIMITY` | Détection de proximité d'objet |
| Magnetic Field | `TYPE_MAGNETIC_FIELD` | Intensité du champ (norme du vecteur) |
| Acceleration Axes | `TYPE_ACCELEROMETER` | Axes X, Y, Z et magnitude |
| Gravity Vector | `TYPE_GRAVITY` | Composante gravitationnelle isolée |
| Rotation Rate | `TYPE_GYROSCOPE` | Vitesse angulaire en rad/s |
| Step Tracker | `TYPE_STEP_COUNTER` | Pas depuis le boot + pas de session |
| Digital Heading | Accéléromètre + Magnétomètre | Azimut et direction cardinale |
| Motion Classifier | `TYPE_ACCELEROMETER` | Détection : idle, marche, saut, posture |

---

## Identité visuelle

AppSENSOR utilise un thème **violet profond / ambre doré**, distinct des palettes industrielles classiques :

| Élément | Couleur | Usage |
|---------|---------|-------|
| Fond principal | `#1B1033` | Arrière-plan des écrans |
| Surface élevée | `#2A1A4E` | Barre d'outils, drawer, cartes |
| Accent violet | `#A855F7` | Titres secondaires, courbes |
| Accent ambre | `#FBBF24` | Titres principaux, point lumineux |
| Texte clair | `#F3E8FF` | Valeurs mesurées |
| Texte atténué | `#C4B5FD` | Détails et spécifications |

Les couleurs sont centralisées dans `res/values/colors.xml` et réutilisées via la classe utilitaire `UiPalette`.

---

## Arborescence du projet

```
lab21-Capteurs/                          ← Racine du dépôt
│
├── README.md                            ← Documentation du projet
├── settings.gradle                      ← Nom du module : AppSENSOR
├── build.gradle                         ← Configuration Gradle racine
├── gradle.properties
│
├── gradle/wrapper/
│   └── gradle-wrapper.properties
│
└── app/
    ├── build.gradle                     ← SDK 34, minSdk 26
    └── src/main/
        ├── AndroidManifest.xml          ← MainActivity + permission
        │
        ├── java/com/example/sensors/
        │   ├── MainActivity.java                  ← Point d'entrée, navigation drawer
        │   │
        │   ├── fragments/
        │   │   ├── SensorsListFragment.java       ← Liste des périphériques
        │   │   ├── SensorGraphFragment.java       ← Graphes scalaires / norme
        │   │   ├── MotionSensorFragment.java      ← Capteurs 3 axes
        │   │   ├── StepCounterFragment.java       ← Compteur de pas
        │   │   ├── CompassFragment.java           ← Boussole numérique
        │   │   └── ActivityRecognitionFragment.java ← Classification de mouvement
        │   │
        │   ├── utils/
        │   │   ├── UiPalette.java                 ← Constantes de couleurs
        │   │   ├── SensorFormatter.java           ← Formatage des fiches capteur
        │   │   ├── SignalMath.java                ← Normes 3D, lissage, écart-type (Welford)
        │   │   ├── MotionHeuristics.java          ← Classification de posture (enum)
        │   │   ├── SyntheticFeedGenerator.java    ← Flux simulé si capteur absent
        │   │   └── BearingMapper.java             ← Azimut → direction cardinale
        │   │
        │   └── views/
        │       └── LineChartView.java             ← Graphique Canvas personnalisé
        │
        └── res/
            ├── layout/
            │   └── activity_main.xml              ← DrawerLayout + Toolbar
            ├── menu/
            │   └── nav_drawer.xml                 ← Entrées de navigation
            └── values/
                ├── colors.xml                     ← Palette AppSENSOR
                ├── strings.xml                    ← Libellés (anglais)
                └── themes.xml                     ← Theme.AppSensor
```

---

## Architecture logicielle

```
┌─────────────────────────────────────────────────────────────┐
│                       MainActivity                          │
│  DrawerLayout + NavigationView + FragmentManager            │
└──────────────────────────┬──────────────────────────────────┘
                           │ openFragment()
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
│ SensorsList     │ │ SensorGraph  │ │ MotionSensor     │
│ Fragment        │ │ Fragment     │ │ Fragment         │
└────────┬────────┘ └──────┬───────┘ └────────┬─────────┘
         │                 │                   │
         ▼                 ▼                   ▼
┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
│ SensorFormatter │ │ LineChartView│ │ SignalMath       │
│                 │ │ + Synthetic  │ │ + SensorManager  │
│                 │ │   FeedGenerator│ │ (Android API)  │
└─────────────────┘ └──────────────┘ └──────────────────┘

┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
│ StepCounter     │ │ Compass      │ │ Activity         │
│ Fragment        │ │ Fragment     │ │ RecognitionFrag. │
└────────┬────────┘ └──────┬───────┘ └────────┬─────────┘
         │                 │                   │
         ▼                 ▼                   ▼
   Permission API    BearingMapper        SignalMath
   ACTIVITY_         + Rotation Matrix    + MotionHeuristics
   RECOGNITION       + getOrientation     (tampon circulaire)
```

**Flux de données typique :**

1. L'utilisateur sélectionne un écran dans le drawer.
2. `MainActivity` instancie le fragment correspondant.
3. Le fragment enregistre un `SensorEventListener` sur `SensorManager`.
4. Chaque `onSensorChanged` met à jour l'UI et/ou pousse un échantillon dans `LineChartView`.
5. À la pause (`onPause`), l'écouteur est désenregistré pour économiser la batterie.

---

## Prérequis techniques

| Composant | Version minimale |
|-----------|------------------|
| Android Studio | Hedgehog 2023.1.1 ou plus récent |
| JDK | 17+ recommandé (Gradle 9) — compatibilité source Java 8 |
| compileSdk | 34 |
| minSdk | 26 (Android 8.0 Oreo) |
| targetSdk | 34 |
| Appareil / Émulateur | API 26+ avec capteurs virtuels |

---

## Guide d'installation pas à pas

### Étape 1 — Récupérer le projet

```bash
# Option A : cloner le dépôt Git
git clone <url-du-depot> lab21-Capteurs
cd lab21-Capteurs

# Option B : décompresser l'archive
unzip AppSENSOR.zip -d lab21-Capteurs
cd lab21-Capteurs
```

### Étape 2 — Ouvrir dans Android Studio

1. Lancer **Android Studio**.
2. Cliquer sur **File → Open**.
3. Sélectionner le dossier `lab21-Capteurs`.
4. Attendre l'indexation initiale du projet.

### Étape 3 — Synchroniser Gradle

1. Menu **File → Sync Project with Gradle Files** (ou l'icône éléphant avec flèche).
2. Vérifier qu'aucune erreur n'apparaît dans l'onglet **Build**.
3. Si demandé, accepter le téléchargement du SDK Android 34.

### Étape 4 — Configurer un appareil cible

**Émulateur :**
1. **Tools → Device Manager → Create Device**.
2. Choisir un modèle (ex. Pixel 6) avec image système API 30+.
3. Activer les capteurs virtuels dans les paramètres de l'AVD.

**Appareil physique :**
1. Activer le **mode développeur** et le **débogage USB**.
2. Connecter le téléphone via câble USB.
3. Autoriser le débogage sur l'appareil.

### Étape 5 — Compiler et exécuter

1. Sélectionner la configuration **app** dans la barre d'outils.
2. Choisir l'émulateur ou l'appareil connecté.
3. Cliquer sur **Run ▶** (ou `Shift + F10`).
4. L'application **AppSENSOR** s'installe et démarre sur `MainActivity`.

### Étape 6 — Vérifier le bon fonctionnement

1. Ouvrir le drawer (icône ☰ en haut à gauche).
2. Naviguer vers **Device Peripherals** : la liste des capteurs doit s'afficher.
3. Tester **Ambient Heat** ou **Acceleration Axes** : une courbe doit apparaître.
4. Sur **Step Tracker** : accorder la permission si demandée.

---

## Lancement et premiers tests

| Action | Résultat attendu |
|--------|------------------|
| Ouvrir l'app | Écran catalogue des périphériques, thème violet/ambre |
| Sélectionner un capteur scalaire | Valeur numérique + courbe animée |
| Incliner le téléphone (accéléromètre) | Axes X/Y/Z mis à jour en direct |
| Approcher un objet (proximité) | Valeur bascule entre 0 et 5 cm |
| Marcher avec le téléphone | Motion Classifier affiche « Walking » |

---

## Tests sur émulateur

L'émulateur Android Studio propose un panneau de simulation :

```
Extended Controls (⋯) → Virtual sensors
```

| Capteur | Simulable | Remarque |
|---------|-----------|----------|
| Accéléromètre | ✅ | Inclinaison via sliders |
| Magnétomètre | ✅ | Nécessaire pour la boussole |
| Gyroscope | ✅ | Rotation angulaire |
| Température | ⚠️ | Souvent absent → mock interne |
| Humidité | ⚠️ | Souvent absent → mock interne |
| Proximité | ✅ | Bascule manuelle |
| Compteur de pas | ❌ | Préférer un appareil physique |

Quand un capteur est absent, **AppSENSOR** active un **flux synthétique** (onde sinusoïdale ou signal alterné) pour permettre la démonstration pédagogique.

---

## Composants techniques détaillés

### `LineChartView`

Vue `Canvas` personnalisée : historique de **80 échantillons** (`ArrayDeque`), rendu découpé en sous-méthodes, dégradé violet sous la courbe et point ambre sur la dernière valeur.

### `SensorGraphFragment`

Fragment paramétrable via `newInstance(kind, title, mode)` :
- `FIRST_VALUE` — première valeur du tableau
- `MAGNITUDE` — norme 3D via `SignalMath.vectorLength()`
- Capteur absent → simulation via `SyntheticFeedGenerator`

### Couche utilitaire (`utils/`)

| Classe | Responsabilité |
|--------|----------------|
| `SignalMath` | Norme euclidienne (`Math.hypot`), lissage exponentiel, écart-type Welford |
| `MotionHeuristics` | Évaluation de posture via enum `PostureKind` et tampon de 30 valeurs |
| `SyntheticFeedGenerator` | Génération de données fictives (température, humidité, proximité, champ magnétique) |
| `BearingMapper` | Normalisation de l'azimut (0°–360°) et libellé cardinal par secteur de 45° |
| `SensorFormatter` | Fiche technique textuelle pour chaque capteur listé |
| `UiPalette` | Constantes de couleurs partagées entre Java et le thème |

### `ActivityRecognitionFragment`

Le traitement du signal est délégué aux utilitaires `SignalMath` et `MotionHeuristics` :

1. **Lissage exponentiel** (`SignalMath.smoothInPlace`, gain 0.2) pour estimer la gravité
2. Calcul de l'énergie résiduelle via `Math.hypot`
3. Tampon circulaire de **30 échantillons** (`energyRing`)
4. Écart-type calculé en **une passe** (algorithme de Welford)
5. Décision via l'énumération `PostureKind` :

| Condition | Mouvement détecté |
|-----------|-------------------|
| Pic d'impulsion élevé | Saut (Jumping) |
| Dispersion élevée | Marche (Walking) |
| Axe Z dominant | Téléphone à plat |
| Axe X ou Y dominant | Assis ou debout |
| Sinon | Posture idle |

### `CompassFragment`

Flux en deux étapes : `cacheSample()` puis `renderHeading()`.  
Combine accéléromètre et magnétomètre via `SensorManager.getRotationMatrix()` / `getOrientation()`, puis délègue l'affichage cardinal à `BearingMapper`.

---

## Permissions Android

```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
```

| Permission | API | Fragment concerné |
|------------|-----|-------------------|
| `ACTIVITY_RECOGNITION` | 29+ (Android 10) | `StepCounterFragment` |

La demande est effectuée dynamiquement via `ActivityResultLauncher` au premier accès à l'écran Step Tracker.

---

## Dépannage courant

| Problème | Solution |
|----------|----------|
| Gradle sync échoue | Vérifier Internet, SDK 34, et **JDK 17+** (`File → Settings → Build Tools → Gradle`) |
| Courbe vide | Attendre 2 secondes ou secouer l'appareil (accéléromètre) |
| Compteur de pas à 0 | Tester sur appareil physique, accorder la permission |
| Boussole figée | Vérifier que magnétomètre ET accéléromètre sont actifs |
| Thème incorrect | Nettoyer le build : **Build → Clean Project → Rebuild** |

---
## Realise par:
NAFTAOUI NIAMA


