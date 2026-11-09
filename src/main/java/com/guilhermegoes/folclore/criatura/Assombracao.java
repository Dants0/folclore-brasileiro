package com.guilhermegoes.folclore.criatura;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Shared behaviour of the folklore creatures that come and go: they vanish in a puff (each with
 * its own particle), creatures of the night leave when the sun comes up, and every one of them
 * leaves after a while with nobody around, so the world never fills up with legends.
 */
public abstract class Assombracao extends PathfinderMob {
	/** Ticks with no player within {@link #RAIO_SOLIDAO} blocks before leaving. */
	private static final int SOLIDAO = 20 * 60 * 2;
	private static final double RAIO_SOLIDAO = 48;

	/** Called by command: stays regardless of the time of day (for recording). */
	private boolean chamada;
	private int sozinho;
	private int idade;

	protected Assombracao(EntityType<? extends Assombracao> type, Level level) {
		super(type, level);
	}

	/** Whether this creature only walks at night (it leaves at sunrise). */
	protected abstract boolean noturna();

	/** The particle of its arrival and departure. */
	protected ParticleOptions particulaSumico() {
		return ParticleTypes.LARGE_SMOKE;
	}

	public void chamar() {
		chamada = true;
	}

	@Override
	public net.minecraft.world.entity.@Nullable SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
			net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason reason,
			net.minecraft.world.entity.@Nullable SpawnGroupData data) {
		// Spawn eggs and /summon: the creature stays whatever the time of day.
		if (reason == net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE || reason == net.minecraft.world.entity.EntitySpawnReason.COMMAND) {
			chamada = true;
		}
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	public int idade() {
		return idade;
	}

	/** Night in the Overworld: from dusk to dawn. */
	public static boolean noite(Level level) {
		long t = level.getOverworldClockTime() % 24000;
		return t >= 12500 && t <= 23400;
	}

	public void aparecer(ServerLevel level) {
		level.sendParticles(particulaSumico(), getX(), getY() + getBbHeight() / 2, getZ(), 30, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.03);
	}

	/** Leaves the world in a puff of its own particle. */
	public void sumir() {
		if (level() instanceof ServerLevel level) {
			aparecer(level);
		}
		discard();
	}

	protected @Nullable ServerPlayer jogadorPerto(double raio) {
		return level() instanceof ServerLevel level && level.getNearestPlayer(this, raio) instanceof ServerPlayer p ? p : null;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		idade++;
		if (tickCount % 20 != 0) {
			return;
		}
		if (noturna() && !chamada && !noite(level) && !hasCustomName()) {
			sumir();
			return;
		}
		sozinho = level.getNearestPlayer(this, RAIO_SOLIDAO) == null ? sozinho + 20 : 0;
		if (sozinho > SOLIDAO && !hasCustomName()) {
			sumir();
		}
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !hasCustomName();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Idade", idade);
		output.putBoolean("Chamada", chamada);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		idade = input.getIntOr("Idade", 0);
		chamada = input.getBooleanOr("Chamada", false);
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}
}
