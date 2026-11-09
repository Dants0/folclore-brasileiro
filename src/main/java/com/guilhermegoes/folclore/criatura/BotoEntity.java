package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.registry.ModItems;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Boto-cor-de-rosa: by day a pink river dolphin who plays in the water and brings drowning
 * swimmers back to the surface; at night he walks out of the river as a young man in a white suit
 * and a white hat (that he never takes off), and dances by the shore. Bring him a fish and he gives
 * you a present. Strike him and his hat falls off, revealing who he is: he turns back into a
 * dolphin and dives away. The Chapéu do Boto makes you swim like him.
 */
public class BotoEntity extends Anfibia {
	private static final EntityDataAccessor<Boolean> HOMEM = SynchedEntityData.defineId(BotoEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDimensions GOLFINHO_DIM = EntityDimensions.scalable(0.9F, 0.6F).withEyeHeight(0.3F);
	private static final EntityDimensions HOMEM_DIM = EntityDimensions.scalable(0.6F, 1.9F).withEyeHeight(1.62F);
	private static final List<Item> PRESENTES = List.of(Items.EMERALD, Items.PINK_TULIP, Items.PRISMARINE_SHARD,
			Items.NAUTILUS_SHELL, Items.GOLD_NUGGET, Items.MUSIC_DISC_MALL, Items.HEART_OF_THE_SEA);

	private boolean semChapeu;
	private int esperaSalvar;
	private int esperaPresente;

	public BotoEntity(EntityType<? extends BotoEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 24.0)
				.add(Attributes.MOVEMENT_SPEED, 0.8)
				.add(Attributes.FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HOMEM, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new SalvarGoal());
		goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 20));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.35) {
			@Override
			public boolean canUse() {
				return homem() && super.canUse();
			}
		});
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
	}

	public boolean homem() {
		return entityData.get(HOMEM);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (HOMEM.equals(accessor)) {
			refreshDimensions();
		}
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return homem() ? HOMEM_DIM : GOLFINHO_DIM;
	}

	@Override
	protected boolean noturna() {
		return false;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.SPLASH;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return homem() ? null : SoundEvents.DOLPHIN_AMBIENT;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.DOLPHIN_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.DOLPHIN_DEATH;
	}

	// ------------------------------------------------------------------ changing shape

	private void virarHomem(ServerLevel level) {
		BlockPos margem = margem(level);
		if (margem == null) {
			return;
		}
		level.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 40, 0.5, 0.5, 0.5, 0.1);
		teleportTo(margem.getX() + 0.5, margem.getY(), margem.getZ() + 0.5);
		entityData.set(HOMEM, true);
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 20, 0.3, 0.8, 0.3, 0.02);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.DOLPHIN_JUMP, SoundSource.NEUTRAL, 1.0F, 0.8F);
	}

	private void virarGolfinho(ServerLevel level) {
		entityData.set(HOMEM, false);
		level.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1, getZ(), 40, 0.4, 0.8, 0.4, 0.1);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.DOLPHIN_PLAY, SoundSource.NEUTRAL, 1.0F, 1.0F);
		BlockPos agua = agua(level);
		if (agua != null) {
			teleportTo(agua.getX() + 0.5, agua.getY(), agua.getZ() + 0.5);
		}
	}

	/** Dry ground next to the water within 8 blocks, with room to stand. */
	private @Nullable BlockPos margem(ServerLevel level) {
		BlockPos c = blockPosition();
		for (BlockPos p : BlockPos.betweenClosed(c.offset(-8, -3, -8), c.offset(8, 4, 8))) {
			if (level.getFluidState(p).isEmpty() && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
					&& level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
				for (Direction d : Direction.Plane.HORIZONTAL) {
					if (level.getFluidState(p.relative(d).below()).isSource()) {
						return p.immutable();
					}
				}
			}
		}
		return null;
	}

	private @Nullable BlockPos agua(ServerLevel level) {
		BlockPos c = blockPosition();
		for (BlockPos p : BlockPos.betweenClosed(c.offset(-8, -4, -8), c.offset(8, 2, 8))) {
			if (level.getFluidState(p).isSource() && level.getFluidState(p.below()).isSource()) {
				return p.immutable();
			}
		}
		return null;
	}

	// ------------------------------------------------------------------ the young man in white

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!homem() || !stack.is(ItemTags.FISHES)) {
			return super.mobInteract(player, hand);
		}
		if (level() instanceof ServerLevel level && esperaPresente <= 0) {
			stack.consume(1, player);
			esperaPresente = 20 * 30;
			// Rare gifts are rarer: the further down the list, the less likely.
			int i = Math.min(random.nextInt(PRESENTES.size()), random.nextInt(PRESENTES.size()));
			spawnAtLocation(level, new ItemStack(PRESENTES.get(i)));
			level.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.1, getZ(), 3, 0.3, 0.2, 0.3, 0.0);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.DOLPHIN_PLAY, SoundSource.NEUTRAL, 1.0F, 1.3F);
			if (player instanceof ServerPlayer sp) {
				sp.sendOverlayMessage(Component.translatable("folclore.boto.presente"));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (homem() && source.getEntity() instanceof Player p) {
			// The hat falls: everyone sees the dolphin's blowhole. He runs back to the river.
			if (!semChapeu) {
				semChapeu = true;
				spawnAtLocation(level, new ItemStack(ModItems.CHAPEU_BOTO));
				if (p instanceof ServerPlayer sp) {
					sp.sendSystemMessage(Component.translatable("folclore.boto.revelado"));
				}
			}
			virarGolfinho(level);
			return false;
		}
		return super.hurtServer(level, source, damage);
	}

	// ------------------------------------------------------------------ tick

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		if (esperaSalvar > 0) {
			esperaSalvar--;
		}
		if (esperaPresente > 0) {
			esperaPresente--;
		}
		if (tickCount % 100 != 0) {
			return;
		}
		boolean noite = noite(level);
		if (!homem() && noite && !semChapeu && isInWater() && jogadorPerto(24) != null && random.nextInt(3) == 0) {
			virarHomem(level);
		} else if (homem() && !noite) {
			virarGolfinho(level);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Homem", homem());
		output.putBoolean("SemChapeu", semChapeu);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(HOMEM, input.getBooleanOr("Homem", false));
		semChapeu = input.getBooleanOr("SemChapeu", false);
	}

	/** Swims to a player who is running out of air and carries them up to the surface. */
	private class SalvarGoal extends Goal {
		private @Nullable ServerPlayer afogando;

		SalvarGoal() {
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (homem() || esperaSalvar > 0 || !(level() instanceof ServerLevel level)) {
				return false;
			}
			afogando = null;
			for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(20))) {
				if (p.isUnderWater() && p.getAirSupply() < p.getMaxAirSupply() * 0.4 && !p.isSpectator()) {
					afogando = p;
					break;
				}
			}
			return afogando != null;
		}

		@Override
		public boolean canContinueToUse() {
			return afogando != null && afogando.isAlive() && afogando.isUnderWater() && esperaSalvar == 0;
		}

		@Override
		public void tick() {
			if (afogando == null) {
				return;
			}
			getNavigation().moveTo(afogando, 1.6);
			if (distanceTo(afogando) < 2.5) {
				afogando.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 30, 0));
				afogando.setDeltaMovement(afogando.getDeltaMovement().add(0, 0.9, 0));
				afogando.needsSync = true;
				afogando.sendOverlayMessage(Component.translatable("folclore.boto.salvou"));
				level().playSound(null, getX(), getY(), getZ(), SoundEvents.DOLPHIN_PLAY, SoundSource.NEUTRAL, 1.0F, 1.0F);
				esperaSalvar = 20 * 20;
				afogando = null;
			}
		}

		@Override
		public void stop() {
			afogando = null;
		}
	}
}
