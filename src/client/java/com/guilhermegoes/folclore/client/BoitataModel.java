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
 * Boitatá: a long serpent of fire with two huge glowing eyes. Seven body segments chained one
 * after the other, each swaying a little later than the one before, so the body slithers across
 * the ground. Texture 64x64.
 */
public class BoitataModel extends EntityModel<CriaturaRenderState> {
	private static final int GOMOS = 7;

	private final ModelPart corpo;
	private final ModelPart cabeca;
	private final ModelPart[] gomos = new ModelPart[GOMOS];

	public BoitataModel(ModelPart root) {
		super(root);
		corpo = root.getChild("corpo");
		cabeca = corpo.getChild("cabeca");
		ModelPart g = corpo;
		for (int i = 0; i < GOMOS; i++) {
			g = g.getChild("gomo_" + i);
			gomos[i] = g;
		}
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition corpo = mesh.getRoot().addOrReplaceChild("corpo", CubeListBuilder.create(), PartPose.offset(0.0F, 21.5F, 0.0F));
		PartDefinition cabeca = corpo.addOrReplaceChild("cabeca", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -2.5F, -8.0F, 6, 5, 8)
				.texOffs(28, 0).addBox(-4.2F, -3.0F, -6.5F, 2, 3, 3)
				.texOffs(28, 0).mirror().addBox(2.2F, -3.0F, -6.5F, 2, 3, 3), PartPose.ZERO);
		cabeca.addOrReplaceChild("lingua", CubeListBuilder.create().texOffs(38, 5).addBox(-0.5F, 0.0F, -3.0F, 1, 0, 3),
				PartPose.offset(0.0F, 1.5F, -8.0F));
		PartDefinition anterior = corpo;
		for (int i = 0; i < GOMOS; i++) {
			float encolhe = -0.25F * i;
			PartDefinition g = anterior.addOrReplaceChild("gomo_" + i, CubeListBuilder.create()
					.texOffs(0, 13).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 6, new CubeDeformation(encolhe))
					.texOffs(38, 0).addBox(-0.5F, -4.5F - encolhe, 1.5F, 1, 3, 2), PartPose.offset(0.0F, 0.0F, i == 0 ? 0.0F : 6.0F));
			anterior = g;
		}
		anterior.addOrReplaceChild("cauda", CubeListBuilder.create().texOffs(22, 13).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 5),
				PartPose.offset(0.0F, 0.0F, 6.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float t = s.ageInTicks;
		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD * 0.6F;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;
		float rapidez = s.agressivo ? 0.45F : 0.3F;
		for (int i = 0; i < GOMOS; i++) {
			gomos[i].yRot = Mth.sin(t * rapidez - i * 0.8F) * 0.3F;
		}
	}
}
