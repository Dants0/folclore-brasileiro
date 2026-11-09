package com.guilhermegoes.folclore.saci;

import com.guilhermegoes.folclore.FolcloreConfig;
import com.guilhermegoes.folclore.registry.ModEntities;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * When the Saci shows up. Once a minute, each survival player standing under the open sky in the
 * Overworld has a small chance that a Saci comes whirling in from 14 to 22 blocks away, during
 * the day and at dusk. Only one Saci at a time pesters the same area.
 */
public final class Aparicoes {
	private static final int INTERVALO = 20 * 60;
	/** Players who already got the "only a peneira catches him" hint this session. */
	private static final Set<UUID> AVISADOS = new HashSet<>();

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerLevel level = server.overworld();
			if (level.getGameTime() % INTERVALO != 0 || !FolcloreConfig.get().saciAtivo) {
				return;
			}
			RandomSource r = level.getRandom();
			for (ServerPlayer p : level.players()) {
				if (p.isCreative() || p.isSpectator() || r.nextInt(FolcloreConfig.get().saciChance) != 0) {
					continue;
				}
				// He travels in the warm hours: from morning to dusk.
				long hora = level.getOverworldClockTime() % 24000;
				if (hora > 13000 || !level.canSeeSky(p.blockPosition())) {
					continue;
				}
				if (!level.getEntitiesOfClass(SaciEntity.class, p.getBoundingBox().inflate(64)).isEmpty()) {
					continue;
				}
				chegar(level, p.blockPosition(), r);
			}
		});
	}

	/** Brings a Saci in a whirlwind to a surface spot 14 to 22 blocks from {@code perto}. */
	public static @Nullable SaciEntity chegar(ServerLevel level, BlockPos perto, RandomSource r) {
		for (int i = 0; i < 10; i++) {
			double a = r.nextDouble() * Math.PI * 2;
			int d = 14 + r.nextInt(9);
			int x = perto.getX() + (int) Math.round(Math.cos(a) * d);
			int z = perto.getZ() + (int) Math.round(Math.sin(a) * d);
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			if (Math.abs(y - perto.getY()) > 12 || !level.getFluidState(pos.below()).isEmpty()) {
				continue;
			}
			SaciEntity saci = ModEntities.SACI.spawn(level, pos, EntitySpawnReason.EVENT);
			if (saci != null) {
				saci.rajada(level);
				level.playSound(null, pos, SoundEvents.BREEZE_IDLE_GROUND, SoundSource.HOSTILE, 1.5F, 0.8F);
				for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, saci.getBoundingBox().inflate(32))) {
					p.sendOverlayMessage(Component.translatable("folclore.saci.chegou"));
				}
				return saci;
			}
		}
		return null;
	}

	/** After the first robbery, tells the player how the Saci can be caught. */
	static void avisarRoubo(ServerPlayer player) {
		if (AVISADOS.add(player.getUUID())) {
			player.sendSystemMessage(Component.translatable("folclore.saci.dica"));
		}
	}

	private Aparicoes() {
	}
}
