package maihatsuu.resonance.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import maihatsuu.resonance.PulsePayload;
import maihatsuu.resonance.Resonance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ResonanceClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CoreShaderRegistrationCallback.EVENT.register(context ->
				context.register(Resonance.id("pulse"), DefaultVertexFormat.POSITION, PulseRenderer::setWaveShader));
		ClientPlayNetworking.registerGlobalReceiver(PulsePayload.TYPE,
				(payload, context) -> PulseRenderer.start(payload, context.client().level));
		ClientTickEvents.END_CLIENT_TICK.register(PulseRenderer::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PulseRenderer.clear());
		WorldRenderEvents.LAST.register(PulseRenderer::render);
	}
}
