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
	private static final int MAX_RADIUS = 64;

	public int radius = 16;
	public int durationTicks = 100;
	public int cooldownTicks = 200;
	public boolean consumeItem = true;
	public int maxSpots = 512;
	public boolean showNightSpots = true;
	public boolean vibration = true;
	public boolean revealMobs = false;

	public static ResonanceConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("resonance.json");
		ResonanceConfig config = Files.exists(path) ? read(path) : writeDefaults(path);
		config.clamp();
		return config;
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

	private static ResonanceConfig writeDefaults(Path path) {
		ResonanceConfig config = new ResonanceConfig();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			Resonance.LOGGER.warn("Could not write {}", path, e);
		}
		return config;
	}

	private void clamp() {
		radius = Math.clamp(radius, 1, MAX_RADIUS);
		durationTicks = Math.max(durationTicks, 0);
		cooldownTicks = Math.max(cooldownTicks, 0);
		maxSpots = Math.max(maxSpots, 0);
	}
}
