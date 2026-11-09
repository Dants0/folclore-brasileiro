package com.guilhermegoes.folclore;

import com.guilhermegoes.folclore.curupira.Desmatamento;
import com.guilhermegoes.folclore.registry.ModBlocks;
import com.guilhermegoes.folclore.registry.ModEntities;
import com.guilhermegoes.folclore.registry.ModItems;
import com.guilhermegoes.folclore.saci.Aparicoes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Folclore implements ModInitializer {
	public static final String MOD_ID = "folclore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		FolcloreConfig.carregar();
		ModBlocks.init();
		ModEntities.init();
		ModItems.init();
		Aparicoes.init();
		Desmatamento.init();
		com.guilhermegoes.folclore.criatura.Assombracoes.init();
		ComandoFolclore.init();
		LOGGER.info("Folclore: tem um redemoinho vindo aí.");
	}
}
