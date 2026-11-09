package com.guilhermegoes.folclore.client;

import com.guilhermegoes.folclore.curupira.CurupiraEntity;
import com.guilhermegoes.folclore.curupira.Desmatamento;
import com.guilhermegoes.folclore.registry.ModBlocks;
import com.guilhermegoes.folclore.saci.SaciEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Automated in-game test run for development (only with FOLCLORE_ROTEIRO=1): opens the world
 * "teste", poses the Saci and the Curupira for photos (day and night), then checks the behaviour
 * on the integrated server (robbery, escaping blows, the peneira, the warning footprints, the
 * offering) and logs each check with [ROTEIRO]. Screenshots go to run/screenshots/folclore_*.png.
 */
public final class Roteiro {
	private record Passo(int espera, Runnable acao) {
	}

	private static final List<Passo> PASSOS = new ArrayList<>();
	private static int indice;
	private static int contador;
	private static boolean abriu;

	public static void init() {
		if (!"1".equals(System.getenv("FOLCLORE_ROTEIRO"))) {
			return;
		}
		montar();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (!abriu) {
				if (mc.gui.screen() instanceof TitleScreen && ++contador > 60) {
					abriu = true;
					contador = 0;
					log("abrindo o mundo teste");
					mc.createWorldOpenFlows().openWorld("teste", () -> log("abertura cancelada"));
				}
				return;
			}
			if (mc.player == null || mc.level == null || indice >= PASSOS.size()) {
				return;
			}
			if (mc.player.isDeadOrDying()) {
				log("jogador morreu: renascendo");
				mc.player.respawn();
				contador = 0;
				return;
			}
			if (mc.gui.screen() instanceof net.minecraft.client.gui.screens.PauseScreen) {
				mc.gui.setScreen(null);
			}
			if (++contador < PASSOS.get(indice).espera()) {
				return;
			}
			contador = 0;
			try {
				PASSOS.get(indice).acao().run();
			} catch (RuntimeException e) {
				log("ERRO no passo " + indice + ": " + e);
			}
			indice++;
		});
	}

	private static void log(String s) {
		System.out.println("[ROTEIRO] " + s);
	}

	private static Minecraft mc() {
		return Minecraft.getInstance();
	}

	private static void cmd(String... comandos) {
		var srv = mc().getSingleplayerServer();
		java.util.UUID id = mc().player.getUUID();
		srv.execute(() -> {
			var sp = srv.getPlayerList().getPlayer(id);
			for (String c : comandos) {
				var fonte = srv.createCommandSourceStack().withEntity(sp).withLevel(sp.level()).withPosition(sp.position())
						.withRotation(new net.minecraft.world.phys.Vec2(sp.getXRot(), sp.getYRot()));
				srv.getCommands().performPrefixedCommand(fonte, c);
			}
		});
	}

	/** Runs code on the server thread with the server-side player. */
	private static void servidor(Consumer<ServerPlayer> acao) {
		var srv = mc().getSingleplayerServer();
		java.util.UUID id = mc().player.getUUID();
		srv.execute(() -> {
			try {
				acao.accept(srv.getPlayerList().getPlayer(id));
			} catch (RuntimeException e) {
				log("ERRO no servidor: " + e);
				e.printStackTrace();
			}
		});
	}

	private static void foto(String nome) {
		Minecraft mc = mc();
		Screenshot.grab(mc.gameDirectory, "folclore_" + nome + ".png", mc.gameRenderer.mainRenderTarget(), 1, msg -> log("foto " + nome));
	}

	private static void passo(int espera, Runnable acao) {
		PASSOS.add(new Passo(espera, acao));
	}

	private static <T extends net.minecraft.world.entity.Entity> List<T> perto(ServerPlayer sp, Class<T> tipo, double r) {
		return sp.level().getEntitiesOfClass(tipo, sp.getBoundingBox().inflate(r));
	}

	private static void montar() {
		passo(10, () -> {
			mc().options.pauseOnLostFocus = false;
			cmd("gamemode creative");
		});
		passo(150, () -> {
			log("no mundo; preparando");
			mc().getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			cmd("time set 6000", "weather clear", "effect clear @s", "kill @e[type=folclore:saci]", "kill @e[type=folclore:curupira]",
					"gamerule spawn_mobs false", "kill @e[type=!minecraft:player]",
					"tp @s ~ ~ ~ 0 10");
		});

		if ("lado".equals(System.getenv("FOLCLORE_ROTEIRO_SO"))) {
			passo(5, () -> cmd("tp @s ~ ~ ~ 0 5", "fill ~-7 ~ ~-2 ~7 ~8 ~12 air", "fill ~-7 ~-1 ~-2 ~7 ~-1 ~12 grass_block", "kill @e[type=!minecraft:player]"));
			passo(20, () -> cmd("summon folclore:mula_sem_cabeca ~-2 ~ ~6 {NoAI:1b,Rotation:[90f,0f]}", "summon folclore:boto ~2.5 ~ ~5 {NoAI:1b,Rotation:[90f,0f]}"));
			passo(30, () -> foto("lado"));
			passo(5, () -> servidor(sp -> sp.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(12)).forEach(e -> log("pos " + e.getType() + " y=" + e.getY() + " jogador y=" + sp.getY() + " bb=" + e.getBoundingBox()))));
			passo(10, () -> mc().stop());
			return;
		}
		if ("ovos".equals(System.getenv("FOLCLORE_ROTEIRO_SO"))) {
			passo(20, Roteiro::ovos);
			passo(10, () -> mc().stop());
			return;
		}
		boolean so = "retratos".equals(System.getenv("FOLCLORE_ROTEIRO_SO"));
		if (so) {
			retratos();
			pegadas();
			passo(20, () -> {
				log("fim do roteiro");
				mc().stop();
			});
			return;
		}
		// ---- Portraits (NoAI so they stand still for the camera)
		passo(30, () -> cmd("summon folclore:saci ^-1 ^ ^3 {NoAI:1b,Rotation:[180f,0f]}",
				"summon folclore:curupira ^1.2 ^ ^3.5 {NoAI:1b,Rotation:[180f,0f]}"));
		passo(40, () -> foto("01_dupla_dia"));
		passo(5, () -> cmd("kill @e[type=folclore:curupira]", "kill @e[type=folclore:saci]",
				"summon folclore:saci ^ ^ ^2.5 {NoAI:1b,Rotation:[150f,0f],equipment:{mainhand:{id:\"minecraft:diamond\",count:3}}}"));
		passo(40, () -> foto("02_saci_com_diamante"));
		passo(5, () -> cmd("kill @e[type=folclore:saci]", "summon folclore:curupira ^ ^ ^3 {NoAI:1b,Rotation:[90f,0f]}"));
		passo(40, () -> foto("03_curupira_perfil_pes"));
		passo(5, () -> cmd("kill @e[type=folclore:curupira]", "summon folclore:curupira ^ ^ ^3 {NoAI:1b,Rotation:[0f,0f]}"));
		passo(40, () -> foto("04_curupira_costas"));
		passo(5, () -> cmd("time set 18000", "kill @e[type=folclore:curupira]", "summon folclore:curupira ^ ^ ^3 {NoAI:1b,Rotation:[180f,0f]}"));
		passo(60, () -> foto("05_curupira_noite"));
		passo(5, () -> cmd("kill @e[type=folclore:curupira]", "time set 6000", "item replace entity @s armor.head with folclore:gorro_saci"));
		passo(10, () -> mc().options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		passo(40, () -> foto("06_gorro_na_cabeca"));
		passo(5, () -> {
			mc().options.setCameraType(CameraType.FIRST_PERSON);
			cmd("item replace entity @s armor.head with minecraft:air", "give @s folclore:peneira", "give @s folclore:gorro_saci",
					"give @s folclore:saci_spawn_egg", "give @s folclore:curupira_spawn_egg");
		});
		passo(10, () -> mc().gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc().player)));
		passo(30, () -> foto("07_itens"));
		passo(5, () -> mc().gui.setScreen(null));

		retratos();

		// ---- Saci: robbery
		passo(10, () -> cmd("clear @s", "gamemode survival", "effect give @s resistance 600 4 true", "effect give @s saturation 600 1 true",
				"give @s minecraft:diamond 5", "give @s minecraft:bread 10", "folclore saci"));
		passo(40, () -> foto("08_saci_chegando"));
		passo(400, () -> servidor(sp -> {
			List<SaciEntity> s = perto(sp, SaciEntity.class, 128);
			int itens = 0;
			for (int i = 0; i < 36; i++) {
				itens += sp.getInventory().getItem(i).getCount();
			}
			boolean roubou = !s.isEmpty() && s.get(0).temRoubo();
			log("CHECK saci roubou: " + roubou + " (sacis " + s.size() + ", itens restantes " + itens
					+ (roubou ? ", levou " + s.get(0).getMainHandItem() : "") + ")");
		}));
		passo(5, () -> foto("09_depois_do_roubo"));
		// Blows do not hurt him: he whirls away.
		passo(5, () -> servidor(sp -> {
			List<SaciEntity> s = perto(sp, SaciEntity.class, 128);
			if (s.isEmpty()) {
				log("CHECK saci imune: sem saci");
				return;
			}
			SaciEntity saci = s.get(0);
			var antes = saci.position();
			boolean feriu = saci.hurtServer((ServerLevel) sp.level(), sp.damageSources().playerAttack(sp), 6.0F);
			log("CHECK saci imune: feriu=" + feriu + " vida=" + saci.getHealth() + " moveu=" + String.format("%.1f", antes.distanceTo(saci.position())));
		}));
		// The peneira catches him and gets the loot back.
		passo(20, () -> servidor(sp -> {
			List<SaciEntity> s = perto(sp, SaciEntity.class, 128);
			if (s.isEmpty()) {
				log("CHECK peneira: sem saci");
				return;
			}
			SaciEntity saci = s.get(0);
			sp.teleportTo(saci.getX(), saci.getY(), saci.getZ() - 3);
			saci.capturar(sp);
			int diamantes = 0;
			for (int i = 0; i < 36; i++) {
				if (sp.getInventory().getItem(i).is(Items.DIAMOND) || sp.getInventory().getItem(i).is(Items.BREAD)) {
					diamantes += sp.getInventory().getItem(i).getCount();
				}
			}
			log("CHECK peneira: saci removido=" + saci.isRemoved() + ", itens de volta=" + diamantes + "/15");
		}));
		passo(20, () -> servidor(sp -> log("CHECK gorro caiu: "
				+ !perto(sp, net.minecraft.world.entity.item.ItemEntity.class, 8).stream()
						.filter(e -> e.getItem().is(com.guilhermegoes.folclore.registry.ModItems.GORRO_SACI)).toList().isEmpty())));
		passo(5, () -> foto("10_saci_capturado"));
		// A bottle in the offhand: the Saci gets corked, then does a favour.
		passo(5, () -> cmd("summon folclore:saci ^ ^ ^3", "item replace entity @s weapon.offhand with minecraft:glass_bottle",
				"summon minecraft:item ^4 ^1 ^4 {Item:{id:\"minecraft:apple\",count:7}}"));
		passo(20, () -> servidor(sp -> {
			List<SaciEntity> s = perto(sp, SaciEntity.class, 16);
			if (s.isEmpty()) {
				log("CHECK garrafa: sem saci");
				return;
			}
			s.get(0).capturar(sp);
			boolean tem = false;
			for (int i = 0; i < 36; i++) {
				tem |= sp.getInventory().getItem(i).is(com.guilhermegoes.folclore.registry.ModItems.SACI_GARRAFA);
			}
			log("CHECK garrafa: engarrafou=" + tem + " garrafa vazia gasta=" + sp.getOffhandItem().isEmpty());
		}));
		passo(5, () -> cmd("clear @s folclore:gorro_saci", "item replace entity @s weapon.mainhand with folclore:saci_garrafa"));
		passo(5, () -> servidor(sp -> {
			sp.getMainHandItem().use(sp.level(), sp, InteractionHand.MAIN_HAND);
			long longe = perto(sp, net.minecraft.world.entity.item.ItemEntity.class, 16).stream().filter(e -> e.distanceTo(sp) > 2).count();
			log("CHECK favor da garrafa: itens ainda longe=" + longe + " usos=" + sp.getMainHandItem().getDamageValue());
		}));
		passo(10, () -> foto("10b_favor_da_garrafa"));
		// With the cap on, a Saci leaves you alone.
		passo(5, () -> cmd("item replace entity @s armor.head with folclore:gorro_saci", "folclore saci"));
		passo(300, () -> servidor(sp -> {
			List<SaciEntity> s = perto(sp, SaciEntity.class, 128);
			log("CHECK gorro protege: " + (s.isEmpty() ? "sem saci" : "roubou=" + s.get(0).temRoubo()));
		}));
		passo(5, () -> cmd("kill @e[type=folclore:saci]", "item replace entity @s armor.head with minecraft:air"));

		// ---- Pranks: torches near a Saci
		passo(5, () -> servidor(sp -> {
			BlockPos p = sp.blockPosition().relative(sp.getDirection(), 4);
			sp.level().setBlock(p, net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState(), 3);
			log("tocha em " + p);
		}));

		// ---- Curupira: warning, arrival, offering
		pegadas();
		passo(5, () -> cmd("tp @s ~ ~ ~ ~ 10", "folclore curupira"));
		passo(160, () -> {
			foto("12_curupira_cacando");
			servidor(sp -> {
				List<CurupiraEntity> c = perto(sp, CurupiraEntity.class, 48);
				log("CHECK curupira chegou: " + !c.isEmpty() + (c.isEmpty() ? "" : " alvo=" + (c.get(0).getTarget() == sp)
						+ " dist=" + String.format("%.1f", c.get(0).distanceTo(sp))) + " nausea=" + sp.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA));
			});
		});
		passo(5, () -> cmd("tp @s ~ ~ ~ ~ 70"));
		passo(10, () -> foto("13_rastro_do_curupira"));
		passo(5, () -> servidor(sp -> {
			List<CurupiraEntity> c = perto(sp, CurupiraEntity.class, 48);
			if (c.isEmpty()) {
				log("CHECK oferenda: sem curupira");
				return;
			}
			sp.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.HONEY_BOTTLE));
			var r = sp.interactOn(c.get(0), InteractionHand.MAIN_HAND, c.get(0).position());
			log("CHECK oferenda: resultado=" + r + " desmatamento=" + Desmatamento.pontos(sp) + " calmo=" + c.get(0).calmo());
		}));
		passo(10, () -> cmd("tp @s ~ ~ ~ ~ 20"));
		passo(10, () -> foto("14_curupira_calmo"));
		passo(80, () -> servidor(sp -> log("CHECK curupira partiu: " + perto(sp, CurupiraEntity.class, 48).isEmpty())));
		passo(20, () -> {
			log("fim do roteiro");
			mc().stop();
		});
	}

	/** Portraits of the other legends, by day and by night, with no smoke or dropped items in the way. */
	private static void retratos() {
		// No crosshair, hotbar or hand in the gallery shots (like pressing F1).
		passo(1, () -> { if (!mc().gui.hud.isHidden()) mc().gui.hud.toggle(); });
		// A flat stage in front of the camera, so nobody is buried in a slope.
		passo(5, () -> cmd("tp @s ~ ~ ~ 0 10", "fill ~-7 ~ ~-2 ~7 ~8 ~12 air", "fill ~-7 ~-1 ~-2 ~7 ~-1 ~12 grass_block", "fill ~-7 ~-2 ~-2 ~7 ~-2 ~12 dirt"));
		String[][] retratos = {
				{"saci", "~ ~ ~2.5", "{NoAI:1b,Rotation:[180f,0f],equipment:{mainhand:{id:\"minecraft:diamond\",count:3}}}"},
				{"curupira", "~ ~ ~3", "{NoAI:1b,Rotation:[150f,0f]}"},
				{"boitata", "~ ~1.5 ~4", "{NoAI:1b,Rotation:[150f,0f]}"},
				{"mula_sem_cabeca", "~ ~ ~4", "{NoAI:1b,Rotation:[150f,0f]}"},
				{"iara", "~ ~ ~3", "{NoAI:1b,Rotation:[180f,0f]}"},
				{"boto", "~ ~ ~3", "{NoAI:1b,Rotation:[140f,0f]}"},
				{"boto", "~ ~ ~3", "{NoAI:1b,Rotation:[180f,0f],Homem:1b}"},
				{"cuca", "~ ~ ~3", "{NoAI:1b,Rotation:[180f,0f]}"},
				{"lobisomem", "~ ~ ~3.5", "{NoAI:1b,Rotation:[180f,0f]}"},
				{"mapinguari", "~ ~ ~5", "{NoAI:1b,Rotation:[180f,0f]}"},
		};
		for (String hora : new String[] {"6000", "18000"}) {
			for (int i = 0; i < retratos.length; i++) {
				String[] r = retratos[i];
				String nome = String.format("r_%s_%02d_%s", hora.equals("6000") ? "dia" : "noite", i, r[0]);
				passo(5, () -> cmd("time set " + hora, "tp @e[type=!minecraft:player] ~ -200 ~", "kill @e[type=item]"));
				passo(30, () -> cmd("kill @e[type=item]", "summon folclore:" + r[0] + " " + r[1] + " " + r[2]));
				passo(30, () -> foto(nome));
			}
		}
		passo(1, () -> { if (mc().gui.hud.isHidden()) mc().gui.hud.toggle(); });
		passo(5, () -> cmd("kill @e[type=!minecraft:player]", "time set 6000"));

	}

	/** The Curupira's warning: whistle and a trail of backward footprints (needs survival mode). */
	private static void pegadas() {
		passo(5, () -> cmd("gamemode survival", "effect give @s resistance 600 4 true"));
		passo(5, () -> cmd("kill @e[type=item]", "folclore desmatamento 40"));
		passo(420, () -> servidor(sp -> {
			int pegadas = 0;
			BlockPos c = sp.blockPosition();
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-12, -4, -12), c.offset(12, 4, 12))) {
				if (sp.level().getBlockState(p).is(ModBlocks.PEGADA)) {
					pegadas++;
				}
			}
			log("CHECK aviso do curupira: pegadas=" + pegadas + " pontos=" + com.guilhermegoes.folclore.curupira.Desmatamento.pontos(sp)
					+ " modo=" + sp.gameMode.getGameModeForPlayer() + " tempo=" + sp.level().getGameTime());
		}));
		passo(5, () -> cmd("tp @s ~ ~ ~ ~ 50"));
		passo(10, () -> foto("11_pegadas"));
	}

	/** Checks that every folklore spawn egg is in the vanilla Spawn Eggs creative tab and in ours. */
	private static void ovos() {
		var mc = mc();
		net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(), true, mc.level.registryAccess());
		var ovos = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS);
		var nossa = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getValue(com.guilhermegoes.folclore.Folclore.id("folclore"));
		int naAbaOvos = 0, naNossa = 0;
		for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
			var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
			if (id.getNamespace().equals("folclore") && id.getPath().endsWith("_spawn_egg")) {
				var pilha = new net.minecraft.world.item.ItemStack(item);
				boolean a = ovos.getDisplayItems().stream().anyMatch(x -> x.is(item)), b = nossa != null && nossa.getDisplayItems().stream().anyMatch(x -> x.is(item));
				naAbaOvos += a ? 1 : 0;
				naNossa += b ? 1 : 0;
				log("ovo " + id + " abaOvos=" + a + " abaFolclore=" + b);
			}
		}
		log("CHECK ovos: aba Ovos Geradores " + naAbaOvos + "/9, aba Folclore " + naNossa + "/9");
	}

	private Roteiro() {
	}
}
