PulseLib is lightweight and easy to install — requiring only a few changes to your build.gradle file.

Currently, the project does not have its own hosting, so you can use CurseMaven or Modrinth as your dependency source, but I highly recommend using Modrinth.

## Using CurseForge (CurseMaven)

Add the repository and dependency:

```gradle
repositories {
    maven {
        name = 'CurseMaven'
        url "https://beta.cursemaven.com"
    }
}

dependencies {
    // Example: minecraft: 1.21.1, pulselib: 1.0.7
    // Visit https://www.curseforge.com/minecraft/mc-mods/pulselib/files/all to get the latest version
    implementation "curse.maven:pulselib-${minecraft_version}-${pulselib_version}"
}
```
## Using Modrinth

Add the repository and dependency:

```gradle
repositories {
   maven { 
       url = "https://api.modrinth.com/maven" 
   }
}


dependencies {
  // Example: minecraft: 1.21.1, pulselib: 1.0.7
  // Visit https://modrinth.com/mod/pulselib/versions to get the latest version
  implementation "maven.modrinth:pulselib:${minecraft_version}-${pulselib_version}"
}
```

Final Step

After adding the repository and dependency, refresh Gradle, and PulseLib will be ready to use in your project!