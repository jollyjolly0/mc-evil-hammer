package com.example.necromancer.state;

import com.example.necromancer.NecromancerOrder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class NecromancerState {
	private NecromancerState() {
	}

	public static NecromancerPlayerState getPlayerState(Player player) {
		return player.getAttachedOrSet(NecromancerAttachments.PLAYER_STATE, NecromancerPlayerState.DEFAULT);
	}

	public static void setPlayerState(Player player, NecromancerPlayerState state) {
		player.setAttached(NecromancerAttachments.PLAYER_STATE, state);
	}

	public static boolean isNecromancer(Player player) {
		return getPlayerState(player).necromancer();
	}

	public static void setNecromancer(Player player, boolean necromancer) {
		setPlayerState(player, getPlayerState(player).withNecromancer(necromancer));
	}

	public static NecromancerOrder getSelectedOrder(Player player) {
		return getPlayerState(player).selectedOrder();
	}

	public static void setSelectedOrder(Player player, NecromancerOrder order) {
		setPlayerState(player, getPlayerState(player).withSelectedOrder(order));
	}

	public static ControlledMobState getControlledMobState(Mob mob) {
		return mob.getAttached(NecromancerAttachments.CONTROLLED_MOB_STATE);
	}

	public static void setControlledMobState(Mob mob, ControlledMobState state) {
		mob.setAttached(NecromancerAttachments.CONTROLLED_MOB_STATE, state);
	}

	public static void clearControlledMobState(Mob mob) {
		mob.removeAttached(NecromancerAttachments.CONTROLLED_MOB_STATE);
	}
}
