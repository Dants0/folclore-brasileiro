package com.guilhermegoes.folclore.client;

import com.guilhermegoes.folclore.Folclore;
import com.guilhermegoes.folclore.saci.SaciEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;

public class SaciRenderer extends MobRenderer<SaciEntity, SaciRenderState, SaciModel> {
	private static final Identifier TEXTURA = Folclore.id("textures/entity/saci.png");

	public SaciRenderer(EntityRendererProvider.Context context) {
		super(context, new SaciModel(context.bakeLayer(FolcloreClient.SACI)), 0.35F);
		addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public SaciRenderState createRenderState() {
		return new SaciRenderState();
	}

	@Override
	public void extractRenderState(SaciEntity entity, SaciRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
		state.rindo = entity.rindo();
	}

	@Override
	public Identifier getTextureLocation(SaciRenderState state) {
		return TEXTURA;
	}
}
