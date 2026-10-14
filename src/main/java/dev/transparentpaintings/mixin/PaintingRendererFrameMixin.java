package dev.transparentpaintings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.transparentpaintings.FrameStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * v0.4 true 3D picture frame.
 *
 * Keeps the proven painting transparency Mixin completely separate.
 * Geometry consists of four nonoverlapping closed rectangular prisms.
 * The front border width is user selected (1–6 texture pixels), with
 * a fixed physical depth of 2 pixels and 1 pixel inset relative to its front.
 * The open center has NO backing surface, preserving alpha cutout artwork.
 */
@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererFrameMixin {
    private static final float PIXEL = 1f / 16f;
    private static final float DEPTH = 2f * PIXEL;
    // Painting front is at approximately -0.03125 in vanilla 26.3.
    // Center recess is 1 pixel from raised outer frame.
    private static final float FRONT = -0.03125f - PIXEL;
    private static final float BACK = FRONT + DEPTH;

    @Inject(method = "submit", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            shift = At.Shift.BEFORE))
    private void transparentpaintings$drawFrame(
            PaintingRenderState state, PoseStack poses,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || state.variant == null || state.direction == null) return;

        String dimension = mc.level.dimension().identifier().toString();
        FrameStyles.Style selected = FrameStyles.get(
                FrameStyles.key(dimension, state.x, state.y, state.z));
        if (selected.wood() == 0) return;

        String woodName = FrameStyles.WOODS[selected.wood()]
                .toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        TextureAtlasSprite sprite = mc.getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(Identifier.withDefaultNamespace("block/" + woodName + "_planks"));

        float width = state.variant.width();
        float height = state.variant.height();
        float border = selected.width() * PIXEL;
        if (2f * border >= width || 2f * border >= height) return;

        float x0 = -width / 2f;
        float x1 = width / 2f;
        float y0 = -height / 2f;
        float y1 = height / 2f;
        int light = state.lightCoords;
        // Vanilla submit already rotated the incoming pose stack.
        // Do not rotate or scale again. The collector snapshots this pose.
        collector.submitCustomGeometry(poses, RenderTypes.entitySolid(sprite.atlasLocation()),
                (pose, buffer) -> {
                    // Butt joints, with no overlaps or corner z fighting:
                    prism(pose, buffer, sprite, x0, y1-border, x1, y1, FRONT, BACK, light);
                    prism(pose, buffer, sprite, x0, y0, x1, y0+border, FRONT, BACK, light);
                    prism(pose, buffer, sprite, x0, y0+border, x0+border, y1-border, FRONT, BACK, light);
                    prism(pose, buffer, sprite, x1-border, y0+border, x1, y1-border, FRONT, BACK, light);
                });
    }

    private static void prism(PoseStack.Pose pose, VertexConsumer out,
            TextureAtlasSprite tex, float x0,float y0,float x1,float y1,
            float z0,float z1,int light) {
        // Six quads, correct outward normals and consistent clockwise winding.
        face(pose,out,tex, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, 0,0,-1,light);
        face(pose,out,tex, x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1, 0,0,1,light);
        face(pose,out,tex, x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0, -1,0,0,light);
        face(pose,out,tex, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, 1,0,0,light);
        face(pose,out,tex, x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0, 0,1,0,light);
        face(pose,out,tex, x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1, 0,-1,0,light);
    }

    private static void face(PoseStack.Pose p,VertexConsumer b,TextureAtlasSprite t,
            float ax,float ay,float az, float bx,float by,float bz,
            float cx,float cy,float cz, float dx,float dy,float dz,
            int nx,int ny,int nz,int light) {
        // Sprite UV input is normalized 0–1 (not 0–16).
        // Restrict UVs to the actual sprite rectangle to avoid atlas bleeding.
        vertex(p,b,t,ax,ay,az,0,0,nx,ny,nz,light);
        vertex(p,b,t,bx,by,bz,0,1,nx,ny,nz,light);
        vertex(p,b,t,cx,cy,cz,1,1,nx,ny,nz,light);
        vertex(p,b,t,dx,dy,dz,1,0,nx,ny,nz,light);
    }

    private static void vertex(PoseStack.Pose pose,VertexConsumer buffer,
            TextureAtlasSprite sprite,float x,float y,float z,float u,float v,
            int nx,int ny,int nz,int light) {
        buffer.addVertex(pose,x,y,z)
                .setColor(-1).setUv(sprite.getU(u),sprite.getV(v))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose,nx,ny,nz);
    }
}
