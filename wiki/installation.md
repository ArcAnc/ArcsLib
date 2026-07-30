PulseLib 1.1.2 targets Minecraft 26.2 with NeoForge 26.2.0.40-beta and requires Java 25. Add the dependency to a NeoForge 26.2 project, then refresh Gradle.

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
    // Current example: minecraft: 26.2, pulselib: 1.1.2
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
  // Current example: minecraft: 26.2, pulselib: 1.1.2
  // Visit https://modrinth.com/mod/pulselib/versions to get the latest version
  implementation "maven.modrinth:pulselib:${minecraft_version}-${pulselib_version}"
}
```

After adding the repository and dependency, refresh Gradle. Keep the Minecraft and PulseLib version segments aligned with a published PulseLib release.
