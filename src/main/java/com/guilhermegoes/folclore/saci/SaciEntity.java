package com.guilhermegoes.folclore.saci;

import com.guilhermegoes.folclore.registry.ModItems;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Saci-Pererê: a one-legged boy in a red cap who travels inside whirlwinds. He sneaks up on
 * players and steals one stack from their inventory, then runs off laughing with it in his hand.
 * No weapon can hurt him (every blow just sends him whirling away); only a peneira thrown over him
 * catches him, and then he gives back what he took and leaves his cap behind.
 *
 * <p>Between robberies he plays pranks: he blows out torches and tangles horses' manes.
 */
public class SaciEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> RINDO = SynchedEntityData.defineId(SaciEntity.class, EntityDataSerializers.BOOLEAN);

	/** A Saci with empty hands leaves after this long, if nobody is around. */
	private static final int VIDA = 20 * 60 * 5;
	private static final double ALCANCE_ROUBO = 1.6;

	/** Ticks left in the current fit of laughter (he stands still meanwhile). */
	private int riso;
	/** Ticks until he may try a new prank. */
	private int pegadinha = 100;
	/** Ticks until he may rob again after being caught empty-handed or failing. */
	private int esperaRoubo = 200;
	private int idade;

	public SaciEntity(EntityType<? extends SaciEntity> type, Level level) {
		super(type, level);
		setDropChance(EquipmentSlot.MAINHAND, 2.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.32)
				.add(Attributes.FOLLOW_RANGE, 24.0)
				.add(Attributes.SAFE_FALL_DISTANCE, 64.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RINDO, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new RirGoal());
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 7.0F, 1.3, 1.6,
				e -> temRoubo() && e instanceof Player p && !p.isSpectator()));
		goalSelector.addGoal(3, new RoubarGoal());
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	// ------------------------------------------------------------------ state

	public boolean rindo() {
		return entityData.get(RINDO);
	}

	/** Whether he is carrying something he stole. */
	public boolean temRoubo() {
		return !getMainHandItem().isEmpty();
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !temRoubo();
	}

	@Override
	public boolean isPersistenceRequired() {
		return temRoubo() || super.isPersistenceRequired();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Idade", idade);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		idade = input.getIntOr("Idade", 0);
	}

	// ------------------------------------------------------------------ sounds

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.WITCH_CELEBRATE;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.WITCH_CELEBRATE;
	}

	@Override
	public float getVoicePitch() {
		return 1.6F + random.nextFloat() * 0.3F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 160;
	}

	private void gargalhar() {
		level().playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 1.2F, getVoicePitch());
	}

	// ------------------------------------------------------------------ the whirlwind

	/** A small spiral of wind and dust at his feet. */
	private void redemoinho(ServerLevel level, int voltas) {
		double t = tickCount * 0.6;
		for (int i = 0; i < voltas; i++) {
			double a = t + i * Math.PI * 2 / voltas;
			double h = (i % 4) * 0.35;
			double r = 0.3 + h * 0.4;
			level.sendParticles(ParticleTypes.CLOUD, getX() + Math.cos(a) * r, getY() + h, getZ() + Math.sin(a) * r,
					1, 0, 0, 0, 0.0);
		}
		BlockState chao = level.getBlockState(blockPosition().below());
		if (!chao.isAir() && random.nextInt(3) == 0) {
			level.sendParticles(ParticleTypes.DUST_PLUME, getX(), getY() + 0.1, getZ(), 2, 0.3, 0.05, 0.3, 0.02);
		}
	}

	/** A big whirlwind burst: used when he arrives, vanishes or escapes a blow. */
	public void rajada(ServerLevel level) {
		level.sendParticles(ParticleTypes.GUST, getX(), getY() + 0.6, getZ(), 2, 0.3, 0.4, 0.3, 0.0);
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.6, getZ(), 30, 0.5, 0.8, 0.5, 0.08);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BREEZE_INHALE, SoundSource.HOSTILE, 1.0F, 1.4F);
	}

	/** Whirls away to a random spot 8 to 16 blocks off. */
	public boolean fugir() {
		if (!(level() instanceof ServerLevel level)) {
			return false;
		}
		rajada(level);
		for (int i = 0; i < 16; i++) {
			double a = random.nextDouble() * Math.PI * 2;
			double d = 8 + random.nextDouble() * 8;
			double x = getX() + Math.cos(a) * d;
			double z = getZ() + Math.sin(a) * d;
			double y = getY() + random.nextInt(9) - 4;
			if (randomTeleport(x, y, z, false, (BlockState s) -> s.is(BlockTags.ENDERMAN_DOES_NOT_TELEPORT_TO))) {
				rajada(level);
				gargalhar();
				return true;
			}
		}
		return false;
	}

	/** Vanishes for good in a whirlwind (what he carries goes with him). */
	public void sumir() {
		if (level() instanceof ServerLevel level) {
			rajada(level);
		}
		discard();
	}

	// ------------------------------------------------------------------ getting hurt and caught

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurtServer(level, source, damage);
		}
		// Nothing hurts the Saci: he just laughs and whirls away.
		if (source.getEntity() instanceof Player) {
			fugir();
		}
		return false;
	}

	/** A peneira landed over him: he gives back what he stole, loses his cap and runs off. */
	public void capturar(ServerPlayer player) {
		ServerLevel level = (ServerLevel) level();
		ItemStack roubo = getMainHandItem();
		if (!roubo.isEmpty()) {
			setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			if (!player.getInventory().add(roubo)) {
				spawnAtLocation(level, roubo);
			}
		}
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.8, getZ(), 12, 0.4, 0.5, 0.4, 0.0);
		ItemStack garrafa = player.getOffhandItem();
		if (garrafa.is(Items.GLASS_BOTTLE)) {
			// The old trick: a bottle at hand, and the Saci goes in and gets corked.
			garrafa.consume(1, player);
			ItemStack preso = new ItemStack(ModItems.SACI_GARRAFA);
			if (!player.getInventory().add(preso)) {
				spawnAtLocation(level, preso);
			}
			level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.6, getZ(), 20, 0.3, 0.5, 0.3, 0.05);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.2F);
			player.sendSystemMessage(Component.translatable("folclore.saci.engarrafado"));
			discard();
			return;
		}
		spawnAtLocation(level, new ItemStack(ModItems.GORRO_SACI));
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 1.2F, 1.8F);
		player.sendSystemMessage(Component.translatable("folclore.saci.capturado"));
		sumir();
	}

	// ------------------------------------------------------------------ robbing

	/** Whether {@code player} is a fair target: in survival, and not wearing a Saci's cap. */
	private boolean alvo(Player player) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator()
				&& !player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GORRO_SACI);
	}

	private boolean roubar(ServerPlayer player) {
		Inventory inv = player.getInventory();
		List<Integer> cheios = new ArrayList<>();
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			if (!inv.getItem(i).isEmpty()) {
				cheios.add(i);
			}
		}
		if (cheios.isEmpty()) {
			return false;
		}
		int slot = cheios.get(random.nextInt(cheios.size()));
		ItemStack roubo = inv.getItem(slot);
		inv.setItem(slot, ItemStack.EMPTY);
		setItemSlot(EquipmentSlot.MAINHAND, roubo);
		setGuaranteedDrop(EquipmentSlot.MAINHAND);
		level().playSound(null, getX(), getY(), getZ(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.HOSTILE, 1.0F, 0.8F);
		gargalhar();
		player.sendOverlayMessage(Component.translatable("folclore.saci.roubou", roubo.getHoverName()));
		Aparicoes.avisarRoubo(player);
		return true;
	}

	// ------------------------------------------------------------------ pranks

	/** Blows out a nearby torch, or tangles the mane of a nearby horse. */
	private void pregarPeca(ServerLevel level) {
		BlockPos centro = blockPosition();
		for (BlockPos p : BlockPos.betweenClosed(centro.offset(-6, -2, -6), centro.offset(6, 3, 6))) {
			BlockState s = level.getBlockState(p);
			if (s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH)) {
				level.destroyBlock(p, true);
				level.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5, p.getY() + 0.7, p.getZ() + 0.5, 8, 0.1, 0.1, 0.1, 0.01);
				level.sendParticles(ParticleTypes.SMALL_GUST, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0, 0, 0, 0);
				level.playSound(null, p, SoundEvents.BREEZE_IDLE_GROUND, SoundSource.HOSTILE, 0.8F, 1.6F);
				comecarARir();
				return;
			}
		}
		for (AbstractHorse cavalo : level.getEntitiesOfClass(AbstractHorse.class, getBoundingBox().inflate(8))) {
			if (!cavalo.hasEffect(MobEffects.SLOWNESS)) {
				cavalo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 60, 1));
				level.sendParticles(ParticleTypes.ANGRY_VILLAGER, cavalo.getX(), cavalo.getY() + 1.6, cavalo.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
				cavalo.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
				for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, cavalo.getBoundingBox().inflate(16))) {
					p.sendOverlayMessage(Component.translatable("folclore.saci.crina"));
				}
				comecarARir();
				return;
			}
		}
	}

	private void comecarARir() {
		riso = 30 + random.nextInt(30);
		gargalhar();
	}

	// ------------------------------------------------------------------ tick

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		idade++;
		if (riso > 0) {
			riso--;
		}
		entityData.set(RINDO, riso > 0);
		if (esperaRoubo > 0) {
			esperaRoubo--;
		}

		Vec3 v = getDeltaMovement();
		boolean correndo = v.horizontalDistanceSqr() > 0.004;
		if (correndo || tickCount % 4 == 0) {
			redemoinho(level, correndo ? 6 : 2);
		}
		// One leg: he gets around by hopping.
		if (correndo && onGround() && random.nextInt(4) == 0) {
			getJumpControl().jump();
		}

		if (--pegadinha <= 0) {
			pegadinha = 200 + random.nextInt(400);
			pregarPeca(level);
		}
		// With the loot in hand he stops now and then to laugh at you: that is your chance.
		if (temRoubo() && riso == 0 && random.nextInt(140) == 0) {
			comecarARir();
		}
		if (!temRoubo() && idade > VIDA && level.getNearestPlayer(this, 24) == null) {
			sumir();
		}
	}

	/** Stands still, hand on the belly, laughing. */
	private class RirGoal extends Goal {
		RirGoal() {
			setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			return riso > 0;
		}

		@Override
		public void start() {
			getNavigation().stop();
		}
	}

	/** Runs to the nearest fair player and snatches a stack from their inventory. */
	private class RoubarGoal extends Goal {
		private @Nullable ServerPlayer alvo;
		private int repath;
		private int paciencia;

		RoubarGoal() {
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (temRoubo() || esperaRoubo > 0 || !(level() instanceof ServerLevel level)) {
				return false;
			}
			Player p = level.getNearestPlayer(getX(), getY(), getZ(), 20, e -> e instanceof Player pl && alvo(pl));
			alvo = p instanceof ServerPlayer sp ? sp : null;
			return alvo != null;
		}

		@Override
		public boolean canContinueToUse() {
			return alvo != null && !temRoubo() && alvo(alvo) && paciencia > 0 && distanceToSqr(alvo) < 32 * 32;
		}

		@Override
		public void start() {
			repath = 0;
			paciencia = 20 * 30;
		}

		@Override
		public void stop() {
			alvo = null;
			getNavigation().stop();
		}

		@Override
		public void tick() {
			if (alvo == null) {
				return;
			}
			paciencia--;
			getLookControl().setLookAt(alvo, 30.0F, 30.0F);
			if (--repath <= 0) {
				repath = 10;
				getNavigation().moveTo(alvo, 1.5);
			}
			double alcance = ALCANCE_ROUBO + alvo.getBbWidth() / 2;
			if (distanceToSqr(alvo) < alcance * alcance) {
				if (roubar(alvo)) {
					// Snatch and vanish: off he goes in a whirlwind.
					fugir();
				} else {
					esperaRoubo = 400;
				}
				alvo = null;
			} else if (paciencia <= 0) {
				esperaRoubo = 600;
			}
		}
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}
}
