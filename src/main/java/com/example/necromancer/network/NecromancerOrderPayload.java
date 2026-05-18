package com.example.necromancer.network;

import com.example.ExampleMod;
import com.example.necromancer.NecromancerOrder;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NecromancerOrderPayload(String orderId) implements CustomPacketPayload {
	public static final Type<NecromancerOrderPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "necromancer_order"));
	public static final StreamCodec<RegistryFriendlyByteBuf, NecromancerOrderPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.mapStream(buffer -> buffer),
		NecromancerOrderPayload::orderId,
		NecromancerOrderPayload::new
	);

	public NecromancerOrderPayload(NecromancerOrder order) {
		this(order.id());
	}

	public DataResult<NecromancerOrder> resolveOrder() {
		return NecromancerOrder.parse(this.orderId);
	}

	@Override
	public Type<NecromancerOrderPayload> type() {
		return TYPE;
	}
}
