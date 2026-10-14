package dev.transparentpaintings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Experimental: alpha-cutout front face without the default solid backing. */
@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin {
    @Shadow @Final private TextureAtlas paintingsAtlas;

    @ModifyVariable(method = "renderPainting", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private RenderType transparentpaintings$cutout(RenderType original) {
        return RenderTypes.entityCutout(paintingsAtlas.location());
    }

    // Offset only the painting's front face toward its backing wall to match
    // the frame's revised wall placement; preserve artwork transparency.
    @ModifyVariable(method = "vertex", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private static float transparentpaintings$wallFitArtwork(float z) {
        return z + 0.015625f;
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    private static void transparentpaintings$frontOnly(
            PoseStack.Pose pose, VertexConsumer buffer,
            float x, float y, float u, float v, float z,
            int nx, int ny, int nz, int lightCoords, CallbackInfo ci) {
        if (nx != 0 || ny != 0 || nz != -1) ci.cancel();
    }
}
