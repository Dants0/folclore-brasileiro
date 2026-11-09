package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Iara, the mother of the waters: she sings in the rivers from dusk to dawn, and whoever looks at
 * her while she sings is drawn into the water, step by step. Look away to break the spell. Anyone
 * who reaches her in the water is pulled down to the bottom. Hurting her makes her dive and swim
 * away. She leaves the Escama da Iara when defeated.
 */
public class IaraEntity extends Anfibia {
	private static final EntityDataAccessor<Boolean> CANTANDO = SynchedEntityData.defineId(IaraEntity.class, EntityDataSerializers.BOOLEAN);
	private static final double ALCANCE_CANTO = 24.0;
	/** How exactly you must be looking at her to fall under the spell (cosine of the angle). */
	private static final double OLHAR = 0.93;

	public IaraEntity(EntityType<? extends IaraEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0)
				.add(Attributes.MOVEMENT_SPEED, 0.6)
				.add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CANTANDO, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.6, 40));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F, 1.0F));
	}

	public boolean cantando() {
		return entityData.get(CANTANDO);
	}

	@Override
	protected boolean noturna() {
		return true;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.SPLASH;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean ferida = super.hurtServer(level, source, damage);
		if (ferida && isAlive() && random.nextInt(2) == 0) {
			mergulhar(level);
		}
		return ferida;
	}

	/** Dives and swims off to another spot in the water, up to 16 blocks away. */
	private void mergulhar(ServerLevel level) {
		for (int i = 0; i < 16; i++) {
			BlockPos p = blockPosition().offset(random.nextInt(33) - 16, random.nextInt(7) - 4, random.nextInt(33) - 16);
			if (level.getFluidState(p).isSource() && level.getFluidState(p.above()).isSource() == false) {
				level.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1, getZ(), 30, 0.5, 0.3, 0.5, 0.1);
				teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
				level.sendParticles(ParticleTypes.BUBBLE, getX(), getY() + 1, getZ(), 30, 0.5, 0.5, 0.5, 0.1);
				return;
			}
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		spawnAtLocation(level, new ItemStack(ModItems.ESCAMA_IARA, 1 + random.nextInt(2)));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		boolean cantar = isInWater() || level.getFluidState(blockPosition().below()).isSource();
		entityData.set(CANTANDO, cantar);
		if (!cantar) {
			return;
		}
		if (tickCount % 40 == 0) {
			// The song: soft bells over the water.
			float tom = 0.8F + (tickCount / 40 % 4) * 0.15F;
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0F, tom);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.HOSTILE, 1.5F, 0.7F);
			level.sendParticles(ParticleTypes.NOTE, getX(), getY() + 2.2, getZ(), 1, 0.3, 0.2, 0.3, 1.0);
		}
		for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(ALCANCE_CANTO))) {
			if (p.isCreative() || p.isSpectator()) {
				continue;
			}
			double d = distanceTo(p);
			if (d < 3.0 && p.isInWater()) {
				// In her arms: down to the bottom of the river.
				p.setDeltaMovement(p.getDeltaMovement().add(0, -0.08, 0));
				p.needsSync = true;
				continue;
			}
			if (!encantado(p)) {
				continue;
			}
			Vec3 puxao = position().subtract(p.position()).normalize().scale(0.06);
			p.setDeltaMovement(p.getDeltaMovement().add(puxao.x, 0, puxao.z));
			p.needsSync = true;
			p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, false));
			if (tickCount % 60 == 0) {
				p.sendOverlayMessage(Component.translatable("folclore.iara.encanto"));
			}
		}
	}

	/** Whether {@code p} is looking right at her while she sings. */
	private boolean encantado(ServerPlayer p) {
		Vec3 ate = getEyePosition().subtract(p.getEyePosition());
		return ate.normalize().dot(p.getViewVector(1.0F)) > OLHAR && p.hasLineOfSight(this);
	}
}
