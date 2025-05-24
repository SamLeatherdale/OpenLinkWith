# Contributing

## Setting up the development environment

### Necessary Gradle files

To be able to import the Gradle project into Android Studio, you will need to create a copy of the `gradle/debug.signing` file to `gradle/release.signing`. 

The `release.signing` file is not included in the repository for security reasons, as it contains the production signing key. 
You can create the `release.signing` file by replacing the values with your own production keystore, which can be created as `gradle/my-release-key.keystore`.