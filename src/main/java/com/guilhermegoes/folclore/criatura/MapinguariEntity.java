package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.registry.ModItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mapinguari: the giant of the Amazon, covered in red fur, with a single eye and a mouth in the
 * middle of its belly. It smells so bad that standing near it makes you sick. Its hide stops almost
 * every blow (a fifth of the damage gets through), except from the front, where the mouth in its
 * belly is its weak spot. Roams jungles, by day.
 */
public class MapinguariEntity extends Assombracao {
	/** Cosine of the half-angle of the "belly" cone in front of it. */
	private static final double FRENTE = 0.5;

	public MapinguariEntity(EntityType<? extends MapinguariEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 120.0)
				.add(Attributes.MOVEMENT_SPEED, 0.24)
				.add(Attributes.ATTACK_DAMAGE, 10.0)
				.add(Attributes.ATTACK_KNOCKBACK, 2.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.STEP_HEIGHT, 1.1);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected boolean noturna() {
		return false;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.SNEEZE;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.RAVAGER_AMBIENT;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.RAVAGER_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.RAVAGER_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		Entity quem = source.getDirectEntity();
		if (quem != null && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			Vec3 frente = Vec3.directionFromRotation(0, getYRot());
			Vec3 ate = quem.position().subtract(position()).multiply(1, 0, 1).normalize();
			if (frente.dot(ate) > FRENTE) {
				// Right in the mouth on its belly.
				damage *= 1.5F;
				level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.6, getZ(), 15, 0.5, 0.5, 0.5, 0.2);
				if (source.getEntity() instanceof ServerPlayer p && random.nextInt(3) == 0) {
					p.sendOverlayMessage(Component.translatable("folclore.mapinguari.barriga"));
				}
			} else {
				damage *= 0.2F;
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 0.8F, 0.6F);
				if (source.getEntity() instanceof ServerPlayer p && random.nextInt(4) == 0) {
					p.sendOverlayMessage(Component.translatable("folclore.mapinguari.couro"));
				}
			}
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean ok = super.doHurtTarget(level, target);
		if (ok) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 0.8F);
		}
		return ok;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		spawnAtLocation(level, new ItemStack(Items.LEATHER, 4 + random.nextInt(5)));
		spawnAtLocation(level, new ItemStack(ModItems.GARRA_MAPINGUARI));
	}

	@Override
	protected int getBaseExperienceReward(ServerLevel level) {
		return 40;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		// The stench.
		if (tickCount % 5 == 0) {
			level.sendParticles(ParticleTypes.SNEEZE, getX(), getY() + 1.5, getZ(), 3, 0.9, 1.0, 0.9, 0.01);
		}
		if (tickCount % 40 == 0) {
			for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(6))) {
				if (!p.isCreative() && !p.isSpectator()) {
					p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 6, 0));
					p.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 2, 0));
					if (random.nextInt(3) == 0) {
						p.sendOverlayMessage(Component.translatable("folclore.mapinguari.fedor"));
					}
				}
			}
		}
		if (getTarget() != null && tickCount % 160 == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.6F);
		}
	}
}
