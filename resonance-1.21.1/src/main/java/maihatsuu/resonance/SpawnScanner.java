package maihatsuu.resonance;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SpawnScanner {
	public enum Kind { RED, YELLOW }

	public record Spot(BlockPos pos, Kind kind) {}

	private SpawnScanner() {}

	/** Returns the spawnable feet positions around {@code origin}, nearest first. */
	public static List<Spot> scan(ServerLevel level, BlockPos origin, ResonanceConfig config) {
		int radius = config.radius;
		int radiusSqr = radius * radius;
		List<Spot> spots = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dy * dy + dz * dz > radiusSqr) {
						continue;
					}
					cursor.setWithOffset(origin, dx, dy, dz);
					if (level.isOutsideBuildHeight(cursor) || !level.hasChunkAt(cursor)
							|| !SpawnPlacements.isSpawnPositionOk(EntityType.ZOMBIE, level, cursor)) {
						continue;
					}
					Kind kind = classify(level, cursor, config.showNightSpots);
					if (kind != null) {
						spots.add(new Spot(cursor.immutable(), kind));
					}
				}
			}
		}

		spots.sort(Comparator.comparingDouble(spot -> spot.pos().distSqr(origin)));
		return spots.size() > config.maxSpots ? List.copyOf(spots.subList(0, config.maxSpots)) : spots;
	}

	/**
	 * Mirrors {@code Monster.isDarkEnoughToSpawn} with its random rolls at their most permissive,
	 * so a spot's colour doesn't change from one pulse to the next.
	 */
	private static Kind classify(ServerLevel level, BlockPos pos, boolean showNightSpots) {
		DimensionType dimension = level.dimensionType();
		if (level.getBrightness(LightLayer.BLOCK, pos) > dimension.monsterSpawnBlockLightLimit()) {
			return null;
		}
		int brightness = level.isThundering()
				? level.getMaxLocalRawBrightness(pos, 10)
				: level.getMaxLocalRawBrightness(pos);
		if (brightness <= dimension.monsterSpawnLightTest().getMaxValue()) {
			return Kind.RED;
		}
		return showNightSpots && dimension.hasSkyLight() ? Kind.YELLOW : null;
	}
}
