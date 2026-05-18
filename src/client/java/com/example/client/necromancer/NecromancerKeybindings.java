package com.example.client.necromancer;

import com.example.ExampleMod;
import com.example.necromancer.NecromancerOrder;
import com.example.necromancer.network.NecromancerOrderPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.lwjgl.glfw.GLFW;

public final class NecromancerKeybindings {
	private static final KeyMapping FOLLOW_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key." + ExampleMod.MOD_ID + ".follow",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_C,
		KeyMapping.Category.GAMEPLAY
	));
	private static final KeyMapping ATTACK_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key." + ExampleMod.MOD_ID + ".attack",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		KeyMapping.Category.GAMEPLAY
	));
	private static final KeyMapping DEFAULT_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key." + ExampleMod.MOD_ID + ".default",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		KeyMapping.Category.GAMEPLAY
	));

	private NecromancerKeybindings() {
	}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(NecromancerKeybindings::handleKeyPresses);
	}

	private static void handleKeyPresses(Minecraft client) {
		if (client.player == null || client.level == null || client.screen != null) {
			return;
		}

		while (FOLLOW_KEY.consumeClick()) {
			triggerOrder(client, NecromancerOrder.FOLLOW);
		}

		while (ATTACK_KEY.consumeClick()) {
			triggerOrder(client, NecromancerOrder.ATTACK);
		}

		while (DEFAULT_KEY.consumeClick()) {
			triggerOrder(client, NecromancerOrder.DEFAULT);
		}
	}

	private static void triggerOrder(Minecraft client, NecromancerOrder order) {
		ClientPlayNetworking.send(new NecromancerOrderPayload(order));
		spawnActivationParticles(client, client.player, order);
	}

	private static void spawnActivationParticles(Minecraft client, LocalPlayer player, NecromancerOrder order) {
		ParticleOptions particle = switch (order) {
			case FOLLOW -> ParticleTypes.SOUL;
			case ATTACK -> ParticleTypes.FLAME;
			case DEFAULT -> ParticleTypes.ENCHANT;
		};

		double centerX = player.getX();
		double centerY = player.getY() + 1.0;
		double centerZ = player.getZ();

		for (int i = 0; i < 14; i++) {
			double angle = (Math.PI * 2.0 * i) / 14.0;
			double radius = 0.6 + (i % 3) * 0.08;
			double x = centerX + Math.cos(angle) * radius;
			double y = centerY + (i % 4) * 0.05;
			double z = centerZ + Math.sin(angle) * radius;
			double vx = Math.cos(angle) * 0.02;
			double vy = 0.03 + (i % 2) * 0.01;
			double vz = Math.sin(angle) * 0.02;
			client.level.addParticle(particle, x, y, z, vx, vy, vz);
		}
	}
}
