package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Boitatá: the great fire serpent with huge shining eyes who protects the fields from whoever sets
 * them on fire. He crawls across the fields at night, puts out every fire around him (he "eats" the
 * flames), and goes after anyone who lights a fire near the forest, setting them ablaze in turn.
 * Leaves at dawn. Drops the Olho de Boitatá.
 */
public class BoitataEntity extends Assombracao {
	/** The player who set the forest on fire, if he came for someone. */
	private @Nullable UUID incendiario;

	public BoitataEntity(EntityType<? extends BoitataEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
				(alvo, level) -> incendiario != null && alvo.getUUID().equals(incendiario)));
	}

	/** Comes for {@code p}, who set fire to the forest. */
	public void perseguir(ServerPlayer p) {
		incendiario = p.getUUID();
		setTarget(p);
	}

	@Override
	protected boolean noturna() {
		return incendiario == null;
	}

	@Override
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.FLAME;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.BLAZE_AMBIENT;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.BLAZE_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.BLAZE_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean ok = super.doHurtTarget(level, target);
		if (ok) {
			target.igniteForSeconds(5.0F);
			level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
		}
		return ok;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		spawnAtLocation(level, new ItemStack(ModItems.OLHO_BOITATA));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) {
			return;
		}
		// A trail of fire along the body.
		if (tickCount % 2 == 0) {
			double a = getYRot() * Math.PI / 180;
			for (int i = 1; i <= 3; i++) {
				double d = i * 0.7;
				level.sendParticles(ParticleTypes.FLAME, getX() + Math.sin(a) * d, getY() + 0.4, getZ() - Math.cos(a) * d, 1, 0.1, 0.1, 0.1, 0.005);
			}
		}
		// He eats the flames: every fire nearby goes out.
		if (tickCount % 10 == 0) {
			BlockPos c = blockPosition();
			int apagados = 0;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-8, -5, -8), c.offset(8, 5, 8))) {
				if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
					level.removeBlock(p, false);
					level.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.01);
					apagados++;
				}
			}
			if (apagados > 0) {
				level.playSound(null, c, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0F, 0.7F);
			}
		}
		if (incendiario != null && getTarget() == null && idade() > 20 * 60) {
			// Done punishing: back to the fields.
			incendiario = null;
		}
	}

	/** Called when {@code p} lit a fire near the forest: tells them who is coming. */
	public static void avisar(ServerPlayer p) {
		p.sendSystemMessage(Component.translatable("folclore.boitata.chegou"));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.storeNullable("Incendiario", UUIDUtil.CODEC, incendiario);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		incendiario = input.read("Incendiario", UUIDUtil.CODEC).orElse(null);
	}
}
