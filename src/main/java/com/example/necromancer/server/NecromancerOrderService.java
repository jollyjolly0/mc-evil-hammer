package com.example.necromancer.server;

import com.example.ExampleMod;
import com.example.necromancer.NecromancerOrder;
import com.example.necromancer.state.ControlledMobState;
import com.example.necromancer.state.NecromancerState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public final class NecromancerOrderService {
	private static final double CONTROL_RADIUS = 24.0;
	private static final double FOLLOW_SPEED = 1.15;
	private static final double FOLLOW_STOP_DISTANCE_SQR = 9.0;
	private static final double ATTACK_MOVE_SPEED = 1.2;
	private static final double ATTACK_MOVE_DISTANCE = 20.0;
	private static final double ATTACK_MOVE_REACHED_DISTANCE_SQR = 4.0;
	private static final int ATTACK_MOVE_DURATION_TICKS = 200;

	private NecromancerOrderService() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(NecromancerOrderService::tickServer);
	}

	public static int issueOrder(ServerPlayer player, NecromancerOrder order) {
		if (!NecromancerState.isNecromancer(player)) {
			return 0;
		}

		NecromancerState.setSelectedOrder(player, order);

		int affectedMobCount = switch (order) {
			case DEFAULT -> releaseControlledMobs(player);
			case FOLLOW -> assignNearbyHostiles(player, order, Optional.empty(), 0);
			case ATTACK -> assignNearbyHostiles(player, order, Optional.of(computeAttackMoveTarget(player)), player.level().getServer().getTickCount() + ATTACK_MOVE_DURATION_TICKS);
		};

		ExampleMod.LOGGER.info(
			"Necromancer order '{}' recorded for player {} affecting {} hostile mobs",
			order.id(),
			player.getName().getString(),
			affectedMobCount
		);

		return affectedMobCount;
	}

	public static int releaseControlledMobs(ServerPlayer player) {
		int releasedCount = 0;

		for (ServerLevel level : player.level().getServer().getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (!(entity instanceof Mob mob)) {
					continue;
				}

				ControlledMobState state = NecromancerState.getControlledMobState(mob);
				if (state == null || !state.controllerUuid().equals(player.getUUID())) {
					continue;
				}

				releaseMob(mob);
				releasedCount++;
			}
		}

		return releasedCount;
	}

	private static int assignNearbyHostiles(ServerPlayer player, NecromancerOrder order, Optional<BlockPos> targetPos, int expiryTick) {
		ServerLevel level = player.level();
		AABB controlBounds = player.getBoundingBox().inflate(CONTROL_RADIUS);
		int claimedCount = 0;

		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof Mob mob) || !isControllableHostile(mob)) {
				continue;
			}

			ControlledMobState existingState = NecromancerState.getControlledMobState(mob);
			boolean alreadyControlledByPlayer = existingState != null && existingState.controllerUuid().equals(player.getUUID());
			boolean nearby = controlBounds.contains(mob.position());

			if (!nearby && !alreadyControlledByPlayer) {
				continue;
			}

			NecromancerState.setControlledMobState(mob, new ControlledMobState(player.getUUID(), order, expiryTick, targetPos));
			if (isNecromancerProtectedTarget(mob, mob.getTarget())) {
				mob.setTarget(null);
			}
			if (order == NecromancerOrder.ATTACK) {
				mob.setTarget(null);
			}
			claimedCount++;
		}

		return claimedCount;
	}

	private static void tickServer(MinecraftServer server) {
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (!(entity instanceof Mob mob)) {
					continue;
				}

				ControlledMobState state = NecromancerState.getControlledMobState(mob);
				if (state == null) {
					continue;
				}

				tickControlledMob(server, mob, state);
			}
		}
	}

	private static void tickControlledMob(MinecraftServer server, Mob mob, ControlledMobState state) {
		if (!mob.isAlive() || !isControllableHostile(mob)) {
			releaseMob(mob);
			return;
		}

		ServerPlayer controller = server.getPlayerList().getPlayer(state.controllerUuid());
		if (controller == null || !controller.isAlive() || !NecromancerState.isNecromancer(controller) || controller.level() != mob.level()) {
			releaseMob(mob);
			return;
		}

		LivingEntity currentTarget = mob.getTarget();
		if (isNecromancerProtectedTarget(mob, currentTarget)) {
			mob.setTarget(null);
			currentTarget = null;
		}

		switch (state.order()) {
			case DEFAULT -> releaseMob(mob);
			case FOLLOW -> tickFollowOrder(mob, controller, currentTarget);
			case ATTACK -> tickAttackOrder(server, mob, state, currentTarget);
		}
	}

	private static void tickFollowOrder(Mob mob, ServerPlayer controller, LivingEntity currentTarget) {
		if (isValidCombatTarget(mob, currentTarget)) {
			return;
		}

		double distanceToController = mob.distanceToSqr(controller);
		if (distanceToController <= FOLLOW_STOP_DISTANCE_SQR) {
			mob.getNavigation().stop();
			return;
		}

		mob.getNavigation().moveTo(controller, FOLLOW_SPEED);
	}

	private static void tickAttackOrder(MinecraftServer server, Mob mob, ControlledMobState state, LivingEntity currentTarget) {
		if (isValidCombatTarget(mob, currentTarget)) {
			releaseMob(mob);
			return;
		}

		if (state.orderExpiryTick() > 0 && server.getTickCount() >= state.orderExpiryTick()) {
			releaseMob(mob);
			return;
		}

		BlockPos destination = state.orderTargetPos().orElse(null);
		if (destination == null) {
			releaseMob(mob);
			return;
		}

		Vec3 destinationCenter = Vec3.atBottomCenterOf(destination);
		if (mob.distanceToSqr(destinationCenter) <= ATTACK_MOVE_REACHED_DISTANCE_SQR) {
			releaseMob(mob);
			return;
		}

		mob.getNavigation().moveTo(destinationCenter.x, destinationCenter.y, destinationCenter.z, ATTACK_MOVE_SPEED);
	}

	private static boolean isValidCombatTarget(Mob mob, LivingEntity target) {
		return target != null && target.isAlive() && !isNecromancerProtectedTarget(mob, target) && mob.canAttack(target);
	}

	private static boolean isNecromancerProtectedTarget(Mob mob, LivingEntity target) {
		return target instanceof ServerPlayer serverPlayer && isControllableHostile(mob) && NecromancerState.isNecromancer(serverPlayer);
	}

	private static boolean isControllableHostile(Mob mob) {
		return mob instanceof Enemy;
	}

	private static BlockPos computeAttackMoveTarget(ServerPlayer player) {
		Vec3 lookVector = player.getLookAngle();
		Vec3 horizontalLook = new Vec3(lookVector.x, 0.0, lookVector.z);
		if (horizontalLook.lengthSqr() < 1.0E-6) {
			horizontalLook = new Vec3(0.0, 0.0, 1.0);
		}

		Vec3 destination = player.position().add(horizontalLook.normalize().scale(ATTACK_MOVE_DISTANCE));
		return BlockPos.containing(destination.x, player.getY(), destination.z);
	}

	private static void releaseMob(Mob mob) {
		NecromancerState.clearControlledMobState(mob);
		if (mob.getTarget() == null) {
			mob.getNavigation().stop();
		}
	}
}
