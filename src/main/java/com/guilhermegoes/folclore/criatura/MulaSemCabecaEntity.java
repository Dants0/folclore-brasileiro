package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.registry.ModItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Mula sem Cabeça: a mule with no head and fire pouring out of its neck, who gallops through the
 * night (more often from Thursday to Friday), screaming like a horse and trampling whoever crosses
 * her path. The only cure is to pull off her iron bridle: once she is worn out (below half health),
 * right-click her with an empty hand. She vanishes, the curse broken, and the Freio da Mula stays
 * with you.
 */
public class MulaSemCabecaEntity extends Assombracao {
	private static final EntityDataAccessor<Boolean> CANSADA = SynchedEntityData.defineId(MulaSemCabecaEntity.class, EntityDataSerializers.BOOLEAN);

	public MulaSemCabecaEntity(EntityType<? extends MulaSemCabecaEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0)
				.add(Attributes.MOVEMENT_SPEED, 0.36)
				.add(Attributes.ATTACK_DAMAGE, 6.0)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5)
				.add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
				.add(Attributes.STEP_HEIGHT, 1.1);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CANSADA, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.2));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				(alvo, level) -> !cansada()));
	}

	/** Worn out: she stops charging, and her bridle can be pulled off. */
	public boolean cansada() {
		return entityData.get(CANSADA);
	}

	@Override
	protected boolean noturna() {
		return true;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.FLAME;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.HORSE_ANGRY;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.HORSE_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.HORSE_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F + random.nextFloat() * 0.2F;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean ok = super.doHurtTarget(level, target);
		if (ok) {
			target.igniteForSeconds(3.0F);
		}
		return ok;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean ferida = super.hurtServer(level, source, damage);
		if (ferida && isAlive() && !cansada() && getHealth() < getMaxHealth() / 2) {
			entityData.set(CANSADA, true);
			setTarget(null);
			getNavigation().stop();
			if (source.getEntity() instanceof ServerPlayer p) {
				p.sendOverlayMessage(Component.translatable("folclore.mula.cansada"));
			}
		}
		return ferida;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!cansada() || !player.getItemInHand(hand).isEmpty()) {
			return super.mobInteract(player, hand);
		}
		if (level() instanceof ServerLevel level && player instanceof ServerPlayer sp) {
			// The bridle comes off: the curse is broken.
			spawnAtLocation(level, new ItemStack(ModItems.FREIO_MULA));
			level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 40, 0.6, 0.8, 0.6, 0.05);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 1.0F, 1.2F);
			sp.sendSystemMessage(Component.translatable("folclore.mula.desencantada"));
			discard();
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		// Fire from the neck, and sparks from the iron shoes when she gallops.
		level.sendParticles(ParticleTypes.FLAME, getX() - Math.sin(getYRot() * Math.PI / 180) * 0.9, getY() + 1.4,
				getZ() + Math.cos(getYRot() * Math.PI / 180) * 0.9, 2, 0.1, 0.15, 0.1, 0.02);
		boolean galopando = getDeltaMovement().horizontalDistanceSqr() > 0.01;
		if (galopando && onGround()) {
			if (tickCount % 3 == 0) {
				level.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.1, getZ(), 1, 0.3, 0, 0.3, 0);
			}
			if (tickCount % 6 == 0) {
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 0.8F, 0.8F);
			}
		}
		if (cansada()) {
			if (tickCount % 10 == 0) {
				level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.4, getZ(), 3, 0.1, 0.1, 0.1, 0.01);
			}
			if (getHealth() >= getMaxHealth() * 0.8F) {
				entityData.set(CANSADA, false); // got her breath back
			} else if (tickCount % 40 == 0) {
				heal(1.0F);
			}
		}
	}

	@Override
	public boolean fireImmune() {
		return true;
	}
}
