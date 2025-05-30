[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/F-P0wqoO)

# LocalExplorer

Application Android permettant de découvrir les restaurants à proximité.

## Configuration des clés API

Pour des raisons de sécurité, les clés API ne sont pas incluses dans le dépôt GitHub. Vous devez configurer vos propres clés API pour que l'application fonctionne correctement.

### Étape 1 : Créer le fichier de clés API

1. Copiez le fichier modèle `app/src/main/assets/api_keys.properties.template` vers `app/src/main/assets/api_keys.properties`
2. Remplacez les valeurs par vos propres clés API :
    ```
    GEOAPIFY_API_KEY=votre_clé_geoapify
    GOOGLE_MAPS_API_KEY=votre_clé_google_maps
    ```

### Étape 2 : Configurer le fichier strings.xml

1. Copiez le fichier modèle `app/src/main/res/values/strings.xml.template` vers `app/src/main/res/values/strings.xml`
2. Remplacez la valeur de la clé Google Maps :
    ```xml
    <string name="google_maps_api_key" translatable="false">votre_clé_google_maps</string>
    ```

## Obtenir des clés API

-   Pour obtenir une clé API Geoapify, inscrivez-vous sur [Geoapify](https://www.geoapify.com/)
-   Pour obtenir une clé API Google Maps, inscrivez-vous sur [Google Cloud Platform](https://console.cloud.google.com/) et activez l'API Maps SDK pour Android

## Fonctionnalités

-   Affichage des restaurants à proximité sur une carte et une liste
-   Filtrage des restaurants par distance et note
-   Sauvegarde des restaurants favoris
-   Détails des restaurants (adresse, numéro de téléphone, site web, etc.)
-   Navigation vers les restaurants

## Technologies utilisées

-   Android Java
-   Google Maps API
-   Geoapify API
-   Retrofit pour les appels réseau
-   Room pour la persistance des données
