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
 * Mula sem Cabeça: a mule's body, iron shoes, an iron bridle around the stump of the neck, and
 * fire where the head should be. Gallops with the legs in pairs. Texture 64x64.
 */
public class MulaModel extends EntityModel<CriaturaRenderState> {
	private final ModelPart corpo;
	private final ModelPart pescoco;
	private final ModelPart[] chamas = new ModelPart[3];
	private final ModelPart[] pernas = new ModelPart[4];
	private final ModelPart rabo;

	public MulaModel(ModelPart root) {
		super(root);
		corpo = root.getChild("corpo");
		pescoco = root.getChild("pescoco");
		chamas[0] = pescoco.getChild("chama_0");
		chamas[1] = pescoco.getChild("chama_1");
		chamas[2] = pescoco.getChild("labareda");
		for (int i = 0; i < 4; i++) {
			pernas[i] = root.getChild("perna_" + i);
		}
		rabo = root.getChild("rabo");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("corpo", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -9.0F, 8, 8, 18),
				PartPose.offset(0.0F, 10.0F, 0.0F));
		PartDefinition pescoco = root.addOrReplaceChild("pescoco", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-2.5F, -6.0F, -2.5F, 5, 7, 5)
				.texOffs(0, 38).addBox(-3.0F, -4.0F, -3.0F, 6, 1, 6), PartPose.offsetAndRotation(0.0F, 7.0F, -8.0F, 0.5F, 0.0F, 0.0F));
		pescoco.addOrReplaceChild("chama_0", CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
				PartPose.offsetAndRotation(-1.3F, -6.0F, 0.0F, 0.0F, 0.0F, -0.35F));
		pescoco.addOrReplaceChild("chama_1", CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
				PartPose.offsetAndRotation(1.3F, -6.0F, 0.0F, 0.0F, 0.0F, 0.35F));
		pescoco.addOrReplaceChild("labareda", CubeListBuilder.create().texOffs(28, 26).addBox(-1.5F, -7.0F, -1.5F, 3, 7, 3),
				PartPose.offset(0.0F, -6.0F, 0.0F));
		float[][] pos = {{-2.5F, -6.0F}, {2.5F, -6.0F}, {-2.5F, 7.0F}, {2.5F, 7.0F}};
		for (int i = 0; i < 4; i++) {
			root.addOrReplaceChild("perna_" + i, CubeListBuilder.create().texOffs(40, 26).addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3),
					PartPose.offset(pos[i][0], 14.0F, pos[i][1]));
		}
		root.addOrReplaceChild("rabo", CubeListBuilder.create().texOffs(52, 26).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2),
				PartPose.offsetAndRotation(0.0F, 7.0F, 9.0F, 0.5F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CriaturaRenderState s) {
		super.setupAnim(s);
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		float t = s.ageInTicks;
		float galope = Mth.cos(pos * 0.6662F) * 1.1F * vel;
		pernas[0].xRot = galope;
		pernas[1].xRot = Mth.cos(pos * 0.6662F + 0.4F) * 1.1F * vel;
		pernas[2].xRot = -galope;
		pernas[3].xRot = -Mth.cos(pos * 0.6662F + 0.4F) * 1.1F * vel;
		float pulo = Mth.abs(Mth.sin(pos * 0.6662F)) * 1.5F * vel;
		corpo.y -= pulo;
		pescoco.y -= pulo;
		corpo.xRot = Mth.sin(pos * 0.6662F) * 0.05F * vel;
		pescoco.xRot += Mth.sin(pos * 0.6662F) * 0.1F * vel;
		rabo.xRot = 0.5F + vel * 0.6F + Mth.sin(t * 0.3F) * 0.1F;
		rabo.zRot = Mth.sin(t * 0.17F) * 0.2F;
		for (int i = 0; i < chamas.length; i++) {
			chamas[i].xRot = Mth.sin(t * 0.5F + i * 2.1F) * 0.15F - vel * 0.5F;
			chamas[i].yScale = 1.0F + Mth.sin(t * 0.7F + i) * 0.2F;
		}
		if (s.especial) {
			// Worn out: neck low, flames small.
			pescoco.xRot = 1.0F;
			for (ModelPart c : chamas) {
				c.yScale *= 0.5F;
			}
		}
	}
}
