# Contributing

## Setting up the development environment

### Necessary Gradle files

The `release.signing` file is not included in the repository for security reasons, as it contains the production signing key.

It is optional to have a release signing key, and if one is not provided, signing will be skipped during the build process.

### Google Services JSON files

It's recommended to work on the `floss` variant of the project, which does not require any Google Services JSON files.

If you are working on the `play` variant, you will need to 

1. Create a new Firebase project
2. Add new Android apps to the same project for both the `release` and `debug` variants
3. Download the `google-services.json` file