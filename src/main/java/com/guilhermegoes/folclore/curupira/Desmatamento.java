package com.guilhermegoes.folclore.curupira;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.FolcloreConfig;
import com.guilhermegoes.folclore.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * How much harm each player has done to the forest. Every log broken adds a point, every wild
 * animal killed adds two, every sapling planted takes three away, and the forest forgives one
 * point every 20 seconds on its own.
 *
 * <p>At {@link #aviso()} points the player starts hearing a whistle in the woods and finds backward
 * footprints around them. At {@link #furia()} points the Curupira himself comes for them.
 */
public final class Desmatamento {
	private static final int TETO = 120;
	private static final int PERIODO = 20 * 20;
	/** Ticks between warnings for the same player. */
	private static final int ENTRE_AVISOS = 20 * 90;

	/** Saved with the player and kept through death: the forest does not forget because you died. */
	private static final AttachmentType<Integer> PONTOS = AttachmentRegistry.create(Folclore.id("desmatamento"),
			b -> b.persistent(Codec.INT).copyOnDeath());
	private static final Map<UUID, Long> ULTIMO_AVISO = new HashMap<>();

	/** Warning threshold (whistle and footprints). */
	public static int aviso() {
		return FolcloreConfig.get().curupiraAviso;
	}

	/** The Curupira comes in person. */
	public static int furia() {
		return Math.max(aviso() + 1, FolcloreConfig.get().curupiraFuria);
	}

	public static int pontos(ServerPlayer p) {
		return p.getAttachedOrElse(PONTOS, 0);
	}

	public static void definir(ServerPlayer p, int valor) {
		int v = Math.max(0, Math.min(TETO, valor));
		if (v == 0) {
			p.removeAttached(PONTOS);
		} else {
			p.setAttached(PONTOS, v);
		}
	}

	public static void somar(ServerPlayer p, int delta) {
		definir(p, pontos(p) + delta);
	}

	/** The Curupira accepted an offering: the forest forgives this player completely. */
	public static void perdoar(ServerPlayer p) {
		definir(p, 0);
		ULTIMO_AVISO.remove(p.getUUID());
	}

	public static void init() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer sp && level.dimension() == Level.OVERWORLD && state.is(BlockTags.LOGS)) {
				somar(sp, 1);
			}
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof Animal a && source.getEntity() instanceof ServerPlayer sp
					&& entity.level().dimension() == Level.OVERWORLD && selvagem(a)) {
				somar(sp, 2);
			}
		});
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (player instanceof ServerPlayer sp && player.getItemInHand(hand).is(ItemTags.SAPLINGS)) {
				somar(sp, -3);
			}
			return InteractionResult.PASS;
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerLevel level = server.overworld();
			if (level.getGameTime() % PERIODO != 0) {
				return;
			}
			if (!FolcloreConfig.get().curupiraAtivo) {
				return;
			}
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				if (pontos(p) > 0) {
					somar(p, -1);
				}
				if (p.level() == level && !p.isCreative() && !p.isSpectator()) {
					avaliar(level, p);
				}
			}
		});
	}

	/** Animals not raised by anyone: not leashed, not named, not babies. */
	private static boolean selvagem(Animal a) {
		return !a.isLeashed() && !a.hasCustomName() && !a.isBaby();
	}

	private static void avaliar(ServerLevel level, ServerPlayer p) {
		int pts = pontos(p);
		if (pts < aviso()) {
			return;
		}
		boolean temCurupira = !level.getEntitiesOfClass(CurupiraEntity.class, p.getBoundingBox().inflate(48)).isEmpty();
		if (pts >= furia() && !temCurupira) {
			CurupiraEntity c = chegar(level, p, level.getRandom());
			if (c != null) {
				p.sendSystemMessage(Component.translatable("folclore.curupira.chegou"));
			}
			return;
		}
		long agora = level.getGameTime();
		if (!temCurupira && agora - ULTIMO_AVISO.getOrDefault(p.getUUID(), -ENTRE_AVISOS * 2L) >= ENTRE_AVISOS) {
			ULTIMO_AVISO.put(p.getUUID(), agora);
			assobiar(level, p);
			rastro(level, p, level.getRandom());
			p.sendOverlayMessage(Component.translatable("folclore.curupira.aviso"));
		}
	}

	/** The Curupira's whistle, from somewhere in the woods around {@code p}. */
	public static void assobiar(ServerLevel level, ServerPlayer p) {
		RandomSource r = level.getRandom();
		double a = r.nextDouble() * Math.PI * 2;
		double x = p.getX() + Math.cos(a) * 10, z = p.getZ() + Math.sin(a) * 10;
		// A rising two-note whistle.
		level.playSound(null, x, p.getY() + 1, z, SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.HOSTILE, 2.0F, 1.5F);
		level.playSound(null, x, p.getY() + 1, z, SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.HOSTILE, 2.0F, 2.0F);
	}

	/** A short trail of backward footprints passing near {@code p}. */
	private static void rastro(ServerLevel level, ServerPlayer p, RandomSource r) {
		Direction ida = Direction.Plane.HORIZONTAL.getRandomDirection(r);
		Direction lado = ida.getClockWise();
		BlockPos inicio = p.blockPosition().relative(lado, 3 + r.nextInt(3)).relative(ida.getOpposite(), 5);
		for (int i = 0; i < 10; i++) {
			BlockPos alvo = inicio.relative(ida, i);
			// The ground near the player's own height (not the top of the world: there may be a roof or a cave).
			for (int dy = 3; dy >= -4; dy--) {
				// Walking towards "ida", the toes point back the other way.
				if (PegadaBlock.marcar(level, alvo.above(dy), ida.getOpposite())) {
					break;
				}
			}
		}
	}

	/** Brings the Curupira to a surface spot 16 to 24 blocks from {@code p}, angry at them. */
	public static @Nullable CurupiraEntity chegar(ServerLevel level, ServerPlayer p, RandomSource r) {
		BlockPos perto = p.blockPosition();
		for (int i = 0; i < 12; i++) {
			double a = r.nextDouble() * Math.PI * 2;
			int d = 16 + r.nextInt(9);
			int x = perto.getX() + (int) Math.round(Math.cos(a) * d);
			int z = perto.getZ() + (int) Math.round(Math.sin(a) * d);
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			if (Math.abs(y - perto.getY()) > 12 || !level.getFluidState(pos.below()).isEmpty()) {
				continue;
			}
			CurupiraEntity c = ModEntities.CURUPIRA.spawn(level, pos, EntitySpawnReason.EVENT);
			if (c != null) {
				c.setTarget(p);
				assobiar(level, p);
				return c;
			}
		}
		return null;
	}

	private Desmatamento() {
	}
}
