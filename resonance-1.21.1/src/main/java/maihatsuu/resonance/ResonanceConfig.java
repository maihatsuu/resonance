package maihatsuu.resonance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResonanceConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final int MAX_RADIUS = 64;
	/** At 9 bytes per spot this keeps a pulse packet well under the 1 MiB payload limit. */
	private static final int MAX_SPOTS = 65536;

	public int radius = 32;
	public int durationTicks = 100;
	public int cooldownTicks = 40;
	public boolean consumeItem = true;
	public int maxSpots = 16384;
	public boolean showNightSpots = true;
	public boolean vibration = true;
	public boolean revealMobs = false;

	public static ResonanceConfig load() {
		Path path = path();
		ResonanceConfig config;
		if (Files.exists(path)) {
			config = read(path);
		} else {
			config = new ResonanceConfig();
			config.save();
		}
		config.clamp();
		return config;
	}

	public void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			Resonance.LOGGER.warn("Could not write {}", path, e);
		}
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("resonance.json");
	}

	/** A file that can't be parsed is left alone so the user's edits aren't lost. */
	private static ResonanceConfig read(Path path) {
		try (Reader reader = Files.newBufferedReader(path)) {
			ResonanceConfig config = GSON.fromJson(reader, ResonanceConfig.class);
			if (config != null) {
				return config;
			}
			Resonance.LOGGER.warn("{} is empty, using defaults", path);
		} catch (IOException | JsonParseException e) {
			Resonance.LOGGER.warn("Could not read {}, using defaults", path, e);
		}
		return new ResonanceConfig();
	}

	private void clamp() {
		radius = Math.clamp(radius, 1, MAX_RADIUS);
		durationTicks = Math.max(durationTicks, 0);
		cooldownTicks = Math.max(cooldownTicks, 0);
		maxSpots = Math.clamp(maxSpots, 0, MAX_SPOTS);
	}
}
