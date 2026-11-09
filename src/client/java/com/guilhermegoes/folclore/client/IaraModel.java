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
 * Iara: long black hair down her back, a top of river leaves, and a fish tail in three parts that
 * ripples as she swims. While singing she opens her arms and lifts her face. Texture 64x64.
 */
public class IaraModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart cabeca;
	private final ModelPart cabelo;
	private final ModelPart tronco;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;
	private final ModelPart cauda1;
	private final ModelPart cauda2;
	private final ModelPart barbatana;

	public IaraModel(ModelPart root) {
		super(root);
		cabeca = root.getChild("cabeca");
		cabelo = cabeca.getChild("cabelo");
		tronco = root.getChild("tronco");
		bracoDireito = root.getChild("braco_direito");
		bracoEsquerdo = root.getChild("braco_esquerdo");
		cauda1 = root.getChild("cauda1");
		cauda2 = cauda1.getChild("cauda2");
		barbatana = cauda2.getChild("barbatana");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition cabeca = root.addOrReplaceChild("cabeca", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
				PartPose.offset(0.0F, -1.0F, 0.0F));
		cabeca.addOrReplaceChild("cabelo", CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -8.5F, 0.0F, 9, 14, 3),
				PartPose.offset(0.0F, 0.0F, 1.5F));
		root.addOrReplaceChild("tronco", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 10, 4),
				PartPose.offset(0.0F, -1.0F, 0.0F));
		root.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(24, 16).addBox(-1.5F, -1.0F, -1.5F, 3, 10, 3),
				PartPose.offset(-5.5F, 0.0F, 0.0F));
		root.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(24, 16).mirror().addBox(-1.5F, -1.0F, -1.5F, 3, 10, 3),
				PartPose.offset(5.5F, 0.0F, 0.0F));
		PartDefinition c1 = root.addOrReplaceChild("cauda1", CubeListBuilder.create().texOffs(36, 17).addBox(-3.5F, 0.0F, -2.0F, 7, 6, 4),
				PartPose.offset(0.0F, 9.0F, 0.0F));
		PartDefinition c2 = c1.addOrReplaceChild("cauda2", CubeListBuilder.create().texOffs(0, 30).addBox(-2.5F, 0.0F, -1.5F, 5, 5, 3),
				PartPose.offset(0.0F, 6.0F, 0.0F));
		c2.addOrReplaceChild("barbatana", CubeListBuilder.create().texOffs(16, 30).addBox(-4.0F, 0.0F, -0.5F, 8, 4, 1),
				PartPose.offset(0.0F, 5.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float t = s.ageInTicks;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;
		float onda = 0.2F + vel * 0.3F;
		cauda1.xRot = Mth.sin(t * onda) * 0.15F;
		cauda2.xRot = Mth.sin(t * onda - 1.0F) * 0.3F;
		barbatana.xRot = Mth.sin(t * onda - 2.0F) * 0.4F;
		cabelo.xRot = 0.1F + Mth.sin(t * 0.08F) * 0.08F + vel * 0.3F;
		bracoDireito.xRot = Mth.sin(t * 0.1F) * 0.2F;
		bracoEsquerdo.xRot = -Mth.sin(t * 0.1F) * 0.2F;
		bracoDireito.zRot = 0.15F;
		bracoEsquerdo.zRot = -0.15F;
		if (s.especial) {
			// Singing: arms open towards whoever listens, face lifted.
			cabeca.xRot -= 0.25F;
			bracoDireito.xRot = -0.9F + Mth.sin(t * 0.15F) * 0.1F;
			bracoEsquerdo.xRot = -0.9F - Mth.sin(t * 0.15F) * 0.1F;
			bracoDireito.zRot = 0.6F;
			bracoEsquerdo.zRot = -0.6F;
			tronco.xRot = -0.05F;
		}
	}
}
