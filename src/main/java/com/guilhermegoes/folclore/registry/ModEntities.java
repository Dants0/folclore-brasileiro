package com.guilhermegoes.folclore.registry;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.criatura.BoitataEntity;
import com.guilhermegoes.folclore.criatura.BotoEntity;
import com.guilhermegoes.folclore.criatura.CucaEntity;
import com.guilhermegoes.folclore.criatura.IaraEntity;
import com.guilhermegoes.folclore.criatura.LobisomemEntity;
import com.guilhermegoes.folclore.criatura.MapinguariEntity;
import com.guilhermegoes.folclore.criatura.MulaSemCabecaEntity;
import com.guilhermegoes.folclore.curupira.CurupiraEntity;
import com.guilhermegoes.folclore.saci.SaciEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	private static final ResourceKey<EntityType<?>> SACI_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Folclore.id("saci"));
	private static final ResourceKey<EntityType<?>> CURUPIRA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Folclore.id("curupira"));

	/** The Saci: a small one-legged prankster in a red cap. */
	public static final EntityType<SaciEntity> SACI = Registry.register(BuiltInRegistries.ENTITY_TYPE, SACI_KEY,
			EntityType.Builder.of(SaciEntity::new, MobCategory.MISC)
					.sized(0.5F, 1.3F)
					.eyeHeight(1.05F)
					.fireImmune()
					.clientTrackingRange(10)
					.build(SACI_KEY));

	/** The Curupira: guardian of the forest, with flaming hair and backward feet. */
	public static final EntityType<CurupiraEntity> CURUPIRA = Registry.register(BuiltInRegistries.ENTITY_TYPE, CURUPIRA_KEY,
			EntityType.Builder.of(CurupiraEntity::new, MobCategory.MISC)
					.sized(0.6F, 1.6F)
					.eyeHeight(1.35F)
					.fireImmune()
					.clientTrackingRange(10)
					.build(CURUPIRA_KEY));

	public static final EntityType<BoitataEntity> BOITATA = registrar("boitata", BoitataEntity::new, 1.2F, 0.8F, 0.4F, true);
	public static final EntityType<MulaSemCabecaEntity> MULA_SEM_CABECA = registrar("mula_sem_cabeca", MulaSemCabecaEntity::new, 1.3F, 1.5F, 1.3F, true);
	public static final EntityType<IaraEntity> IARA = registrar("iara", IaraEntity::new, 0.6F, 1.8F, 1.55F, false);
	public static final EntityType<BotoEntity> BOTO = registrar("boto", BotoEntity::new, 0.9F, 0.6F, 0.3F, false);
	public static final EntityType<CucaEntity> CUCA = registrar("cuca", CucaEntity::new, 0.7F, 1.9F, 1.6F, false);
	public static final EntityType<LobisomemEntity> LOBISOMEM = registrar("lobisomem", LobisomemEntity::new, 0.8F, 2.1F, 1.8F, false);
	public static final EntityType<MapinguariEntity> MAPINGUARI = registrar("mapinguari", MapinguariEntity::new, 1.6F, 2.9F, 2.4F, false);

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> registrar(String nome, EntityType.EntityFactory<T> fabrica,
			float largura, float altura, float olhos, boolean fogo) {
		ResourceKey<EntityType<?>> chave = ResourceKey.create(Registries.ENTITY_TYPE, Folclore.id(nome));
		EntityType.Builder<T> b = EntityType.Builder.of(fabrica, MobCategory.MISC).sized(largura, altura).eyeHeight(olhos).clientTrackingRange(10);
		if (fogo) {
			b = b.fireImmune();
		}
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, chave, b.build(chave));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(BOITATA, BoitataEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MULA_SEM_CABECA, MulaSemCabecaEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(IARA, IaraEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(BOTO, BotoEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CUCA, CucaEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(LOBISOMEM, LobisomemEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MAPINGUARI, MapinguariEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SACI, SaciEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CURUPIRA, CurupiraEntity.createAttributes());
	}

	private ModEntities() {
	}
}
