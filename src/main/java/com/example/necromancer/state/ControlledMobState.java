package com.example.necromancer.state;

import com.example.necromancer.NecromancerOrder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

public record ControlledMobState(UUID controllerUuid, NecromancerOrder order, int orderExpiryTick, Optional<BlockPos> orderTargetPos) {
	private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

	public static final Codec<ControlledMobState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		UUID_CODEC.fieldOf("controller_uuid").forGetter(ControlledMobState::controllerUuid),
		NecromancerOrder.CODEC.fieldOf("order").forGetter(ControlledMobState::order),
		Codec.INT.optionalFieldOf("order_expiry_tick", 0).forGetter(ControlledMobState::orderExpiryTick),
		BlockPos.CODEC.optionalFieldOf("order_target_pos").forGetter(ControlledMobState::orderTargetPos)
	).apply(instance, ControlledMobState::new));
}
