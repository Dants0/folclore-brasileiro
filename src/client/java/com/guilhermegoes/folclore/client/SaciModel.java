package com.guilhermegoes.folclore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * The Saci: a small boy on one leg, red pointed cap and a clay pipe. Texture 64x64 (laid out by
 * tools/gerar_texturas.js). He hops instead of walking, swings his arms, and when laughing he
 * throws his head back and holds his belly.
 */
public class SaciModel extends EntityModel<SaciRenderState> implements ArmedModel<SaciRenderState> {
	private final ModelPart corpo;
	private final ModelPart cabeca;
	private final ModelPart gorroPonta;
	private final ModelPart perna;
	private final ModelPart bracoDireito;
	private final ModelPart bracoEsquerdo;

	public SaciModel(ModelPart root) {
		super(root);
		corpo = root.getChild("corpo");
		cabeca = corpo.getChild("cabeca");
		gorroPonta = cabeca.getChild("gorro").getChild("meio");
		perna = root.getChild("perna");
		bracoDireito = corpo.getChild("braco_direito");
		bracoEsquerdo = corpo.getChild("braco_esquerdo");
	}

	public static LayerDefinition criar() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("perna", CubeListBuilder.create().texOffs(24, 12).addBox(-1.5F, 0.0F, -1.5F, 3, 9, 3),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		PartDefinition corpo = root.addOrReplaceChild("corpo",
				CubeListBuilder.create().texOffs(0, 12).addBox(-3.0F, 0.0F, -1.5F, 6, 7, 3), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition cabeca = corpo.addOrReplaceChild("cabeca",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6), PartPose.ZERO);
		// The pipe sticks out of the left corner of his grin.
		PartDefinition cachimbo = cabeca.addOrReplaceChild("cachimbo",
				CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
				PartPose.offsetAndRotation(1.5F, -1.5F, -3.0F, 0.15F, 0.0F, 0.0F));
		cachimbo.addOrReplaceChild("fornilho", CubeListBuilder.create().texOffs(8, 34).addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2),
				PartPose.offset(0.0F, 0.5F, -3.0F));

		PartDefinition gorro = cabeca.addOrReplaceChild("gorro",
				CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, -2.0F, -3.5F, 7, 2, 7), PartPose.offset(0.0F, -5.5F, 0.0F));
		PartDefinition meio = gorro.addOrReplaceChild("meio",
				CubeListBuilder.create().texOffs(28, 24).addBox(-2.5F, -2.0F, -2.5F, 5, 2, 5),
				PartPose.offsetAndRotation(0.0F, -1.8F, 0.3F, -0.35F, 0.0F, 0.0F));
		meio.addOrReplaceChild("ponta", CubeListBuilder.create().texOffs(48, 24).addBox(-1.5F, -3.0F, -1.5F, 3, 3, 3),
				PartPose.offsetAndRotation(0.0F, -1.6F, 0.6F, -0.5F, 0.0F, 0.0F));

		corpo.addOrReplaceChild("braco_direito", CubeListBuilder.create().texOffs(40, 12).addBox(-1.0F, -1.0F, -1.0F, 2, 7, 2),
				PartPose.offset(-4.0F, 1.0F, 0.0F));
		corpo.addOrReplaceChild("braco_esquerdo", CubeListBuilder.create().texOffs(48, 12).addBox(-1.0F, -1.0F, -1.0F, 2, 7, 2),
				PartPose.offset(4.0F, 1.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(SaciRenderState s) {
		super.setupAnim(s);
		float pos = s.walkAnimationPos;
		float vel = Mth.clamp(s.walkAnimationSpeed, 0.0F, 1.0F);
		float t = s.ageInTicks;

		cabeca.yRot = s.yRot * Mth.DEG_TO_RAD;
		cabeca.xRot = s.xRot * Mth.DEG_TO_RAD;

		// Hopping on one leg: the whole body bounces and the leg kicks a little.
		float pulo = Mth.abs(Mth.sin(pos * 0.6662F)) * 3.0F * vel;
		corpo.y -= pulo;
		perna.y -= pulo;
		perna.xRot = Mth.sin(pos * 0.6662F * 2.0F) * 0.35F * vel;

		bracoDireito.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * vel;
		bracoEsquerdo.xRot = Mth.cos(pos * 0.6662F) * 1.4F * vel;
		bracoDireito.zRot = 0.1F + Mth.cos(t * 0.09F) * 0.05F;
		bracoEsquerdo.zRot = -0.1F - Mth.cos(t * 0.09F) * 0.05F;

		gorroPonta.zRot = Mth.sin(t * 0.12F) * 0.12F + Mth.sin(pos * 0.6662F) * 0.2F * vel;

		if (s.rindo) {
			// Head thrown back, hands on the belly, shaking with laughter.
			float tremor = Mth.sin(t * 2.2F);
			cabeca.xRot = -0.45F + tremor * 0.08F;
			corpo.xRot = -0.08F + tremor * 0.03F;
			corpo.y += Mth.abs(tremor) * 0.6F;
			bracoDireito.xRot = -0.8F;
			bracoDireito.zRot = 0.9F;
			bracoEsquerdo.xRot = -0.8F;
			bracoEsquerdo.zRot = -0.9F;
		}
		if (!s.getMainHandItemState().isEmpty() && !s.rindo) {
			// Holding the loot up high, showing off.
			bracoDireito.xRot = -1.9F + Mth.sin(t * 0.3F) * 0.15F;
			bracoDireito.zRot = 0.15F;
		}
		if (s.swingAnimation > 0) {
			bracoDireito.xRot = -1.2F + Mth.sin(s.swingAnimation * Mth.PI) * -1.0F;
		}
	}

	@Override
	public void translateToHand(SaciRenderState state, HumanoidArm arm, PoseStack poseStack) {
		ModelPart braco = arm == HumanoidArm.RIGHT ? bracoDireito : bracoEsquerdo;
		root.translateAndRotate(poseStack);
		corpo.translateAndRotate(poseStack);
		braco.translateAndRotate(poseStack);
		poseStack.translate(arm == HumanoidArm.RIGHT ? 0.0F : 0.0F, 0.2F, 0.0F);
		poseStack.scale(0.7F, 0.7F, 0.7F);
	}
}
