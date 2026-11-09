package com.guilhermegoes.folclore.client;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.curupira.CurupiraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CurupiraRenderer extends MobRenderer<CurupiraEntity, CurupiraRenderState, CurupiraModel> {
	private static final Identifier TEXTURA = Folclore.id("textures/entity/curupira.png");
	/** Hair and eyes glow in the dark of the forest. */
	private static final RenderType BRILHO = RenderTypes.eyes(Folclore.id("textures/entity/curupira_brilho.png"));

	public CurupiraRenderer(EntityRendererProvider.Context context) {
		super(context, new CurupiraModel(context.bakeLayer(FolcloreClient.CURUPIRA)), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return BRILHO;
			}
		});
	}

	@Override
	public CurupiraRenderState createRenderState() {
		return new CurupiraRenderState();
	}

	@Override
	public void extractRenderState(CurupiraEntity entity, CurupiraRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
		state.calmo = entity.calmo();
		state.cacando = entity.isAggressive();
	}

	@Override
	public Identifier getTextureLocation(CurupiraRenderState state) {
		return TEXTURA;
	}
}
