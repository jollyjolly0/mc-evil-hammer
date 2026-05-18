package com.example.necromancer.state;

import com.example.ExampleMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class NecromancerAttachments {
	public static final AttachmentType<NecromancerPlayerState> PLAYER_STATE = AttachmentRegistry.create(
		id("player_state"),
		builder -> builder
			.initializer(() -> NecromancerPlayerState.DEFAULT)
			.persistent(NecromancerPlayerState.CODEC)
			.copyOnDeath()
	);

	public static final AttachmentType<ControlledMobState> CONTROLLED_MOB_STATE = AttachmentRegistry.create(
		id("controlled_mob_state"),
		builder -> builder.persistent(ControlledMobState.CODEC)
	);

	private NecromancerAttachments() {
	}

	public static void initialize() {
		// Trigger static initialization during mod startup.
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, path);
	}
}
