package com.guilhermegoes.folclore.criatura;

import com.guilhermegoes.folclore.Folclore;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The iron bridle pulled off the Mula sem Cabeça. Put it on a tamed horse, donkey or mule and it
 * gallops with the speed of the cursed mule from then on (+30% speed, once per animal).
 */
public class FreioMulaItem extends Item {
	private static final net.minecraft.resources.Identifier MODIFICADOR = Folclore.id("freio_mula");

	public FreioMulaItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity alvo, InteractionHand hand) {
		if (!(alvo instanceof AbstractHorse cavalo) || !cavalo.isTamed()) {
			return InteractionResult.PASS;
		}
		AttributeInstance velocidade = cavalo.getAttribute(Attributes.MOVEMENT_SPEED);
		if (velocidade == null || velocidade.hasModifier(MODIFICADOR)) {
			return InteractionResult.PASS;
		}
		if (player.level() instanceof ServerLevel level) {
			velocidade.addPermanentModifier(new AttributeModifier(MODIFICADOR, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
			stack.consume(1, player);
			level.sendParticles(ParticleTypes.FLAME, cavalo.getX(), cavalo.getY() + 1.2, cavalo.getZ(), 25, 0.4, 0.4, 0.4, 0.03);
			level.playSound(null, cavalo.getX(), cavalo.getY(), cavalo.getZ(), SoundEvents.HORSE_ARMOR.value(), SoundSource.NEUTRAL, 1.0F, 0.8F);
			player.sendOverlayMessage(Component.translatable("folclore.mula.freio_usado"));
		}
		return InteractionResult.SUCCESS;
	}
}
