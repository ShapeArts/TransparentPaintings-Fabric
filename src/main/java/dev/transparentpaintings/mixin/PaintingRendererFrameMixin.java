package dev.transparentpaintings.mixin;

import dev.transparentpaintings.ClassicFrameGeometry;
import dev.transparentpaintings.FrameStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds v0.5 Classic molding within the native painting orientation. */
@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererFrameMixin {
    @Inject(method="submit",at=@At(value="INVOKE",
       target="Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
       shift=At.Shift.BEFORE))
    private void transparentpaintings$submitFrames(PaintingRenderState state, PoseStack poses,
        SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null || state.variant==null || state.direction==null) return;
        String dimension=mc.level.dimension().identifier().toString();
        FrameStyles.Style style=FrameStyles.get(FrameStyles.key(dimension,state.x,state.y,state.z));
        if(style.wood()==0) return;
        String type=FrameStyles.WOODS[style.wood()].toLowerCase(java.util.Locale.ROOT).replace(' ','_');
        TextureAtlasSprite sprite=mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
            .getSprite(Identifier.withDefaultNamespace("block/"+type+"_planks"));
        float w=state.variant.width(), h=state.variant.height();
        int border=style.width(), light=state.lightCoords;
        collector.submitCustomGeometry(poses,RenderTypes.entitySolid(sprite.atlasLocation()),
           (pose,buffer)->ClassicFrameGeometry.draw(pose,buffer,sprite,w,h,border,light));
    }
}
