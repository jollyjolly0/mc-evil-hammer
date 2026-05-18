package com.example.necromancer.network;

import com.example.necromancer.server.NecromancerOrderService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

public final class NecromancerNetworking {
	private NecromancerNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(NecromancerOrderPayload.TYPE, NecromancerOrderPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(NecromancerOrderPayload.TYPE, (payload, context) ->
			payload.resolveOrder().result().ifPresentOrElse(
				order -> NecromancerOrderService.issueOrder(context.player(), order),
				() -> context.player().sendSystemMessage(Component.literal("Received invalid necromancer order packet: " + payload.orderId()))
			)
		);
	}
}
