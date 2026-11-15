# AMTS Dépenses — prototype Android V0.1

**Application Android native, locale et hors ligne** pour Fouad (AMTS SARL).

## Fonctionnel dans le code source
- Accueil avec nombre et total des dépenses (ce total **n'est pas** un solde de caisse).
- Formulaire de saisie : montant DH, motif, chantier, financement caisse/personnel, état du justificatif.
- Validation des montants en centimes entiers, sans flottants en base.
- Persistance locale SQLite via Room, historique des dépenses.
- Identifiants UUID et statut LOCAL pour une synchronisation future.
- Aucun accès Internet ni compte Google requis.

## Non implémenté dans V0.1
- Installation APK déjà compilé, comptes multiutilisateurs, authentification/biométrie.
- Comptabilité des caisses, transferts, avances, remboursements, validations.
- Capture photo/PDF, OCR, partage WhatsApp, Google Drive.
- Saisie vocale intelligente, sauvegarde, chiffrement spécifique de la base, synchronisation.

**Important :** ne pas utiliser V0.1 comme registre comptable officiel ou pour stocker des données sensibles. Le prototype n'a pas encore de mécanisme de sauvegarde et l'effacement des données de l'application supprimera les opérations locales.

## Ouvrir et compiler
1. Installer Android Studio récent, Android SDK 35 et JDK 17.
2. Ouvrir le dossier `AMTS_Depenses_Android` comme projet Gradle.
3. Laisser Android Studio télécharger Gradle et les dépendances (Internet requis **pour la compilation**, pas pour utiliser l'application).
4. Exécuter sur un téléphone Android connecté en USB (débogage USB activé) ou un émulateur.
5. Pour obtenir un APK : Android Studio > Build > Build APK(s).

Le projet est fourni sans wrapper Gradle (`gradlew`) : Android Studio peut utiliser une distribution Gradle installée/configurée. Utiliser Gradle 8.9 avec AGP 8.7.3. Si nécessaire, générer le wrapper avec `gradle wrapper --gradle-version 8.9` dans un environnement ayant Gradle.

## Générer l'APK avec GitHub Actions
Le workflow `.github/workflows/build-apk.yml` compile l'APK de débogage à chaque push et peut aussi être lancé manuellement depuis l'onglet **Actions** du dépôt GitHub. Une fois le workflow terminé, téléchargez `amts-depenses-debug-apk` dans la section **Artifacts** de l'exécution.

Cet APK de débogage sert aux tests et à l'installation manuelle. Pour une publication sur Google Play, il faudra générer un APK ou un AAB de release signé avec une clé de signature dédiée.

## Architecture
- `app/src/main/java/ma/amts/depenses/data/Expense.kt` : entité, DAO, base Room.
- `.../ui/ExpenseViewModel.kt` : logique de saisie et flux d'historique.
- `.../MainActivity.kt` : interface Jetpack Compose.

## Prochaine itération V0.2
- Schéma de caisse et journal de mouvements en partie double, transfert/confirmation.
- Capture de photos dans le stockage privé Android et liens aux dépenses.
- Écran de correction avec audit et préparation du journal de synchronisation.
