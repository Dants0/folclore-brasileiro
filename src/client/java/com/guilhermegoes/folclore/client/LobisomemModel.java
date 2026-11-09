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
 * Lobisomem: a hunched wolf-man with a long snout, pointed ears, arms down to the knees ending in
 * claws, wolf legs bent backwards and a bushy tail. Texture 64x64; the eyes glow red.
 */
public class LobisomemModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart tronco;
	private final ModelPart cabeca;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;
	private final ModelPart coxaDireita;
	private final ModelPart coxaEsquerda;
	private final ModelPart rabo;

	public LobisomemModel(ModelPart root) {
		super(root);
		tronco = root.getChild("tronco");
		cabeca = root.getChild("cabeca");
		bracoDireito = root.getChild("braco_direito");
		bracoEsquerdo = root.getChild("braco_esquerdo");
		coxaDireita = root.getChild("coxa_direita");
		coxaEsquerda = root.getChild("coxa_esquerda");
		rabo = root.getChild("rabo");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("tronco", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, 0.0F, -3.0F, 10, 11, 6),
				PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.4F, 0.0F, 0.0F));
		PartDefinition cabeca = root.addOrReplaceChild("cabeca", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.5F, -6.0F, -4.0F, 7, 6, 6)
				.texOffs(26, 0).addBox(-2.0F, -3.0F, -8.0F, 4, 3, 4), PartPose.offset(0.0F, 4.0F, -4.0F));
		cabeca.addOrReplaceChild("orelha_direita", CubeListBuilder.create().texOffs(42, 0).addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1),
				PartPose.offsetAndRotation(-2.3F, -6.0F, 0.5F, 0.0F, 0.0F, -0.2F));
		cabeca.addOrReplaceChild("orelha_esquerda", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1),
				PartPose.offsetAndRotation(2.3F, -6.0F, 0.5F, 0.0F, 0.0F, 0.2F));
		root.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(32, 12).addBox(-1.5F, -1.0F, -1.5F, 3, 15, 3),
				PartPose.offset(-6.5F, 5.0F, -3.0F));
		root.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(32, 12).mirror().addBox(-1.5F, -1.0F, -1.5F, 3, 15, 3),
				PartPose.offset(6.5F, 5.0F, -3.0F));
		for (String lado : new String[] {"direita", "esquerda"}) {
			float x = lado.equals("direita") ? -2.5F : 2.5F;
			PartDefinition coxa = root.addOrReplaceChild("coxa_" + lado, CubeListBuilder.create().texOffs(44, 12).addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4),
					PartPose.offsetAndRotation(x, 13.0F, 1.0F, -0.3F, 0.0F, 0.0F));
			PartDefinition canela = coxa.addOrReplaceChild("canela", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3),
					PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, 0.75F, 0.0F, 0.0F));
			canela.addOrReplaceChild("pe", CubeListBuilder.create().texOffs(12, 29).addBox(-1.5F, 0.0F, -3.0F, 3, 1, 4),
					PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.45F, 0.0F, 0.0F));
		}
		root.addOrReplaceChild("rabo", CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 8),
				PartPose.offsetAndRotation(0.0F, 12.0F, 3.5F, -0.6F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float t = s.ageInTicks;
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;
		coxaDireita.xRot = -0.3F + Mth.cos(pos * 0.6662F) * 1.2F * vel;
		coxaEsquerda.xRot = -0.3F + Mth.cos(pos * 0.6662F + Mth.PI) * 1.2F * vel;
		bracoDireito.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.3F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.6662F) * 1.3F * vel;
		bracoDireito.zRot = 0.15F;
		bracoEsquerdo.zRot = -0.15F;
		rabo.yRot = Mth.sin(t * 0.2F) * 0.25F;
		rabo.xRot = -0.6F + vel * 0.3F;
		tronco.xRot = 0.4F + Mth.sin(t * 0.08F) * 0.03F;
		if (s.agressivo) {
			// Claws out, ready to pounce.
			bracoDireito.xRot -= 0.8F;
			bracoEsquerdo.xRot -= 0.8F;
			bracoDireito.zRot = 0.4F;
			bracoEsquerdo.zRot = -0.4F;
		}
		if (s.swingAnimation > 0) {
			float g = Mth.sin(s.swingAnimation * Mth.PI);
			bracoDireito.xRot = -2.2F + g * 1.8F;
			bracoEsquerdo.xRot = -2.2F + g * 1.6F;
		}
	}
}
