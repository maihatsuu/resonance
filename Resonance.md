# Resonance (working name)

A Fabric mod for Minecraft 1.21.1. Use an amethyst shard to send out a resonant pulse that
briefly shows where hostile mobs can spawn around you. Server-side only, so vanilla clients work.

## Using it
- Right-click with an amethyst shard: uses up one shard, plays the amethyst chime, and sends a
  pulse outward from you.
- As the pulse spreads (about 1 s to reach the edge), spawnable spots light up with particles,
  nearest first, and stay visible for about 5 s.
- Short cooldown (vanilla item cooldown overlay on the shard) so it can't be spammed.
- Only the player who used it sees the particles.

## What counts as spawnable
Use vanilla's own checks with a zombie as the reference mob, so slabs, glass, carpets, etc.
behave exactly like real spawning:
- `SpawnPlacements.isSpawnPositionOk` (solid top surface, room for the mob, no liquid).
- `Monster.isDarkEnoughToSpawn` for light.
- Two colours:
  - **Red:** a mob could spawn here right now.
  - **Yellow:** block light is 0, but sky light is keeping it safe right now, so mobs will spawn here at night or in storms.
- Only check surface blocks (solid block with air above) inside the radius, and cap the number of
  marked spots (nearest first) to keep particle counts sane.

## Showing it (server-side)
- `ServerLevel.sendParticles(player, ...)` with coloured dust particles just above each spot.
  Re-sent every ~10 ticks for the duration, so vanilla clients render it with no client mod.
- Pulse ring: a short burst of end rod / amethyst-coloured particles expanding at the pulse front.

## Optional extras (config, off or on by default TBD)
- **Vibration:** the pulse emits a game event so sculk sensors pick it up and shriekers might
  react, meaning using it near an ancient city is risky.
- **Reveal mobs:** hostile mobs inside the radius glow for ~3 s. Vanilla Glowing is visible to
  everyone; acceptable, or skip.

## Config (`config/resonance.json`)
| Option | Default | Description |
|---|---|---|
| `radius` | 16 | Pulse radius in blocks. |
| `durationTicks` | 100 | How long spots stay visible. |
| `cooldownTicks` | 200 | Item cooldown after a pulse. |
| `consumeItem` | true | Use up the shard. |
| `maxSpots` | 512 | Cap on marked spots. |
| `showNightSpots` | true | Show the yellow "spawnable at night" spots. |
| `vibration` | true | Pulse counts as a vibration. |
| `revealMobs` | false | Nearby hostile mobs glow briefly. |

Items that trigger a pulse come from an item tag, `resonance:pulse_items` (default
`minecraft:amethyst_shard`), so a datapack can change them.

## Open questions
- Name (Resonance? Chime? Echo?).
- Should the shard be consumed, or take durability-like uses from something crafted (e.g. a
  "tuning fork" made from shards)?
- Particle look: how readable dust particles are in caves vs. bright surface; may need tuning.
- Radius vs. performance: 16 is roughly 33×33×33 blocks to scan, once per use, which is fine. Larger needs care.

## Testing
- Dark cave: red spots appear on spawnable floors, none on slabs, glass, or lit areas.
- Surface in daytime: open grass shows yellow (sky light), torch-lit areas show nothing.
- Cooldown overlay appears, shard is consumed, other players don't see the particles.
- Sculk sensor triggers when `vibration` is on.
