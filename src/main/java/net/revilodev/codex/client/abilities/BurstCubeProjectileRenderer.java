package net.revilodev.codex.client.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.codex.CodexMod;
import net.revilodev.codex.abilities.AbilityElement;
import net.revilodev.codex.entity.projectile.BurstCubeProjectile;

public final class BurstCubeProjectileRenderer extends EntityRenderer<BurstCubeProjectile> {
    private static final ResourceLocation DUMMY = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/abilities/fire.png");
    private static final float HALF = 3.0F / 32.0F; // 3x3 px cube in block units

    public BurstCubeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BurstCubeProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        AbilityElement element = entity.element();
        if (element == AbilityElement.FIRE) {
            r = 1.0F; g = 0.45F; b = 0.1F;
        } else if (element == AbilityElement.ICE) {
            r = 0.35F; g = 0.7F; b = 1.0F;
        } else if (element == AbilityElement.POISON) {
            r = 0.35F; g = 1.0F; b = 0.35F;
        }
        VertexConsumer vc = buffer.getBuffer(RenderType.debugQuads());
        addCube(vc, poseStack, r, g, b, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void addCube(VertexConsumer vc, PoseStack poseStack, float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        // front
        addQuad(vc, pose, -HALF, -HALF, HALF, HALF, -HALF, HALF, HALF, HALF, HALF, -HALF, HALF, HALF, r, g, b, a);
        // back
        addQuad(vc, pose, HALF, -HALF, -HALF, -HALF, -HALF, -HALF, -HALF, HALF, -HALF, HALF, HALF, -HALF, r, g, b, a);
        // left
        addQuad(vc, pose, -HALF, -HALF, -HALF, -HALF, -HALF, HALF, -HALF, HALF, HALF, -HALF, HALF, -HALF, r, g, b, a);
        // right
        addQuad(vc, pose, HALF, -HALF, HALF, HALF, -HALF, -HALF, HALF, HALF, -HALF, HALF, HALF, HALF, r, g, b, a);
        // top
        addQuad(vc, pose, -HALF, HALF, HALF, HALF, HALF, HALF, HALF, HALF, -HALF, -HALF, HALF, -HALF, r, g, b, a);
        // bottom
        addQuad(vc, pose, -HALF, -HALF, -HALF, HALF, -HALF, -HALF, HALF, -HALF, HALF, -HALF, -HALF, HALF, r, g, b, a);
    }

    private static void addQuad(VertexConsumer vc, PoseStack.Pose pose,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                float r, float g, float b, float a) {
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
        vc.addVertex(pose, x4, y4, z4).setColor(r, g, b, a);
    }

    @Override
    public ResourceLocation getTextureLocation(BurstCubeProjectile entity) {
        return DUMMY;
    }
}
