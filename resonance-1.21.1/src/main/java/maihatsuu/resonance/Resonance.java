package maihatsuu.resonance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resonance implements ModInitializer {
	public static final String MOD_ID = "resonance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final TagKey<Item> PULSE_ITEMS = TagKey.create(Registries.ITEM, id("pulse_items"));

	private static final int REVEAL_TICKS = 60;

	private static ResonanceConfig config;

	@Override
	public void onInitialize() {
		config = ResonanceConfig.load();
		PayloadTypeRegistry.playS2C().register(PulsePayload.TYPE, PulsePayload.CODEC);
		UseItemCallback.EVENT.register(Resonance::onUseItem);
	}

	private static InteractionResultHolder<ItemStack> onUseItem(Player user, Level world, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		// Fabric fires this before vanilla's spectator and cooldown checks.
		if (!(user instanceof ServerPlayer player) || !(world instanceof ServerLevel level)
				|| player.isSpectator() || !stack.is(PULSE_ITEMS)
				|| player.getCooldowns().isOnCooldown(stack.getItem())
				// Without the mod a player would see nothing, so don't spend their shard.
				|| !ServerPlayNetworking.canSend(player, PulsePayload.TYPE)) {
			return InteractionResultHolder.pass(stack);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.35F, 1.0F);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
		if (config.cooldownTicks > 0) {
			player.getCooldowns().addCooldown(stack.getItem(), config.cooldownTicks);
		}
		if (config.consumeItem) {
			stack.consume(1, player);
		}
		if (config.vibration) {
			level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
		}
		if (config.revealMobs) {
			revealMobs(player, level);
		}

		BlockPos origin = player.blockPosition();
		ServerPlayNetworking.send(player, new PulsePayload(origin, config.radius, config.durationTicks,
				SpawnScanner.scan(level, origin, config)));
		return InteractionResultHolder.success(stack);
	}

	private static void revealMobs(ServerPlayer player, ServerLevel level) {
		double radius = config.radius;
		level.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(radius),
				entity -> entity instanceof Enemy && entity.distanceToSqr(player) <= radius * radius)
				.forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, REVEAL_TICKS, 0, false, false)));
	}

	public static ResonanceConfig config() {
		return config;
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
