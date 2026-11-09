package com.guilhermegoes.folclore.criatura;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Cuca: the old witch with the head of an alligator who comes for whoever does not sleep. Stay
 * awake for {@link #NOITES} nights and she shows up in the dark, cackling, throwing potions and
 * casting a heavy sleep on you. Go to bed and she gives up and leaves. She drops potions when
 * defeated.
 */
public class CucaEntity extends Assombracao implements RangedAttackMob {
	/** Nights without sleep that bring the Cuca. */
	public static final int NOITES = 3;
	private static final List<Holder<Potion>> POCOES = List.of(Potions.SLOWNESS, Potions.WEAKNESS, Potions.POISON, Potions.HARMING);
	private static final List<Holder<Potion>> ESPOLIO = List.of(Potions.HEALING, Potions.REGENERATION, Potions.SWIFTNESS,
			Potions.NIGHT_VISION, Potions.STRENGTH);

	public CucaEntity(EntityType<? extends CucaEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0)
				.add(Attributes.MOVEMENT_SPEED, 0.28)
				.add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 50, 12.0F));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				(alvo, level) -> alvo instanceof ServerPlayer p && insone(p)));
	}

	/** Whether {@code p} has gone {@link #NOITES} nights or more without sleeping. */
	public static boolean insone(ServerPlayer p) {
		return !p.isCreative() && !p.isSpectator()
				&& p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= 24000 * NOITES;
	}

	@Override
	protected boolean noturna() {
		return true;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.WITCH;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.WITCH_CELEBRATE;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.WITCH_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.WITCH_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.7F + random.nextFloat() * 0.1F;
	}

	@Override
	public void performRangedAttack(LivingEntity alvo, float forca) {
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 m = alvo.getDeltaMovement();
		double dx = alvo.getX() + m.x - getX();
		double dy = alvo.getEyeY() - 1.1 - getY();
		double dz = alvo.getZ() + m.z - getZ();
		double d = Math.sqrt(dx * dx + dz * dz);
		ItemStack pocao = PotionContents.createItemStack(Items.SPLASH_POTION, POCOES.get(random.nextInt(POCOES.size())));
		Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, pocao, this, dx, dy + d * 0.2, dz, 0.75F, 8.0F);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_THROW, SoundSource.HOSTILE, 1.0F, 0.8F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 0; i < 2; i++) {
			spawnAtLocation(level, PotionContents.createItemStack(Items.POTION, ESPOLIO.get(random.nextInt(ESPOLIO.size()))));
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		if (tickCount % 4 == 0) {
			level.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1.8, getZ(), 1, 0.3, 0.3, 0.3, 0.0);
		}
		LivingEntity alvo = getTarget();
		if (alvo instanceof ServerPlayer p) {
			if (!insone(p)) {
				// The child went to sleep: nothing left for her here.
				p.sendOverlayMessage(Component.translatable("folclore.cuca.desistiu"));
				sumir();
				return;
			}
			if (tickCount % 200 == 0 && distanceTo(p) < 14) {
				// The lullaby: a sleep so heavy you can barely move.
				p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 4, 0));
				p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 4, 2));
				p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 6, 1));
				p.sendOverlayMessage(Component.translatable("folclore.cuca.sono"));
				level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.6F);
			}
		}
	}
}
