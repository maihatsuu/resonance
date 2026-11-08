# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Resonance is a Fabric mod for Minecraft 1.21.1. Using an amethyst shard sends a pulse that briefly shows where hostile mobs can spawn nearby. It must be installed on both client and server.

The Gradle project is in `resonance-1.21.1/`, and **all Gradle commands run from there**. In `src/main`, `Resonance` handles item use, `SpawnScanner` finds and colours spots, and `ResonanceConfig` loads `config/resonance.json`. The result goes to the using player in a `PulsePayload`; `PulseRenderer` in `src/client` draws it. There are no mixins.

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

- **Server decides, client draws.** Scanning and item logic run on the server; visuals are client-only. The wave is a core shader (`assets/resonance/shaders/core/pulse.*`) that rebuilds world positions from the depth buffer, and spots are translucent quads. Shader errors only appear at runtime, so check the client log.
- **Split source sets.** Loom's `splitEnvironmentSourceSets()` separates `src/main` from `src/client`. Code in `src/main` cannot reference client classes.
- **Mojang mappings.** The project uses `loom.officialMojangMappings()`, so names are `ServerLevel`, `ResourceLocation`, and so on, not Yarn names. Fabric documentation examples often use Yarn.
- **Spawn checks follow vanilla.** Placement uses `SpawnPlacements.isSpawnPositionOk` with a zombie as the reference mob. Light deliberately doesn't call `Monster.isDarkEnoughToSpawn`, because it rolls random numbers and spots would flicker between pulses; `SpawnScanner.classify` mirrors it with the most permissive rolls.
- **Versions** for Minecraft, Loader, Loom, and Fabric API are set in `gradle.properties`. The mod targets Java 21, while CI builds with JDK 25.
