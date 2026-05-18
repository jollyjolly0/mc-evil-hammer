package com.example.necromancer.server;

import com.example.necromancer.NecromancerOrder;
import com.example.necromancer.state.NecromancerState;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class NecromancerCommands {
	private NecromancerCommands() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			Commands.literal("evilhammer")
				.requires(CommandSourceStack::isPlayer)
				.then(Commands.literal("necromancer")
					.executes(NecromancerCommands::showNecromancerState)
					.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(context -> setNecromancerState(context, BoolArgumentType.getBool(context, "enabled")))))
				.then(Commands.literal("order")
					.executes(NecromancerCommands::showSelectedOrder)
					.then(Commands.literal(NecromancerOrder.DEFAULT.id())
						.executes(context -> setSelectedOrder(context, NecromancerOrder.DEFAULT)))
					.then(Commands.literal(NecromancerOrder.FOLLOW.id())
						.executes(context -> setSelectedOrder(context, NecromancerOrder.FOLLOW)))
					.then(Commands.literal(NecromancerOrder.ATTACK.id())
						.executes(context -> setSelectedOrder(context, NecromancerOrder.ATTACK))))
		));
	}

	private static int showNecromancerState(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		String stateText = NecromancerState.isNecromancer(player) ? "enabled" : "disabled";
		context.getSource().sendSuccess(() -> Component.literal("Necromancer status is " + stateText + "."), false);
		return 1;
	}

	private static int setNecromancerState(CommandContext<CommandSourceStack> context, boolean enabled) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		NecromancerState.setNecromancer(player, enabled);
		int releasedCount = enabled ? 0 : NecromancerOrderService.releaseControlledMobs(player);
		context.getSource().sendSuccess(
			() -> Component.literal("Necromancer status set to " + enabled + ". Released " + releasedCount + " controlled hostile mobs."),
			false
		);
		return 1;
	}

	private static int showSelectedOrder(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		NecromancerOrder order = NecromancerState.getSelectedOrder(player);
		context.getSource().sendSuccess(() -> Component.literal("Current necromancer order is '" + order.id() + "'."), false);
		return 1;
	}

	private static int setSelectedOrder(CommandContext<CommandSourceStack> context, NecromancerOrder order) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		if (!NecromancerState.isNecromancer(player)) {
			context.getSource().sendFailure(Component.literal("You must be a necromancer to issue hostile mob orders."));
			return 0;
		}

		int affectedMobCount = NecromancerOrderService.issueOrder(player, order);
		String feedback = switch (order) {
			case DEFAULT -> "Released " + affectedMobCount + " hostile mobs back to default behavior.";
			case FOLLOW -> "Ordered " + affectedMobCount + " hostile mobs to follow you.";
			case ATTACK -> "Ordered " + affectedMobCount + " hostile mobs to attack-move forward.";
		};
		context.getSource().sendSuccess(() -> Component.literal(feedback), false);
		return 1;
	}
}
