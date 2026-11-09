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
 * Boto: both of his shapes in one model, only one shown at a time. The pink river dolphin (long
 * beak, small dorsal hump, flukes) and the young man in a white suit and white hat, who sways and
 * dances when standing still. Texture 128x64: dolphin on the left half, man on the right.
 */
public class BotoModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart golfinho;
	private final ModelPart cauda;
	private final ModelPart nadadeira;
	private final ModelPart homem;
	private final ModelPart cabeca;
	private final ModelPart tronco;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;
	private final ModelPart pernaDireita;
	private final ModelPart pernaEsquerda;

	public BotoModel(ModelPart root) {
		super(root);
		golfinho = root.getChild("golfinho");
		cauda = golfinho.getChild("cauda");
		nadadeira = cauda.getChild("nadadeira");
		homem = root.getChild("homem");
		cabeca = homem.getChild("cabeca");
		tronco = homem.getChild("tronco");
		bracoDireito = homem.getChild("braco_direito");
		bracoEsquerdo = homem.getChild("braco_esquerdo");
		pernaDireita = homem.getChild("perna_direita");
		pernaEsquerda = homem.getChild("perna_esquerda");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition g = root.addOrReplaceChild("golfinho", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.5F, -6.0F, 8, 7, 13),
				PartPose.offset(0.0F, 20.5F, 0.0F));
		g.addOrReplaceChild("bico", CubeListBuilder.create().texOffs(42, 0).addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4), PartPose.offset(0.0F, 1.0F, -6.0F));
		g.addOrReplaceChild("dorsal", CubeListBuilder.create().texOffs(52, 20).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 4),
				PartPose.offsetAndRotation(0.0F, -3.5F, 1.0F, 0.4F, 0.0F, 0.0F));
		g.addOrReplaceChild("aleta_direita", CubeListBuilder.create().texOffs(24, 25).addBox(-4.0F, -0.5F, -1.0F, 4, 1, 3),
				PartPose.offsetAndRotation(-4.0F, 2.0F, -2.0F, 0.0F, 0.0F, -0.5F));
		g.addOrReplaceChild("aleta_esquerda", CubeListBuilder.create().texOffs(24, 25).mirror().addBox(0.0F, -0.5F, -1.0F, 4, 1, 3),
				PartPose.offsetAndRotation(4.0F, 2.0F, -2.0F, 0.0F, 0.0F, 0.5F));
		PartDefinition cauda = g.addOrReplaceChild("cauda", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
				PartPose.offset(0.0F, -0.5F, 7.0F));
		cauda.addOrReplaceChild("nadadeira", CubeListBuilder.create().texOffs(24, 20).addBox(-5.0F, -0.5F, 0.0F, 10, 1, 4),
				PartPose.offset(0.0F, 0.0F, 7.0F));

		PartDefinition h = root.addOrReplaceChild("homem", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition cabeca = h.addOrReplaceChild("cabeca", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
				PartPose.ZERO);
		cabeca.addOrReplaceChild("chapeu", CubeListBuilder.create()
				.texOffs(64, 32).addBox(-5.0F, -8.6F, -5.0F, 10, 1, 10)
				.texOffs(104, 32).addBox(-3.0F, -11.5F, -3.0F, 6, 3, 6), PartPose.ZERO);
		h.addOrReplaceChild("tronco", CubeListBuilder.create().texOffs(64, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4), PartPose.ZERO);
		h.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(88, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		h.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(88, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		h.addOrReplaceChild("perna_direita", CubeListBuilder.create().texOffs(104, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		h.addOrReplaceChild("perna_esquerda", CubeListBuilder.create().texOffs(104, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float t = s.ageInTicks;
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		golfinho.visible = !s.especial;
		homem.visible = s.especial;
		if (!s.especial) {
			golfinho.xRot = s.xRot * Mth.DEG_TO_RAD;
			cauda.xRot = Mth.sin(t * 0.3F) * 0.25F;
			nadadeira.xRot = Mth.sin(t * 0.3F - 0.8F) * 0.35F;
			return;
		}
		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;
		pernaDireita.xRot = Mth.cos(pos * 0.6662F) * 1.4F * vel;
		pernaEsquerda.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * vel;
		bracoDireito.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.0F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.6662F) * 1.0F * vel;
		if (vel < 0.1F) {
			// Standing still, he dances: hips sway, arms keep the beat.
			float b = Mth.sin(t * 0.35F);
			tronco.zRot = b * 0.08F;
			cabeca.zRot = -b * 0.06F;
			bracoDireito.xRot = -0.6F + b * 0.4F;
			bracoEsquerdo.xRot = -0.6F - b * 0.4F;
			bracoDireito.zRot = 0.3F;
			bracoEsquerdo.zRot = -0.3F;
			pernaDireita.zRot = Math.max(0, b) * 0.15F;
			pernaEsquerda.zRot = Math.min(0, b) * 0.15F;
		}
	}
}
