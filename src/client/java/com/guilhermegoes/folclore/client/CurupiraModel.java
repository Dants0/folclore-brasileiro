package com.guilhermegoes.folclore.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Curupira: a wiry forest spirit with a mane of fire, a leaf skirt and feet turned backwards
 * (the heel points forward, the toes back). Texture 64x64, laid out by tools/gerar_texturas.js.
 * The flames of his hair flicker all the time.
 */
public class CurupiraModel extends EntityModel<CurupiraRenderState> {
	/** Flame tufts: x, z on top of the head, and how far each leans out (x tilt, z tilt). */
	private static final float[][] CHAMAS = {
			{-2.5F, -1.5F, -0.35F, -0.45F}, {0.0F, -2.0F, -0.25F, 0.0F}, {2.5F, -1.5F, -0.35F, 0.45F},
			{-2.5F, 1.5F, 0.45F, -0.4F}, {2.5F, 1.5F, 0.45F, 0.4F}, {0.0F, 2.5F, 0.7F, 0.0F},
	};

	private final ModelPart corpo;
	private final ModelPart cabeca;
	private final ModelPart[] chamas = new ModelPart[CHAMAS.length + 1];
	private final ModelPart pernaDireita;
	private final ModelPart pernaEsquerda;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;

	public CurupiraModel(ModelPart root) {
		super(root);
		corpo = root.getChild("corpo");
		cabeca = root.getChild("cabeca");
		for (int i = 0; i < CHAMAS.length; i++) {
			chamas[i] = cabeca.getChild("chama_" + i);
		}
		chamas[CHAMAS.length] = cabeca.getChild("labareda");
		pernaDireita = root.getChild("perna_direita");
		pernaEsquerda = root.getChild("perna_esquerda");
		bracoDireito = root.getChild("braco_direito");
		bracoEsquerdo = root.getChild("braco_esquerdo");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition cabeca = root.addOrReplaceChild("cabeca",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7, 7, 7)
						.texOffs(28, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 3, 8, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));
		for (int i = 0; i < CHAMAS.length; i++) {
			float[] c = CHAMAS[i];
			cabeca.addOrReplaceChild("chama_" + i, CubeListBuilder.create().texOffs(24, 28).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
					PartPose.offsetAndRotation(c[0], -7.5F, c[1], c[2], 0.0F, c[3]));
		}
		cabeca.addOrReplaceChild("labareda", CubeListBuilder.create().texOffs(32, 28).addBox(-1.5F, -6.0F, -1.5F, 3, 6, 3),
				PartPose.offsetAndRotation(0.0F, -7.5F, 0.5F, 0.35F, 0.0F, 0.0F));

		root.addOrReplaceChild("corpo", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, 0.0F, -2.0F, 7, 8, 4)
				.texOffs(0, 40).addBox(-4.0F, 5.0F, -2.5F, 8, 3, 5), PartPose.offset(0.0F, 6.0F, 0.0F));

		// Backward feet: the foot sticks out behind the leg (+Z), not in front.
		PartDefinition pd = root.addOrReplaceChild("perna_direita", CubeListBuilder.create().texOffs(22, 14).addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3),
				PartPose.offset(-1.7F, 14.0F, 0.0F));
		pd.addOrReplaceChild("pe", CubeListBuilder.create().texOffs(12, 28).addBox(-1.5F, 9.0F, 0.5F, 3, 1, 3), PartPose.ZERO);
		PartDefinition pe = root.addOrReplaceChild("perna_esquerda", CubeListBuilder.create().texOffs(34, 14).addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3),
				PartPose.offset(1.7F, 14.0F, 0.0F));
		pe.addOrReplaceChild("pe", CubeListBuilder.create().texOffs(12, 28).mirror().addBox(-1.5F, 9.0F, 0.5F, 3, 1, 3), PartPose.ZERO);

		root.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(46, 14).addBox(-1.5F, -1.0F, -1.5F, 3, 9, 3),
				PartPose.offset(-5.0F, 7.0F, 0.0F));
		root.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.0F, -1.5F, 3, 9, 3),
				PartPose.offset(5.0F, 7.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CurupiraRenderState s) {
		super.setupAnim(s);
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		float t = s.ageInTicks;

		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;

		pernaDireita.xRot = Mth.cos(pos * 0.6662F) * 1.3F * vel;
		pernaEsquerda.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.3F * vel;
		bracoDireito.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.1F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.6662F) * 1.1F * vel;
		bracoDireito.zRot = 0.12F + Mth.cos(t * 0.09F) * 0.05F;
		bracoEsquerdo.zRot = -0.12F - Mth.cos(t * 0.09F) * 0.05F;

		// Living fire: every tuft flickers on its own rhythm.
		for (int i = 0; i < chamas.length; i++) {
			chamas[i].xRot += Mth.sin(t * 0.45F + i * 1.7F) * 0.12F;
			chamas[i].zRot += Mth.cos(t * 0.38F + i * 2.3F) * 0.1F;
			chamas[i].yScale = 1.0F + Mth.sin(t * 0.6F + i) * 0.15F;
		}

		if (s.swingAnimation > 0) {
			// Both arms come down like claws.
			float g = Mth.sin(s.swingAnimation * Mth.PI);
			bracoDireito.xRot = -2.0F + g * 1.6F;
			bracoEsquerdo.xRot = -2.0F + g * 1.6F;
			corpo.xRot = g * 0.25F;
		} else if (s.calmo) {
			cabeca.xRot = 0.25F;
			bracoDireito.xRot = -0.3F;
			bracoEsquerdo.xRot = -0.3F;
		} else if (s.cacando) {
			// Crouched forward, arms out: hunting.
			corpo.xRot = 0.2F;
			bracoDireito.xRot -= 0.6F;
			bracoEsquerdo.xRot -= 0.6F;
		}
	}
}
