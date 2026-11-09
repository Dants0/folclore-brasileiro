package com.guilhermegoes.folclore.saci;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The peneira (sieve) of the old tale: throw it over the whirlwind and the Saci is caught. Aim at
 * a Saci up to {@link #ALCANCE} blocks away and right-click; a miss costs a short cooldown.
 */
public class PeneiraItem extends Item {
	public static final double ALCANCE = 9.0;
	/** Half-angle of the throw cone, as a cosine (about 14 degrees). */
	private static final double MIRA = 0.97;

	public PeneiraItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW,
				SoundSource.PLAYERS, 0.8F, 0.6F);
		if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
			SaciEntity saci = mirar(server, sp);
			if (saci != null) {
				saci.capturar(sp);
				stack.hurtAndBreak(1, player, hand);
			} else {
				player.getCooldowns().addCooldown(stack, 20);
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** The Saci closest to the player's line of sight within reach, if any. */
	private static @Nullable SaciEntity mirar(ServerLevel level, ServerPlayer player) {
		Vec3 olho = player.getEyePosition();
		Vec3 olhar = player.getViewVector(1.0F);
		SaciEntity melhor = null;
		double melhorMira = MIRA;
		for (SaciEntity s : level.getEntitiesOfClass(SaciEntity.class, player.getBoundingBox().inflate(ALCANCE))) {
			Vec3 ate = s.position().add(0, s.getBbHeight() / 2, 0).subtract(olho);
			double d = ate.length();
			if (d > ALCANCE || d < 0.01) {
				continue;
			}
			// The closer he is, the more forgiving the aim.
			double mira = ate.normalize().dot(olhar) + Math.min(0.05, 0.3 / d);
			if (mira > melhorMira && player.hasLineOfSight(s)) {
				melhor = s;
				melhorMira = mira;
			}
		}
		return melhor;
	}
}
