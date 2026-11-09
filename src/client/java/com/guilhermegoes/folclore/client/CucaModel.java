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
 * Cuca: a hunched old witch with an alligator's head (long snout, a jaw that snaps when she
 * cackles, bulging eyes), straw-yellow hair, a long dress and the alligator's tail dragging
 * behind. Texture 64x64.
 */
public class CucaModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart cabeca;
	private final ModelPart mandibula;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;
	private final ModelPart pernaDireita;
	private final ModelPart pernaEsquerda;
	private final ModelPart rabo;

	public CucaModel(ModelPart root) {
		super(root);
		cabeca = root.getChild("cabeca");
		mandibula = cabeca.getChild("mandibula");
		bracoDireito = root.getChild("braco_direito");
		bracoEsquerdo = root.getChild("braco_esquerdo");
		pernaDireita = root.getChild("perna_direita");
		pernaEsquerda = root.getChild("perna_esquerda");
		rabo = root.getChild("rabo");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition cabeca = root.addOrReplaceChild("cabeca", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.5F, -6.0F, -3.5F, 7, 6, 7)
				.texOffs(28, 0).addBox(-2.5F, -3.5F, -9.0F, 5, 3, 6)
				.texOffs(50, 0).addBox(-3.2F, -7.0F, -2.0F, 2, 2, 2)
				.texOffs(50, 0).mirror().addBox(1.2F, -7.0F, -2.0F, 2, 2, 2).mirror(false)
				.texOffs(0, 13).addBox(-4.0F, -6.5F, -0.5F, 8, 8, 5), PartPose.offset(0.0F, 5.0F, -2.0F));
		cabeca.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(28, 9).addBox(-2.5F, 0.0F, -5.0F, 5, 1, 5),
				PartPose.offset(0.0F, -0.5F, -3.0F));
		root.addOrReplaceChild("tronco", CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, 0.0F, -2.5F, 8, 11, 5),
				PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.3F, 0.0F, 0.0F));
		root.addOrReplaceChild("saia", CubeListBuilder.create().texOffs(26, 26).addBox(-5.0F, 0.0F, -3.5F, 10, 6, 7),
				PartPose.offset(0.0F, 14.5F, 1.0F));
		root.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(0, 42).addBox(-1.5F, -1.0F, -1.5F, 3, 11, 3),
				PartPose.offset(-5.0F, 6.0F, -1.0F));
		root.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-1.5F, -1.0F, -1.5F, 3, 11, 3),
				PartPose.offset(5.0F, 6.0F, -1.0F));
		root.addOrReplaceChild("perna_direita", CubeListBuilder.create().texOffs(12, 42).addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3),
				PartPose.offset(-2.0F, 19.0F, 1.0F));
		root.addOrReplaceChild("perna_esquerda", CubeListBuilder.create().texOffs(12, 42).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3),
				PartPose.offset(2.0F, 19.0F, 1.0F));
		root.addOrReplaceChild("rabo", CubeListBuilder.create().texOffs(24, 42).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 8),
				PartPose.offsetAndRotation(0.0F, 17.0F, 4.0F, -0.5F, 0.0F, 0.0F));
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
		pernaDireita.xRot = Mth.cos(pos * 0.6662F) * 1.0F * vel;
		pernaEsquerda.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.0F * vel;
		bracoDireito.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 0.8F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.6662F) * 0.8F * vel;
		rabo.yRot = Mth.sin(t * 0.15F + pos * 0.5F) * 0.3F;
		// The jaw snaps as she cackles.
		mandibula.xRot = 0.15F + Mth.abs(Mth.sin(t * 0.4F)) * (s.agressivo ? 0.45F : 0.15F);
		if (s.agressivo) {
			// Arms raised, casting.
			bracoDireito.xRot = -2.2F + Mth.sin(t * 0.3F) * 0.2F;
			bracoEsquerdo.xRot = -2.2F - Mth.sin(t * 0.3F) * 0.2F;
			bracoDireito.zRot = 0.3F;
			bracoEsquerdo.zRot = -0.3F;
		}
		if (s.swingAnimation > 0) {
			bracoDireito.xRot = -2.5F + Mth.sin(s.swingAnimation * Mth.PI) * 1.5F;
		}
	}
}
