package maihatsuu.resonance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import maihatsuu.resonance.PulsePayload;
import maihatsuu.resonance.SpawnScanner.Kind;
import maihatsuu.resonance.SpawnScanner.Spot;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/** Draws the local player's active pulse. All state is touched only on the render thread. */
public final class PulseRenderer {
	private static final float SPREAD_TICKS = 20.0F;
	private static final float SPOT_FADE_IN_TICKS = 4.0F;
	private static final float SPOT_FADE_OUT_TICKS = 20.0F;
	private static final float SPOT_ALPHA = 0.45F;
	private static final float SPOT_INSET = 0.05F;
	private static final float SPOT_LIFT = 0.02F;

	private static ShaderInstance waveShader;
	private static Pulse active;

	private PulseRenderer() {}

	static void setWaveShader(ShaderInstance shader) {
		waveShader = shader;
	}

	static void start(PulsePayload payload, ClientLevel level) {
		active = new Pulse(payload, level);
	}

	static void clear() {
		active = null;
	}

	static void tick(Minecraft client) {
		if (active == null || client.isPaused()) {
			return;
		}
		active.age++;
		if (active.level != client.level || active.age > SPREAD_TICKS + active.payload.durationTicks()) {
			active = null;
		}
	}

	static void render(WorldRenderContext context) {
		if (active == null || active.level != Minecraft.getInstance().level) {
			return;
		}
		float age = active.age + context.tickCounter().getGameTimeDeltaPartialTick(false);
		Vec3 camera = context.camera().getPosition();

		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		drawSpots(context, camera, age);
		if (age < SPREAD_TICKS && waveShader != null) {
			drawWave(context, camera, age);
		}
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
	}

	private static void drawSpots(WorldRenderContext context, Vec3 camera, float age) {
		BlockPos origin = active.payload.origin();
		int radius = active.payload.radius();
		int duration = active.payload.durationTicks();
		Matrix4f view = context.positionMatrix();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for (Spot spot : active.payload.spots()) {
			BlockPos pos = spot.pos();
			float sinceReveal = age - SPREAD_TICKS * (float) Math.sqrt(pos.distSqr(origin)) / radius;
			if (sinceReveal < 0.0F || sinceReveal > duration) {
				continue;
			}
			float alpha = SPOT_ALPHA * Math.min(1.0F, Math.min(sinceReveal / SPOT_FADE_IN_TICKS, (duration - sinceReveal) / SPOT_FADE_OUT_TICKS));
			float green = spot.kind() == Kind.RED ? 0.1F : 0.85F;
			float minX = (float) (pos.getX() - camera.x) + SPOT_INSET;
			float maxX = (float) (pos.getX() - camera.x) + 1.0F - SPOT_INSET;
			float minZ = (float) (pos.getZ() - camera.z) + SPOT_INSET;
			float maxZ = (float) (pos.getZ() - camera.z) + 1.0F - SPOT_INSET;
			float y = (float) (pos.getY() - camera.y) + SPOT_LIFT;
			buffer.addVertex(view, minX, y, minZ).setColor(1.0F, green, 0.1F, alpha);
			buffer.addVertex(view, minX, y, maxZ).setColor(1.0F, green, 0.1F, alpha);
			buffer.addVertex(view, maxX, y, maxZ).setColor(1.0F, green, 0.1F, alpha);
			buffer.addVertex(view, maxX, y, minZ).setColor(1.0F, green, 0.1F, alpha);
		}

		MeshData mesh = buffer.build();
		if (mesh == null) {
			return;
		}
		// Vertices are already in view space, so the shader's model-view must be identity.
		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix().identity();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableCull();
		BufferUploader.drawWithShader(mesh);
		RenderSystem.enableCull();
		modelView.popMatrix();
		RenderSystem.applyModelViewMatrix();
	}

	private static void drawWave(WorldRenderContext context, Vec3 camera, float age) {
		BlockPos origin = active.payload.origin();
		float progress = age / SPREAD_TICKS;
		Matrix4f inverseViewProjection = new Matrix4f(context.projectionMatrix()).mul(context.positionMatrix()).invert();

		RenderSystem.setShader(() -> waveShader);
		waveShader.setSampler("DepthSampler", Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());
		waveShader.safeGetUniform("InvViewProj").set(inverseViewProjection);
		waveShader.safeGetUniform("Center").set(
				(float) (origin.getX() + 0.5 - camera.x),
				(float) (origin.getY() + 0.5 - camera.y),
				(float) (origin.getZ() + 0.5 - camera.z));
		waveShader.safeGetUniform("Front").set(active.payload.radius() * progress);
		waveShader.safeGetUniform("Fade").set(1.0F - progress * progress);

		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		buffer.addVertex(-1.0F, -1.0F, 0.0F);
		buffer.addVertex(1.0F, -1.0F, 0.0F);
		buffer.addVertex(1.0F, 1.0F, 0.0F);
		buffer.addVertex(-1.0F, 1.0F, 0.0F);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.enableCull();
	}

	private static final class Pulse {
		private final PulsePayload payload;
		private final ClientLevel level;
		private int age;

		private Pulse(PulsePayload payload, ClientLevel level) {
			this.payload = payload;
			this.level = level;
		}
	}
}
