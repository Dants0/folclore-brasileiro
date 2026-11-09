package com.guilhermegoes.folclore.criatura;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Lobisomem: on full-moon nights a man turns into a wolf and roams the crossroads. He is fast,
 * leaps at his prey and hunts players and animals alike. The old cure is to wound him until he
 * bleeds: bring him low enough and the spell breaks, and in his place stands a frightened villager
 * who has no idea what happened. He also turns back at sunrise.
 */
public class LobisomemEntity extends Assombracao {
	private static final String[] NOMES = {"Seu Lobato", "Zé do Mato", "Tião Uivador", "Seu Lupércio", "Chico da Encruzilhada"};

	public LobisomemEntity(EntityType<? extends LobisomemEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 50.0)
				.add(Attributes.MOVEMENT_SPEED, 0.36)
				.add(Attributes.ATTACK_DAMAGE, 7.0)
				.add(Attributes.FOLLOW_RANGE, 40.0)
				.add(Attributes.ARMOR, 6.0)
				.add(Attributes.STEP_HEIGHT, 1.1);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.5F));
		goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
	}

	@Override
	protected boolean noturna() {
		return true;
	}

	/** At dawn he does not vanish: he turns back into a man. */
	@Override
	public void sumir() {
		if (level() instanceof ServerLevel level && noite(level) == false && isAlive()) {
			desencantar(level, null);
			return;
		}
		super.sumir();
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.LARGE_SMOKE;
	}

	private static SoundEvent som(int qual) {
		var s = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.ANGRY).adultSounds();
		return (qual == 0 ? s.growlSound() : qual == 1 ? s.hurtSound() : s.deathSound()).value();
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return som(0);
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return som(1);
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return som(2);
	}

	@Override
	public float getVoicePitch() {
		return 0.5F + random.nextFloat() * 0.1F;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean ferido = super.hurtServer(level, source, damage);
		if (ferido && isAlive() && getHealth() < getMaxHealth() * 0.25F) {
			desencantar(level, source.getEntity() instanceof ServerPlayer p ? p : null);
		}
		return ferido;
	}

	/** The spell breaks: a dazed villager stands where the wolf was. */
	private void desencantar(ServerLevel level, @Nullable ServerPlayer quem) {
		Villager homem = EntityTypes.VILLAGER.spawn(level, blockPosition(), EntitySpawnReason.CONVERSION);
		if (homem != null) {
			homem.setYRot(getYRot());
			homem.setCustomName(Component.literal(NOMES[random.nextInt(NOMES.length)]));
			homem.setPersistenceRequired();
		}
		level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 40, 0.5, 1.0, 0.5, 0.03);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.HOSTILE, 1.0F, 0.9F);
		if (quem != null) {
			quem.sendSystemMessage(Component.translatable("folclore.lobisomem.desencantado",
					homem != null ? homem.getCustomName() : Component.literal("?")));
		}
		discard();
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		if (tickCount % 400 == 0) {
			// The howl at the moon.
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.SAD).adultSounds().whineSound().value(),
					SoundSource.HOSTILE, 3.0F, 0.4F);
		}
	}
}
