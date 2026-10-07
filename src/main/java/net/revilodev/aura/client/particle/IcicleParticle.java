package net.revilodev.aura.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class IcicleParticle extends TextureSheetParticle {
    private static final float WIDTH_RATIO = 7.0F / 21.0F;
    private final SpriteSet sprites;

    private IcicleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.gravity = 0.45F;
        this.friction = 1.0F;
        this.sprites = sprites;
        this.xd = 0.0D;
        this.yd = -0.3D;
        this.zd = 0.0D;
        this.quadSize = 0.15F;
        this.lifetime = (int) (16.0D / (this.random.nextFloat() * 0.8D + 0.2D)) + 2;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.xd = 0.0D;
        this.zd = 0.0D;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTick);
        if (this.roll != 0.0F) rotation.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));

        Vec3 cameraPosition = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());
        float halfHeight = this.getQuadSize(partialTick);
        float halfWidth = halfHeight * WIDTH_RATIO;
        int light = this.getLightColor(partialTick);

        renderVertex(buffer, rotation, x, y, z, halfWidth, -halfHeight, this.getU1(), this.getV1(), light);
        renderVertex(buffer, rotation, x, y, z, halfWidth, halfHeight, this.getU1(), this.getV0(), light);
        renderVertex(buffer, rotation, x, y, z, -halfWidth, halfHeight, this.getU0(), this.getV0(), light);
        renderVertex(buffer, rotation, x, y, z, -halfWidth, -halfHeight, this.getU0(), this.getV1(), light);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float xOffset, float yOffset, float u, float v, int light) {
        Vector3f position = new Vector3f(xOffset, yOffset, 0.0F).rotate(rotation).add(x, y, z);
        buffer.addVertex(position.x(), position.y(), position.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(light);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new IcicleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
