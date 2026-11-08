package maihatsuu.resonance.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ResonanceModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// Cloth Config is optional; without it Mod Menu just shows no config button.
		return FabricLoader.getInstance().isModLoaded("cloth-config")
				? ResonanceConfigScreen::create
				: parent -> null;
	}
}
