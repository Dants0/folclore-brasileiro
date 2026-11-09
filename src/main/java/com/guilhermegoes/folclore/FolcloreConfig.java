package com.guilhermegoes.folclore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Settings in {@code config/folclore.json}. The file is created with the defaults on first launch
 * and rewritten on every launch, so new options show up in it after an update.
 */
public final class FolcloreConfig {
	/** Whether the Saci visits players on his own (the command and spawn egg always work). */
	public boolean saciAtivo = true;
	/** One chance in this many, per player and per minute outdoors, that a Saci comes. */
	public int saciChance = 20;
	/** Whether the forest keeps score and the Curupira comes on his own. */
	public boolean curupiraAtivo = true;
	/** Forest points at which the whistle and the footprints begin. */
	public int curupiraAviso = 24;
	/** Forest points at which the Curupira comes in person. */
	public int curupiraFuria = 48;
	/**
	 * One chance in N, per player and per minute when the legend's conditions are met, that each
	 * creature shows up. 0 turns that creature's natural appearances off.
	 */
	public int boitataChance = 25;
	public int mulaChance = 30;
	public int iaraChance = 12;
	public int botoChance = 8;
	public int cucaChance = 3;
	public int lobisomemChance = 5;
	public int mapinguariChance = 20;
	/** Whether lighting a fire near the forest calls the Boitatá. */
	public boolean boitataContraQueimada = true;

	private static FolcloreConfig atual = new FolcloreConfig();

	public static FolcloreConfig get() {
		return atual;
	}

	public static void carregar() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Path arq = FabricLoader.getInstance().getConfigDir().resolve("folclore.json");
		if (Files.exists(arq)) {
			try (Reader r = Files.newBufferedReader(arq, StandardCharsets.UTF_8)) {
				FolcloreConfig lido = gson.fromJson(r, FolcloreConfig.class);
				if (lido != null) {
					atual = lido;
				}
			} catch (IOException | RuntimeException e) {
				Folclore.LOGGER.warn("Folclore: config/folclore.json inválido, usando o padrão: {}", e.toString());
			}
		}
		atual.saciChance = Math.max(1, atual.saciChance);
		atual.curupiraAviso = Math.max(1, atual.curupiraAviso);
		try {
			Files.createDirectories(arq.getParent());
			try (Writer w = Files.newBufferedWriter(arq, StandardCharsets.UTF_8)) {
				gson.toJson(atual, w);
			}
		} catch (IOException e) {
			Folclore.LOGGER.warn("Folclore: não consegui gravar config/folclore.json: {}", e.toString());
		}
	}

	private FolcloreConfig() {
	}
}
