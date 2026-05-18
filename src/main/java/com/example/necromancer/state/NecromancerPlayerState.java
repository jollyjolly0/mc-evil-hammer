package com.example.necromancer.state;

import com.example.necromancer.NecromancerOrder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NecromancerPlayerState(boolean necromancer, NecromancerOrder selectedOrder) {
	public static final NecromancerPlayerState DEFAULT = new NecromancerPlayerState(true, NecromancerOrder.DEFAULT);
	public static final Codec<NecromancerPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("necromancer", true).forGetter(NecromancerPlayerState::necromancer),
		NecromancerOrder.CODEC.optionalFieldOf("selected_order", NecromancerOrder.DEFAULT).forGetter(NecromancerPlayerState::selectedOrder)
	).apply(instance, NecromancerPlayerState::new));

	public NecromancerPlayerState withNecromancer(boolean necromancer) {
		return new NecromancerPlayerState(necromancer, this.selectedOrder);
	}

	public NecromancerPlayerState withSelectedOrder(NecromancerOrder selectedOrder) {
		return new NecromancerPlayerState(this.necromancer, selectedOrder);
	}
}
