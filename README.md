# Resonance

A Fabric mod for Minecraft 1.21.1. Use an amethyst shard to send out a resonant pulse that briefly shows where hostile mobs can spawn around you.

## Using it

Right-click with an amethyst shard. It plays an amethyst sound and sends a wave outward from you, wrapping over the terrain. As the wave passes, tiles light up on the floors where mobs can spawn:

- **Red:** a mob could spawn here right now.
- **Yellow:** only sky light is keeping this spot safe right now, so mobs can spawn here at night or in storms.

Colours reflect the moment of the pulse, so open ground that is yellow by day is red at night. Only the player who used the pulse sees it.

## Installation

Requires [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api). Install Resonance on both the client and the server.

To edit settings in game, also install [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config). The menu covers the consume, cooldown and distance settings.

## Configuration

Settings are stored in `config/resonance.json`. On a multiplayer server, the server's file is the one that applies.

| Option | Default | Description |
|---|---|---|
| `radius` | 32 | Pulse distance in blocks (1–64). |
| `durationTicks` | 100 | How long tiles stay visible. |
| `cooldownTicks` | 40 | Item cooldown after a pulse. |
| `consumeItem` | true | Use up the shard. |
| `maxSpots` | 16384 | Cap on marked tiles, nearest first (limit 65536). |
| `showNightSpots` | true | Show yellow tiles. |
| `vibration` | true | The pulse counts as a vibration for sculk sensors. |
| `revealMobs` | false | Nearby hostile mobs glow for 3 seconds. |

Items that trigger a pulse come from the item tag `resonance:pulse_items` (by default `minecraft:amethyst_shard`), so a datapack can change them.

## Shader packs

With an Iris shader pack active, the tiles still appear but the wave does not. Tested with Complementary Reimagined, BSL and MakeUp UltraFast.

## Development

Run these from `resonance-1.21.1/`:

```sh
./gradlew build       # build the jars into build/libs/
./gradlew runClient   # launch a dev client
./gradlew runServer   # launch a dev dedicated server
./gradlew prodClient  # launch the built jar like a real install, with Sodium and Iris
```

Iris crashes under `runClient`, so use `prodClient` to test shader packs, placing them in `run/shaderpacks/`.

### Manual test checklist

- Dark cave: red tiles on spawnable floors, none on slabs, glass or lit areas.
- Surface in daytime: open grass shows yellow, torch-lit areas show nothing.
- Cooldown overlay appears and the shard is consumed.
- Other players don't see the pulse.
- A sculk sensor triggers when `vibration` is on.

## License

[CC0-1.0](resonance-1.21.1/LICENSE)
