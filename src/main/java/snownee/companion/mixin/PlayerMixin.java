package snownee.companion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import snownee.companion.CompanionCommonConfig;
import snownee.companion.CompanionPlayer;

@Mixin(Player.class)
public abstract class PlayerMixin implements CompanionPlayer {

	@Inject(at = @At("TAIL"), method = "aiStep")
	private void companion_aiStep(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (player.level.isClientSide) {
			return;
		}
		if (player.isSleeping() || player.isInPowderSnow) {
			removeEntitiesOnShoulder();
			return;
		}
		if (CompanionCommonConfig.shoulderDismountInWater && player.isInWater()) {
			removeEntitiesOnShoulder();
			return;
		}
		if (CompanionCommonConfig.shoulderDismountUnderWater && player.isUnderWater()) {
			removeEntitiesOnShoulder();
			return;
		}
		if (player.fallDistance > CompanionCommonConfig.shoulderDismountFallDistance) {
			removeEntitiesOnShoulder();
			return;
		}
		if (CompanionCommonConfig.shoulderDismountWhileFlying && player.getAbilities().flying) {
			removeEntitiesOnShoulder();
			return;
		}
		//TODO under lava???
	}

	@Inject(at = @At("TAIL"), method = "hurt")
	private void companion_hurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> ci) {
		if (f > CompanionCommonConfig.shoulderDismountDamageThreshold) {
			removeEntitiesOnShoulder();
		}
	}

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V"
			), method = { "aiStep", "hurt" }
	)
	protected void nullifyDefaultRemoveEntitiesOnShoulder(Player player) {
		// NOOP
	}

	@Shadow
	abstract void removeEntitiesOnShoulder();

	private Vec3 jumpPos;

	@Override
	public Vec3 getJumpPos() {
		return jumpPos;
	}

	@Override
	public void setJumpPos(Vec3 pos) {
		this.jumpPos = pos;
	}

	@Override
	public void removeShoulderEntities() {
		removeEntitiesOnShoulder();
	}

	@Inject(at = @At("HEAD"), method = "jumpFromGround")
	private void companion_jumpFromGround(CallbackInfo ci) {
		jumpPos = ((Player) (Object) this).position();
	}

}