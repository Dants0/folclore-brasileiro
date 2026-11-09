package com.guilhermegoes.folclore.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Mapinguari: a hulking giant covered in reddish fur, a small head with a single eye, a huge mouth
 * in the middle of the belly, long arms with hooked claws, short thick legs. Texture 128x64.
 */
public class MapinguariModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart tronco;
	private final ModelPart cabeca;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;
	private final ModelPart pernaDireita;
	private final ModelPart pernaEsquerda;

	public MapinguariModel(ModelPart root) {
		super(root);
		tronco = root.getChild("tronco");
		cabeca = root.getChild("cabeca");
		bracoDireito = root.getChild("braco_direito");
		bracoEsquerdo = root.getChild("braco_esquerdo");
		pernaDireita = root.getChild("perna_direita");
		pernaEsquerda = root.getChild("perna_esquerda");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("tronco", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -10.0F, -5.0F, 16, 20, 10), PartPose.ZERO);
		root.addOrReplaceChild("cabeca", CubeListBuilder.create().texOffs(52, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
				PartPose.offset(0.0F, -10.0F, -1.0F));
		root.addOrReplaceChild("braco_direito", CubeListBuilder.create()
				.texOffs(84, 0).addBox(-3.0F, -2.0F, -3.0F, 6, 26, 6)
				.texOffs(28, 30).addBox(-3.0F, 22.0F, -5.0F, 6, 3, 2), PartPose.offset(-11.0F, -8.0F, 0.0F));
		root.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create()
				.texOffs(84, 0).mirror().addBox(-3.0F, -2.0F, -3.0F, 6, 26, 6)
				.texOffs(28, 30).addBox(-3.0F, 22.0F, -5.0F, 6, 3, 2), PartPose.offset(11.0F, -8.0F, 0.0F));
		root.addOrReplaceChild("perna_direita", CubeListBuilder.create().texOffs(0, 30).addBox(-3.5F, 0.0F, -3.5F, 7, 14, 7),
				PartPose.offset(-4.5F, 10.0F, 0.0F));
		root.addOrReplaceChild("perna_esquerda", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-3.5F, 0.0F, -3.5F, 7, 14, 7),
				PartPose.offset(4.5F, 10.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float t = s.ageInTicks;
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;
		// Heavy steps: the body rolls from side to side.
		pernaDireita.xRot = Mth.cos(pos * 0.5F) * 0.8F * vel;
		pernaEsquerda.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 0.8F * vel;
		tronco.zRot = Mth.sin(pos * 0.5F) * 0.06F * vel;
		cabeca.zRot = tronco.zRot;
		bracoDireito.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 0.6F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.5F) * 0.6F * vel;
		bracoDireito.zRot = 0.08F + Mth.sin(t * 0.05F) * 0.03F;
		bracoEsquerdo.zRot = -0.08F - Mth.sin(t * 0.05F) * 0.03F;
		tronco.y += Mth.sin(t * 0.07F) * 0.4F;
		if (s.swingAnimation > 0) {
			// Both arms come down like hammers.
			float g = Mth.sin(s.swingAnimation * Mth.PI);
			bracoDireito.xRot = -2.4F + g * 2.2F;
			bracoEsquerdo.xRot = -2.4F + g * 2.2F;
		}
	}
}
