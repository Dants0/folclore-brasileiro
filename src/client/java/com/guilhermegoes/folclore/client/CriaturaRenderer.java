package com.guilhermegoes.folclore.client;

import com.guilhermegoes.folclore.Folclore;
import java.util.function.Predicate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/**
 * One renderer for every folklore creature: a texture in textures/entity/&lt;name&gt;.png, an
 * optional glow layer (&lt;name&gt;_brilho.png: fire, eyes) drawn at full brightness, and one
 * "special" flag per creature (the Boto in human shape, the Iara singing, the Mula worn out...).
 */
public class CriaturaRenderer<E extends Mob, M extends EntityModel<CriaturaRenderState>> extends MobRenderer<E, CriaturaRenderState, M> {
	private final Identifier textura;
	private final Predicate<E> especial;

	public CriaturaRenderer(EntityRendererProvider.Context context, M model, String nome, float sombra, boolean brilho, Predicate<E> especial) {
		super(context, model, sombra);
		this.textura = Folclore.id("textures/entity/" + nome + ".png");
		this.especial = especial;
		if (brilho) {
			RenderType tipo = RenderTypes.eyes(Folclore.id("textures/entity/" + nome + "_brilho.png"));
			addLayer(new EyesLayer<>(this) {
				@Override
				public RenderType renderType() {
					return tipo;
				}
			});
		}
	}

	@Override
	public CriaturaRenderState createRenderState() {
		return new CriaturaRenderState();
	}

	@Override
	public void extractRenderState(E entity, CriaturaRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
		state.especial = especial.test(entity);
		state.agressivo = entity.isAggressive();
		state.naAgua = entity.isInWater();
	}

	@Override
	public Identifier getTextureLocation(CriaturaRenderState state) {
		return textura;
	}

	/** For creatures with nothing special to show. */
	public static <E> boolean nunca(@Nullable E e) {
		return false;
	}
}
