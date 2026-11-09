package com.guilhermegoes.folclore;

import com.guilhermegoes.folclore.curupira.CurupiraEntity;
import com.guilhermegoes.folclore.curupira.Desmatamento;
import com.guilhermegoes.folclore.saci.Aparicoes;
import com.guilhermegoes.folclore.saci.SaciEntity;
import com.guilhermegoes.folclore.criatura.Assombracao;
import com.guilhermegoes.folclore.criatura.Assombracoes;
import com.guilhermegoes.folclore.registry.ModEntities;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.EntityType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /folclore saci|curupira|desmatamento [valor]}: calls a creature or sets the player's
 * forest score, for testing and for recording videos.
 */
public final class ComandoFolclore {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, ambiente) -> {
			LiteralArgumentBuilder<CommandSourceStack> raiz = Commands.literal("folclore")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
			criatura(raiz, "boitata", ModEntities.BOITATA, true, false);
			criatura(raiz, "mula", ModEntities.MULA_SEM_CABECA, false, false);
			criatura(raiz, "iara", ModEntities.IARA, false, true);
			criatura(raiz, "boto", ModEntities.BOTO, false, true);
			criatura(raiz, "cuca", ModEntities.CUCA, false, false);
			criatura(raiz, "lobisomem", ModEntities.LOBISOMEM, false, false);
			criatura(raiz, "mapinguari", ModEntities.MAPINGUARI, false, false);
			dispatcher.register(raiz
						.then(Commands.literal("saci").executes(c -> {
							ServerPlayer p = c.getSource().getPlayerOrException();
							SaciEntity s = Aparicoes.chegar((ServerLevel) p.level(), p.blockPosition(), p.getRandom());
							c.getSource().sendSuccess(() -> Component.literal(s != null ? "O Saci chegou." : "Não achei lugar para o Saci."), false);
							return s != null ? 1 : 0;
						}))
						.then(Commands.literal("curupira").executes(c -> {
							ServerPlayer p = c.getSource().getPlayerOrException();
							if (Desmatamento.pontos(p) < Desmatamento.furia()) {
								Desmatamento.definir(p, Desmatamento.furia());
							}
							CurupiraEntity cu = Desmatamento.chegar((ServerLevel) p.level(), p, p.getRandom());
							c.getSource().sendSuccess(() -> Component.literal(cu != null ? "O Curupira chegou." : "Não achei lugar para o Curupira."), false);
							return cu != null ? 1 : 0;
						}))
						.then(Commands.literal("desmatamento")
								.executes(c -> {
									ServerPlayer p = c.getSource().getPlayerOrException();
									int v = Desmatamento.pontos(p);
									c.getSource().sendSuccess(() -> Component.literal("Desmatamento: " + v
											+ " (aviso " + Desmatamento.aviso() + ", fúria " + Desmatamento.furia() + ")"), false);
									return v;
								})
								.then(Commands.argument("valor", IntegerArgumentType.integer(0, 120)).executes(c -> {
									ServerPlayer p = c.getSource().getPlayerOrException();
									int v = IntegerArgumentType.getInteger(c, "valor");
									Desmatamento.definir(p, v);
									c.getSource().sendSuccess(() -> Component.literal("Desmatamento = " + v), false);
									return v;
								}))));
		});
	}

	/** {@code /folclore <nome>}: brings that creature near the player (into water for water creatures). */
	private static void criatura(LiteralArgumentBuilder<CommandSourceStack> raiz, String nome,
			EntityType<? extends Assombracao> tipo, boolean voa, boolean agua) {
		raiz.then(Commands.literal(nome).executes(c -> {
			ServerPlayer p = c.getSource().getPlayerOrException();
			ServerLevel level = (ServerLevel) p.level();
			Assombracao e = agua ? Assombracoes.naAgua(level, p, tipo) : null;
			if (e == null) {
				e = Assombracoes.aparecer(level, p, tipo, voa);
			}
			if (e != null) {
				e.chamar();
			}
			boolean ok = e != null;
			c.getSource().sendSuccess(() -> Component.literal(ok ? "Chegou: " + nome : "Não achei lugar para: " + nome), false);
			return ok ? 1 : 0;
		}));
	}

	private ComandoFolclore() {
	}
}
