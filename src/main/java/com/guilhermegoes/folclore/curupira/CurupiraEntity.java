package com.guilhermegoes.folclore.curupira;

import java.util.List;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The Curupira: guardian of the forest, with flaming red hair and feet turned backwards. He comes
 * for whoever cuts down too many trees or kills too many wild animals. While he hunts you, you hear
 * his whistle and lose your bearings (nausea, and darkness now and then), and his footprints always
 * point the wrong way.
 *
 * <p>When badly hurt he melts back into the forest and returns healed, twice. An offering of honey
 * calms him down: he forgives you, leaves saplings to replant, and goes away.
 */
public class CurupiraEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> CALMO = SynchedEntityData.defineId(CurupiraEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int FUGAS = 2;
	private static final List<Item> MUDAS = List.of(Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.SPRUCE_SAPLING,
			Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING, Items.CHERRY_SAPLING);

	/** Times he can still escape when beaten. */
	private int fugas = FUGAS;
	/** Ticks with nobody to punish; he leaves after a minute. */
	private int ocioso;
	/** Set once he accepted an offering; counts down to his departure. */
	private int partindo;
	private int pisada;

	public CurupiraEntity(EntityType<? extends CurupiraEntity> type, Level level) {
		super(type, level);
		setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.LEAVES, 0.0F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CALMO, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0)
				.add(Attributes.MOVEMENT_SPEED, 0.33)
				.add(Attributes.ATTACK_DAMAGE, 5.0)
				.add(Attributes.ATTACK_KNOCKBACK, 0.6)
				.add(Attributes.FOLLOW_RANGE, 40.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
				(alvo, level) -> partindo == 0 && alvo instanceof ServerPlayer p && culpado(p)));
	}

	/** Whether the forest has a score to settle with this player. */
	private static boolean culpado(ServerPlayer p) {
		return !p.isCreative() && !p.isSpectator() && Desmatamento.pontos(p) >= Desmatamento.aviso();
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return getTarget() == null;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Fugas", fugas);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		fugas = input.getIntOr("Fugas", FUGAS);
	}

	// ------------------------------------------------------------------ sounds

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.NOTE_BLOCK_FLUTE.value();
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.FOX_SCREECH;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.FOX_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 1.4F + random.nextFloat() * 0.6F;
	}

	// ------------------------------------------------------------------ the offering

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.HONEY_BOTTLE) || partindo > 0) {
			return super.mobInteract(player, hand);
		}
		if (level() instanceof ServerLevel level && player instanceof ServerPlayer sp) {
			stack.consume(1, player);
			if (!player.isCreative() && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
				spawnAtLocation(level, new ItemStack(Items.GLASS_BOTTLE));
			}
			Desmatamento.perdoar(sp);
			setTarget(null);
			setLastHurtByMob(null);
			getNavigation().stop();
			partindo = 60;
			entityData.set(CALMO, true);
			for (int i = 0; i < 4; i++) {
				spawnAtLocation(level, new ItemStack(MUDAS.get(random.nextInt(MUDAS.size()))));
			}
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2, getZ(), 15, 0.5, 0.6, 0.5, 0.0);
			level.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 3, 0.3, 0.2, 0.3, 0.0);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.5F, 1.0F);
			sp.sendSystemMessage(Component.translatable("folclore.curupira.oferenda"));
		}
		return InteractionResult.SUCCESS;
	}

	// ------------------------------------------------------------------ being hurt

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (partindo > 0 && source.getEntity() instanceof Player) {
			return false;
		}
		boolean ferido = super.hurtServer(level, source, damage);
		if (ferido && isAlive() && fugas > 0 && getHealth() < getMaxHealth() * 0.3F) {
			fugas--;
			setHealth(getMaxHealth());
			sumirNaMata(level);
		}
		return ferido;
	}

	/** Leaves in a swirl of leaves to somewhere 12 to 20 blocks away. */
	private void sumirNaMata(ServerLevel level) {
		folhas(level);
		for (int i = 0; i < 16; i++) {
			double a = random.nextDouble() * Math.PI * 2;
			double d = 12 + random.nextDouble() * 8;
			if (randomTeleport(getX() + Math.cos(a) * d, getY() + random.nextInt(9) - 4, getZ() + Math.sin(a) * d, false,
					(BlockState s) -> s.is(BlockTags.ENDERMAN_DOES_NOT_TELEPORT_TO))) {
				break;
			}
		}
		folhas(level);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.HOSTILE, 2.0F, 0.7F);
	}

	private void folhas(ServerLevel level) {
		level.sendParticles(ParticleTypes.CHERRY_LEAVES, getX(), getY() + 1, getZ(), 40, 0.6, 0.9, 0.6, 0.05);
		level.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.8, getZ(), 12, 0.4, 0.6, 0.4, 0.02);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		// Killing the guardian of the forest is a burden you carry for a while.
		if (level() instanceof ServerLevel && source.getEntity() instanceof ServerPlayer p) {
			p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60 * 5, 0));
			p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 60 * 5, 0));
			p.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 20 * 60 * 20, 0));
			p.sendSystemMessage(Component.translatable("folclore.curupira.morte"));
		}
	}

	// ------------------------------------------------------------------ tick

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (partindo > 0) {
			if (--partindo == 0) {
				folhas(level);
				discard();
			}
			return;
		}

		// Backward footprints: the toes point to where he came from.
		if (getDeltaMovement().horizontalDistanceSqr() > 0.002 && onGround() && ++pisada % 5 == 0) {
			Direction ida = Direction.fromYRot(getYRot());
			PegadaBlock.marcar(level, blockPosition(), ida.getOpposite());
		}

		LivingEntity alvo = getTarget();
		if (alvo instanceof ServerPlayer p && p.isAlive() && distanceToSqr(p) < 24 * 24) {
			ocioso = 0;
			if (tickCount % 100 == 0) {
				assombrar(level, p);
			}
		} else if (++ocioso > 20 * 60) {
			folhas(level);
			discard();
		}
	}

	/** Makes {@code p} lose their way: a whistle from nowhere, dizziness, and sometimes darkness. */
	private void assombrar(ServerLevel level, ServerPlayer p) {
		Desmatamento.assobiar(level, p);
		p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 8, 0));
		if (random.nextInt(3) == 0) {
			p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 4, 0));
		}
		if (random.nextInt(4) == 0) {
			p.sendOverlayMessage(Component.translatable("folclore.curupira.perdido"));
		}
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	/** Used by the renderer: he accepted an offering and is leaving. */
	public boolean calmo() {
		return entityData.get(CALMO);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
		boolean ok = super.doHurtTarget(level, target);
		if (ok) {
			level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.7, getZ(), 6, 0.2, 0.15, 0.2, 0.01);
		}
		return ok;
	}
}
