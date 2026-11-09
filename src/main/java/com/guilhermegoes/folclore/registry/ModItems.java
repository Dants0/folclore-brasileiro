package com.guilhermegoes.folclore.registry;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.saci.PeneiraItem;
import com.guilhermegoes.folclore.saci.SaciGarrafaItem;
import com.guilhermegoes.folclore.criatura.FreioMulaItem;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.Equippable;

public final class ModItems {
	/** Peneira de taquara: thrown over the whirlwind, it is the only thing that catches a Saci. */
	public static final Item PENEIRA = register("peneira", PeneiraItem::new,
			new Item.Properties().durability(16));

	/**
	 * The Saci's red cap. Whoever holds it is quick on their feet, and no Saci dares to rob
	 * them while they wear it.
	 */
	public static final Item GORRO_SACI = register("gorro_saci", Item::new, new Item.Properties().stacksTo(1)
			.rarity(Rarity.RARE)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
					.setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build())
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(Folclore.id("gorro_saci"), 0.2,
							AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HEAD)
					.add(Attributes.JUMP_STRENGTH, new AttributeModifier(Folclore.id("gorro_saci_pulo"), 0.1,
							AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
					.build()));

	/** A Saci corked in a bottle: five favours, then he breaks free. */
	public static final Item SACI_GARRAFA = register("saci_garrafa", SaciGarrafaItem::new,
			new Item.Properties().durability(5).rarity(Rarity.RARE));

	/** The Boitatá's shining eye: swallow it to see in the dark and walk through fire for a while. */
	public static final Item OLHO_BOITATA = register("olho_boitata", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON)
			.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).alwaysEdible().build(),
					Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
							new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 3, 0),
							new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0)))).build()));

	/** The iron bridle of the Mula sem Cabeça: makes a tamed horse much faster. */
	public static final Item FREIO_MULA = register("freio_mula", FreioMulaItem::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

	/** A scale of the Iara: eat it to breathe and swim like her for three minutes. */
	public static final Item ESCAMA_IARA = register("escama_iara", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON)
			.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build(),
					Consumables.defaultFood().consumeSeconds(0.8F).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
							new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 60 * 3, 0),
							new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 20 * 60 * 3, 0)))).build()));

	/** The Boto's white hat: swim fast and hold your breath longer while you wear it. */
	public static final Item CHAPEU_BOTO = register("chapeu_boto", Item::new, new Item.Properties().stacksTo(1)
			.rarity(Rarity.RARE)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
					.setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build())
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(Folclore.id("chapeu_boto"), 0.6,
							AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
					.add(Attributes.OXYGEN_BONUS, new AttributeModifier(Folclore.id("chapeu_boto_folego"), 3.0,
							AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
					.build()));

	/** A claw of the Mapinguari: a heavy, brutal weapon. */
	public static final Item GARRA_MAPINGUARI = register("garra_mapinguari", Item::new, new Item.Properties()
			.sword(ToolMaterial.DIAMOND, 4.0F, -2.8F).rarity(Rarity.RARE));

	public static final Item SACI_SPAWN_EGG = ovo("saci", ModEntities.SACI);
	public static final Item CURUPIRA_SPAWN_EGG = ovo("curupira", ModEntities.CURUPIRA);
	public static final Item BOITATA_SPAWN_EGG = ovo("boitata", ModEntities.BOITATA);
	public static final Item MULA_SPAWN_EGG = ovo("mula_sem_cabeca", ModEntities.MULA_SEM_CABECA);
	public static final Item IARA_SPAWN_EGG = ovo("iara", ModEntities.IARA);
	public static final Item BOTO_SPAWN_EGG = ovo("boto", ModEntities.BOTO);
	public static final Item CUCA_SPAWN_EGG = ovo("cuca", ModEntities.CUCA);
	public static final Item LOBISOMEM_SPAWN_EGG = ovo("lobisomem", ModEntities.LOBISOMEM);
	public static final Item MAPINGUARI_SPAWN_EGG = ovo("mapinguari", ModEntities.MAPINGUARI);

	public static final CreativeModeTab ABA = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Folclore.id("folclore"),
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.folclore"))
					.icon(() -> new ItemStack(GORRO_SACI))
					.displayItems((params, output) -> {
						output.accept(PENEIRA);
						output.accept(GORRO_SACI);
						output.accept(SACI_GARRAFA);
						output.accept(OLHO_BOITATA);
						output.accept(FREIO_MULA);
						output.accept(ESCAMA_IARA);
						output.accept(CHAPEU_BOTO);
						output.accept(GARRA_MAPINGUARI);
						output.accept(SACI_SPAWN_EGG);
						output.accept(CURUPIRA_SPAWN_EGG);
						output.accept(BOITATA_SPAWN_EGG);
						output.accept(MULA_SPAWN_EGG);
						output.accept(IARA_SPAWN_EGG);
						output.accept(BOTO_SPAWN_EGG);
						output.accept(CUCA_SPAWN_EGG);
						output.accept(LOBISOMEM_SPAWN_EGG);
						output.accept(MAPINGUARI_SPAWN_EGG);
					})
					.build());

	private static Item ovo(String nome, net.minecraft.world.entity.EntityType<?> tipo) {
		return register(nome + "_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(tipo));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Folclore.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
	}

	public static void init() {
	}

	private ModItems() {
	}
}
