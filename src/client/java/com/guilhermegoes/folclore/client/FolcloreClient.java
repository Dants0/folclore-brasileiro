package com.guilhermegoes.folclore.client;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.criatura.BotoEntity;
import com.guilhermegoes.folclore.criatura.IaraEntity;
import com.guilhermegoes.folclore.criatura.MulaSemCabecaEntity;
import com.guilhermegoes.folclore.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class FolcloreClient implements ClientModInitializer {
	public static final ModelLayerLocation SACI = camada("saci");
	public static final ModelLayerLocation CURUPIRA = camada("curupira");
	public static final ModelLayerLocation BOITATA = camada("boitata");
	public static final ModelLayerLocation MULA = camada("mula_sem_cabeca");
	public static final ModelLayerLocation IARA = camada("iara");
	public static final ModelLayerLocation BOTO = camada("boto");
	public static final ModelLayerLocation CUCA = camada("cuca");
	public static final ModelLayerLocation LOBISOMEM = camada("lobisomem");
	public static final ModelLayerLocation MAPINGUARI = camada("mapinguari");

	private static ModelLayerLocation camada(String nome) {
		return new ModelLayerLocation(Folclore.id(nome), "main");
	}

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(SACI, SaciModel::criar);
		ModelLayerRegistry.registerModelLayer(CURUPIRA, CurupiraModel::criar);
		ModelLayerRegistry.registerModelLayer(BOITATA, BoitataModel::criar);
		ModelLayerRegistry.registerModelLayer(MULA, MulaModel::criar);
		ModelLayerRegistry.registerModelLayer(IARA, IaraModel::criar);
		ModelLayerRegistry.registerModelLayer(BOTO, BotoModel::criar);
		ModelLayerRegistry.registerModelLayer(CUCA, CucaModel::criar);
		ModelLayerRegistry.registerModelLayer(LOBISOMEM, LobisomemModel::criar);
		ModelLayerRegistry.registerModelLayer(MAPINGUARI, MapinguariModel::criar);

		EntityRendererRegistry.register(ModEntities.SACI, SaciRenderer::new);
		EntityRendererRegistry.register(ModEntities.CURUPIRA, CurupiraRenderer::new);
		EntityRendererRegistry.register(ModEntities.BOITATA, c -> new CriaturaRenderer<>(c,
				new BoitataModel(c.bakeLayer(BOITATA)), "boitata", 0.0F, true, CriaturaRenderer::nunca));
		EntityRendererRegistry.register(ModEntities.MULA_SEM_CABECA, c -> new CriaturaRenderer<>(c,
				new MulaModel(c.bakeLayer(MULA)), "mula_sem_cabeca", 0.7F, true, MulaSemCabecaEntity::cansada));
		EntityRendererRegistry.register(ModEntities.IARA, c -> new CriaturaRenderer<>(c,
				new IaraModel(c.bakeLayer(IARA)), "iara", 0.4F, false, IaraEntity::cantando));
		EntityRendererRegistry.register(ModEntities.BOTO, c -> new CriaturaRenderer<>(c,
				new BotoModel(c.bakeLayer(BOTO)), "boto", 0.5F, false, BotoEntity::homem));
		EntityRendererRegistry.register(ModEntities.CUCA, c -> new CriaturaRenderer<>(c,
				new CucaModel(c.bakeLayer(CUCA)), "cuca", 0.5F, true, CriaturaRenderer::nunca));
		EntityRendererRegistry.register(ModEntities.LOBISOMEM, c -> new CriaturaRenderer<>(c,
				new LobisomemModel(c.bakeLayer(LOBISOMEM)), "lobisomem", 0.6F, true, CriaturaRenderer::nunca));
		EntityRendererRegistry.register(ModEntities.MAPINGUARI, c -> new CriaturaRenderer<>(c,
				new MapinguariModel(c.bakeLayer(MAPINGUARI)), "mapinguari", 1.0F, true, CriaturaRenderer::nunca));
		Roteiro.init();
	}
}
