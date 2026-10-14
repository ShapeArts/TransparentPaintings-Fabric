package dev.transparentpaintings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.transparentpaintings.FrameStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds extruded wooden strips around (not behind) the artwork. */
@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererFrameMixin {
    @Inject(method="submit",at=@At("TAIL"))
    private void transparentpaintings$submitFrames(PaintingRenderState state, PoseStack poses,
       SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if(state.variant==null || state.direction==null || Minecraft.getInstance().level==null) return;
        String dimension=Minecraft.getInstance().level.dimension().location().toString();
        FrameStyles.Style style=FrameStyles.get(FrameStyles.key(dimension,state.x,state.y,state.z));
        if(style.wood()==0) return;
        String type=FrameStyles.WOODS[style.wood()].toLowerCase(java.util.Locale.ROOT).replace(' ','_');
        Identifier texture=Identifier.withDefaultNamespace("textures/block/"+type+"_planks.png");
        float w=state.variant.width(), h=state.variant.height(), t=style.width();
        if(t*2>=w || t*2>=h) return;
        poses.pushPose();
        poses.rotateDegrees(Axis.YP,180f-state.direction.toYRot());
        poses.scale(1f/16f,1f/16f,1f/16f);
        collector.submitCustomGeometry(poses,RenderTypes.entityCutout(texture),(pose,buffer)->{
           float x=-w/2f, y=-h/2f;
           // Four boxes: top, bottom, left, right. Front at -1.35 pixels; back at -0.3.
           box(pose,buffer,x,y+h-t,x+w,y+h,-1.35f,-0.30f,state.lightCoords);
           box(pose,buffer,x,y,x+w,y+t,-1.35f,-0.30f,state.lightCoords);
           box(pose,buffer,x,y+t,x+t,y+h-t,-1.35f,-0.30f,state.lightCoords);
           box(pose,buffer,x+w-t,y+t,x+w,y+h-t,-1.35f,-0.30f,state.lightCoords);
        });
        poses.popPose();
    }
    private static void quad(PoseStack.Pose p,VertexConsumer b,float[] a,float[] c,float[] d,float[] e,int nx,int ny,int nz,int light) {
        vertex(p,b,a[0],a[1],a[2],0,0,nx,ny,nz,light);
        vertex(p,b,c[0],c[1],c[2],1,0,nx,ny,nz,light);
        vertex(p,b,d[0],d[1],d[2],1,1,nx,ny,nz,light);
        vertex(p,b,e[0],e[1],e[2],0,1,nx,ny,nz,light);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,float x,float y,float z,float u,float v,int nx,int ny,int nz,int light) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
    private static float[] v(float x,float y,float z){return new float[]{x,y,z};}
    private static void box(PoseStack.Pose p,VertexConsumer b,float x0,float y0,float x1,float y1,float z0,float z1,int l){
        quad(p,b,v(x0,y0,z0),v(x1,y0,z0),v(x1,y1,z0),v(x0,y1,z0),0,0,-1,l);
        quad(p,b,v(x1,y0,z1),v(x0,y0,z1),v(x0,y1,z1),v(x1,y1,z1),0,0,1,l);
        quad(p,b,v(x0,y0,z1),v(x0,y0,z0),v(x0,y1,z0),v(x0,y1,z1),-1,0,0,l);
        quad(p,b,v(x1,y0,z0),v(x1,y0,z1),v(x1,y1,z1),v(x1,y1,z0),1,0,0,l);
        quad(p,b,v(x0,y1,z0),v(x1,y1,z0),v(x1,y1,z1),v(x0,y1,z1),0,1,0,l);
        quad(p,b,v(x0,y0,z1),v(x1,y0,z1),v(x1,y0,z0),v(x0,y0,z0),0,-1,0,l);
    }
}
