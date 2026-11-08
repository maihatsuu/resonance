package maihatsuu.resonance;

import io.netty.buffer.ByteBuf;
import maihatsuu.resonance.SpawnScanner.Kind;
import maihatsuu.resonance.SpawnScanner.Spot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Sent to the player who used a pulse item; the client draws everything from this. */
public record PulsePayload(BlockPos origin, int radius, int durationTicks, List<Spot> spots) implements CustomPacketPayload {
	public static final Type<PulsePayload> TYPE = new Type<>(Resonance.id("pulse"));

	private static final StreamCodec<ByteBuf, Kind> KIND_CODEC =
			ByteBufCodecs.BOOL.map(red -> red ? Kind.RED : Kind.YELLOW, kind -> kind == Kind.RED);
	private static final StreamCodec<ByteBuf, Spot> SPOT_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, Spot::pos,
			KIND_CODEC, Spot::kind,
			Spot::new);

	public static final StreamCodec<ByteBuf, PulsePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, PulsePayload::origin,
			ByteBufCodecs.VAR_INT, PulsePayload::radius,
			ByteBufCodecs.VAR_INT, PulsePayload::durationTicks,
			SPOT_CODEC.apply(ByteBufCodecs.list()), PulsePayload::spots,
			PulsePayload::new);

	@Override
	public Type<PulsePayload> type() {
		return TYPE;
	}
}
