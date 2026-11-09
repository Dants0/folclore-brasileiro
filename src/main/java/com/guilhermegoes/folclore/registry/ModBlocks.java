package com.guilhermegoes.folclore.registry;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.curupira.PegadaBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
	/** The Curupira's backward footprint; it has no item. */
	public static final Block PEGADA = Registry.register(BuiltInRegistries.BLOCK, Folclore.id("pegada"),
			new PegadaBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, Folclore.id("pegada")))
					.noCollision().instabreak().replaceable().noLootTable()
					.sound(SoundType.MUD).pushReaction(PushReaction.POPPED)));

	public static void init() {
	}

	private ModBlocks() {
	}
}
