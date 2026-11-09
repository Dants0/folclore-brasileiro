package com.guilhermegoes.folclore.saci;

import com.guilhermegoes.folclore.registry.ModEntities;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A Saci corked in a glass bottle, as in the old tale: he owes favours to whoever holds him. Each
 * use, he spins a whirlwind that brings every loose item and experience orb within
 * {@link #RAIO} blocks to your feet. After the last favour (the durability) he breaks the bottle
 * and escapes, laughing, free to rob again.
 */
public class SaciGarrafaItem extends Item {
	public static final double RAIO = 16.0;
	private static final int ESPERA = 20 * 20;

	public SaciGarrafaItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) {
			return InteractionResult.SUCCESS;
		}
		List<ItemEntity> itens = server.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(RAIO), e -> e.isAlive());
		List<ExperienceOrb> orbes = server.getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(RAIO), e -> e.isAlive());
		for (ItemEntity e : itens) {
			server.sendParticles(ParticleTypes.SMALL_GUST, e.getX(), e.getY() + 0.3, e.getZ(), 1, 0, 0, 0, 0);
			e.teleportTo(player.getX(), player.getY() + 0.5, player.getZ());
			e.setNoPickUpDelay();
		}
		for (ExperienceOrb o : orbes) {
			o.teleportTo(player.getX(), player.getY() + 0.5, player.getZ());
		}
		redemoinho(server, player);
		server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS, 1.0F, 1.8F);
		sp.sendOverlayMessage(Component.translatable("folclore.garrafa.favor", itens.size()));
		player.getCooldowns().addCooldown(stack, ESPERA);

		if (stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
			// Last favour: the cork pops and he is free.
			stack.shrink(1);
			server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
			SaciEntity saci = ModEntities.SACI.spawn(server, player.blockPosition().relative(player.getDirection(), 2), EntitySpawnReason.TRIGGERED);
			if (saci != null) {
				saci.rajada(server);
			}
			sp.sendSystemMessage(Component.translatable("folclore.garrafa.fugiu"));
		} else {
			stack.hurtAndBreak(1, player, hand);
		}
		return InteractionResult.SUCCESS;
	}

	private static void redemoinho(ServerLevel level, Player p) {
		for (int i = 0; i < 24; i++) {
			double a = i * Math.PI / 6;
			double h = i * 0.12;
			double r = 0.6 + h * 0.5;
			level.sendParticles(ParticleTypes.CLOUD, p.getX() + Math.cos(a) * r, p.getY() + h, p.getZ() + Math.sin(a) * r, 1, 0, 0.02, 0, 0.0);
		}
		level.sendParticles(ParticleTypes.GUST, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
	}
}
