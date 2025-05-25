# Contributing

## Setting up the development environment

### Necessary Gradle files

To be able to import the Gradle project into Android Studio, you will need to create a copy of the `gradle/debug.signing` file to `gradle/release.signing`. 

The `release.signing` file is not included in the repository for security reasons, as it contains the production signing key. 
You can create the `release.signing` file by replacing the values with your own production keystore, which can be created as `gradle/my-release-key.keystore`.

### Google Services JSON files

It's recommended to work on the `floss` variant of the project, which does not require any Google Services JSON files.

If you are working on the `play` variant, you will need to 

1. Create a new Firebase project
2. Add new Android apps to the same project for both the `release` and `debug` variants
3. Download the `google-services.json` file