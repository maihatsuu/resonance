package maihatsuu.resonance.client;

import maihatsuu.resonance.Resonance;
import maihatsuu.resonance.ResonanceConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class ResonanceConfigScreen {
	private static final ResonanceConfig DEFAULTS = new ResonanceConfig();

	private ResonanceConfigScreen() {}

	static Screen create(Screen parent) {
		ResonanceConfig config = Resonance.config();
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("config.resonance.title"))
				.setSavingRunnable(config::save);
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.resonance.general"));

		general.addEntry(entries.startBooleanToggle(Component.translatable("config.resonance.consumeItem"), config.consumeItem)
				.setDefaultValue(DEFAULTS.consumeItem)
				.setSaveConsumer(value -> config.consumeItem = value)
				.build());
		general.addEntry(entries.startIntField(Component.translatable("config.resonance.cooldownTicks"), config.cooldownTicks)
				.setDefaultValue(DEFAULTS.cooldownTicks)
				.setMin(0)
				.setTooltip(Component.translatable("config.resonance.cooldownTicks.tooltip"))
				.setSaveConsumer(value -> config.cooldownTicks = value)
				.build());
		general.addEntry(entries.startIntSlider(Component.translatable("config.resonance.radius"), config.radius, 1, ResonanceConfig.MAX_RADIUS)
				.setDefaultValue(DEFAULTS.radius)
				.setTooltip(Component.translatable("config.resonance.radius.tooltip"))
				.setSaveConsumer(value -> config.radius = value)
				.build());

		return builder.build();
	}
}
