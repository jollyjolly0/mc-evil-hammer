package com.example.necromancer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum NecromancerOrder {
	DEFAULT("default"),
	FOLLOW("follow"),
	ATTACK("attack");

	public static final Codec<NecromancerOrder> CODEC = Codec.STRING.comapFlatMap(
		NecromancerOrder::parse,
		NecromancerOrder::id
	);

	private final String id;

	NecromancerOrder(String id) {
		this.id = id;
	}

	public String id() {
		return this.id;
	}

	public static DataResult<NecromancerOrder> parse(String id) {
		for (NecromancerOrder order : values()) {
			if (order.id.equals(id)) {
				return DataResult.success(order);
			}
		}

		return DataResult.error(() -> "Unknown necromancer order: " + id);
	}
}
