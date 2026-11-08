# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Resonance is a Fabric mod for Minecraft 1.21.1. Using an amethyst shard sends a pulse that briefly shows, with particles, where hostile mobs can spawn nearby.

The Gradle project is in `resonance-1.21.1/`, and **all Gradle commands run from there**. It is currently the unmodified Fabric template: `ExampleMixin`, `ExampleClientMixin`, and the placeholder metadata in `fabric.mod.json` are all template leftovers, and none of the feature exists yet.

`Resonance.md` (repo root) is the design spec. It defines behaviour, config options and defaults, and manual test scenarios, and lists open questions such as the mod's name and whether the shard is consumed. Read it before implementing a feature, and don't treat its open questions as settled.

## Commands

Run from `resonance-1.21.1/`:

```sh
./gradlew build       # compile and produce the jars in build/libs/
./gradlew runClient   # launch a dev client
./gradlew runServer   # launch a dev dedicated server
```

There is no linter and no test source set, so there is no way to run a single test. Verification is manual, using the scenarios in the Testing section of `Resonance.md`.

## Architecture constraints

- **Server-side only.** The mod must work with vanilla clients, so the feature belongs in `src/main`. Avoid anything that requires the mod on the client, such as custom registered items, blocks, particles, or custom packets. Visuals go through `ServerLevel.sendParticles(player, ...)` so that only the triggering player sees them.
- **Split source sets.** Loom's `splitEnvironmentSourceSets()` separates `src/main` from `src/client`. Code in `src/main` cannot reference client classes.
- **Mojang mappings.** The project uses `loom.officialMojangMappings()`, so names are `ServerLevel`, `ResourceLocation`, and so on, not Yarn names. Fabric documentation examples often use Yarn.
- **Use vanilla's spawn checks.** Spawnability comes from `SpawnPlacements.isSpawnPositionOk` and `Monster.isDarkEnoughToSpawn` with a zombie as the reference mob, rather than reimplementing the rules.
- **Mixins must be registered.** A new mixin has to be listed in `resonance.mixins.json` (or `resonance.client.mixins.json` for client mixins). Both set `defaultRequire: 1`, so an injection that fails to match crashes at startup.
- **Versions** for Minecraft, Loader, Loom, and Fabric API are set in `gradle.properties`. The mod targets Java 21, while CI builds with JDK 25.
