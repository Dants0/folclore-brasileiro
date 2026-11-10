package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.FolcloreConfig;
import com.guilhermegoes.folclore.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * When and where each legend appears. Once a minute, for every survival player in the Overworld,
 * each creature whose conditions are met (the ones from the tales) has a small chance to come:
 * <ul>
 * <li>Boitatá: at night, in forests and along rivers (on land or rising from the water); and always
 * when someone lights a fire near the forest.</li>
 * <li>Mula sem Cabeça: at night, in the open; twice as likely on Thursday-to-Friday nights.</li>
 * <li>Iara: at night, near water; she rises in the river.</li>
 * <li>Boto: near water, day or night.</li>
 * <li>Cuca: at night, for whoever has gone three nights without sleeping.</li>
 * <li>Lobisomem: on full-moon nights, in the open.</li>
 * <li>Mapinguari: by day, in the jungle.</li>
 * </ul>
 * At most one creature arrives per player per minute, and never a second one of the same kind
 * within 64 blocks.
 */
public final class Assombracoes {
	private static final int INTERVALO = 20 * 60;

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerLevel level = server.overworld();
			if (level.getGameTime() % INTERVALO != 30) {
				return;
			}
			for (ServerPlayer p : level.players()) {
				if (!p.isCreative() && !p.isSpectator()) {
					sortear(level, p);
				}
			}
		});
		// Setting fire near the forest calls the Boitatá.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (player instanceof ServerPlayer sp && level instanceof ServerLevel server && level.dimension() == Level.OVERWORLD
					&& FolcloreConfig.get().boitataContraQueimada && !sp.isCreative()
					&& (player.getItemInHand(hand).is(Items.FLINT_AND_STEEL) || player.getItemInHand(hand).is(Items.FIRE_CHARGE))
					&& pertoDaMata(server, hit.getBlockPos()) && !existe(server, sp, ModEntities.BOITATA)) {
				BoitataEntity b = aparecer(server, sp, ModEntities.BOITATA, false);
				if (b != null) {
					b.perseguir(sp);
					BoitataEntity.avisar(sp);
				}
			}
			return InteractionResult.PASS;
		});
	}

	private static void sortear(ServerLevel level, ServerPlayer p) {
		FolcloreConfig c = FolcloreConfig.get();
		RandomSource r = level.getRandom();
		boolean noite = Assombracao.noite(level);
		boolean ceu = level.canSeeSky(p.blockPosition());
		var bioma = level.getBiome(p.blockPosition());

		if (noite && CucaEntity.insone(p) && sorte(r, c.cucaChance) && !existe(level, p, ModEntities.CUCA)) {
			if (aparecer(level, p, ModEntities.CUCA, false) != null) {
				p.sendSystemMessage(Component.translatable("folclore.cuca.chegou"));
				return;
			}
		}
		if (noite && ceu && luaCheia(level, p) && sorte(r, c.lobisomemChance) && !existe(level, p, ModEntities.LOBISOMEM)) {
			if (aparecer(level, p, ModEntities.LOBISOMEM, false) != null) {
				p.sendSystemMessage(Component.translatable("folclore.lobisomem.chegou"));
				return;
			}
		}
		if (noite && ceu && sorte(r, sextaFeira(level) ? Math.max(1, c.mulaChance / 2) : c.mulaChance)
				&& !existe(level, p, ModEntities.MULA_SEM_CABECA)) {
			if (aparecer(level, p, ModEntities.MULA_SEM_CABECA, false) != null) {
				p.sendSystemMessage(Component.translatable("folclore.mula.chegou"));
				return;
			}
		}
		if (noite && mataOuRio(level, p) && sorte(r, c.boitataChance) && !existe(level, p, ModEntities.BOITATA)) {
			// He lives in the forests and along the rivers: half the time he rises from the water.
			BoitataEntity b = r.nextBoolean() ? naAgua(level, p, ModEntities.BOITATA) : null;
			if (b == null) {
				b = aparecer(level, p, ModEntities.BOITATA, false);
			}
			if (b != null) {
				p.sendOverlayMessage(Component.translatable("folclore.boitata.visto"));
				return;
			}
		}
		if (noite && sorte(r, c.iaraChance) && !existe(level, p, ModEntities.IARA)) {
			if (naAgua(level, p, ModEntities.IARA) != null) {
				p.sendOverlayMessage(Component.translatable("folclore.iara.chegou"));
				return;
			}
		}
		if (sorte(r, c.botoChance) && !existe(level, p, ModEntities.BOTO)) {
			if (naAgua(level, p, ModEntities.BOTO) != null) {
				return;
			}
		}
		if (!noite && bioma.is(BiomeTags.IS_JUNGLE) && sorte(r, c.mapinguariChance) && !existe(level, p, ModEntities.MAPINGUARI)) {
			if (aparecer(level, p, ModEntities.MAPINGUARI, false) != null) {
				p.sendSystemMessage(Component.translatable("folclore.mapinguari.chegou"));
			}
		}
	}

	/** Forests, jungles, taigas, rivers and swamps: where the Boitatá lives. */
	public static boolean mataOuRio(ServerLevel level, ServerPlayer p) {
		var b = level.getBiome(p.blockPosition());
		return b.is(BiomeTags.IS_FOREST) || b.is(BiomeTags.IS_JUNGLE) || b.is(BiomeTags.IS_TAIGA) || b.is(BiomeTags.IS_RIVER)
				|| b.is(net.minecraft.world.level.biome.Biomes.SWAMP) || b.is(net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP);
	}

	private static boolean sorte(RandomSource r, int chance) {
		return chance > 0 && r.nextInt(chance) == 0;
	}

	public static boolean luaCheia(ServerLevel level, ServerPlayer p) {
		return level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, p.position()) == MoonPhase.FULL_MOON;
	}

	/** The night from Thursday to Friday: the fifth night of every week (day 0 being a Sunday). */
	public static boolean sextaFeira(ServerLevel level) {
		return (level.getOverworldClockTime() / 24000) % 7 == 4;
	}

	private static boolean existe(ServerLevel level, ServerPlayer p, EntityType<?> tipo) {
		return !level.getEntities(tipo, p.getBoundingBox().inflate(64), e -> true).isEmpty();
	}

	/** Logs or leaves within 4 blocks: the fire would reach the forest. */
	private static boolean pertoDaMata(ServerLevel level, BlockPos pos) {
		for (BlockPos q : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 4, 4))) {
			var s = level.getBlockState(q);
			if (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Brings a creature to a dry surface spot 16 to 26 blocks from {@code p} (a few blocks up in
	 * the air for flyers).
	 */
	public static <T extends Assombracao> @Nullable T aparecer(ServerLevel level, ServerPlayer p, EntityType<T> tipo, boolean voa) {
		RandomSource r = level.getRandom();
		BlockPos perto = p.blockPosition();
		for (int i = 0; i < 12; i++) {
			double a = r.nextDouble() * Math.PI * 2;
			int d = 16 + r.nextInt(11);
			int x = perto.getX() + (int) Math.round(Math.cos(a) * d);
			int z = perto.getZ() + (int) Math.round(Math.sin(a) * d);
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y + (voa ? 4 : 0), z);
			if (Math.abs(y - perto.getY()) > 14 || !level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) {
				continue;
			}
			if (!level.noCollision(tipo.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
				continue;
			}
			T e = tipo.spawn(level, pos, EntitySpawnReason.EVENT);
			if (e != null) {
				e.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
				e.aparecer(level);
				return e;
			}
		}
		return null;
	}

	/** Brings a water creature into water (two blocks deep) within 6 to 20 blocks of {@code p}. */
	public static <T extends Assombracao> @Nullable T naAgua(ServerLevel level, ServerPlayer p, EntityType<T> tipo) {
		RandomSource r = level.getRandom();
		BlockPos perto = p.blockPosition();
		for (int i = 0; i < 40; i++) {
			int x = perto.getX() + r.nextInt(41) - 20;
			int z = perto.getZ() + r.nextInt(41) - 20;
			if (Vec3.atCenterOf(perto).distanceTo(new Vec3(x, perto.getY(), z)) < 6) {
				continue;
			}
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
			BlockPos pos = new BlockPos(x, y, z);
			if (Math.abs(y - perto.getY()) > 10 || !level.getFluidState(pos).is(FluidTags.WATER)
					|| !level.getFluidState(pos.below()).is(FluidTags.WATER)) {
				continue;
			}
			T e = tipo.spawn(level, pos.below(), EntitySpawnReason.EVENT);
			if (e != null) {
				e.aparecer(level);
				return e;
			}
		}
		return null;
	}

	private Assombracoes() {
	}
}
