package com.example.mixin;

import com.example.necromancer.state.NecromancerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobTargetingMixin {
	@Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
	private void evilhammer$preventHostilesFromAttackingNecromancers(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (evilhammer$isProtectedNecromancerTarget(target)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "setTarget", at = @At("TAIL"))
	private void evilhammer$clearNecromancerTargetsAfterAssignment(LivingEntity target, CallbackInfo ci) {
		if (!evilhammer$isProtectedNecromancerTarget(target)) {
			return;
		}

		Mob mob = (Mob) (Object) this;
		if (mob.getTarget() == target) {
			mob.setTarget(null);
		}
	}

	private boolean evilhammer$isProtectedNecromancerTarget(LivingEntity target) {
		return ((Object) this) instanceof Enemy && target instanceof ServerPlayer player && NecromancerState.isNecromancer(player);
	}
}
