package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Plain four-piece 3D wooden picture frame, no stepped decorative bevel.
 * Frame texture comes from the selected vanilla wood plank sprite.
 */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float PIXEL = 1f/16f;
    // The vanilla painting artwork is approximately z=-0.03125.
    private static final float FRONT = -0.03125f - PIXEL;
    private static final float BACK = FRONT + 2f*PIXEL;

    public static void draw(PoseStack.Pose pose, VertexConsumer out, TextureAtlasSprite sprite, boolean directUv,
                            float width,float height,int borderPixels,int light) {
        float t=borderPixels*PIXEL;
        if(t<=0 || 2*t>=width || 2*t>=height) return;
        float l=-width/2f,r=width/2f,b=-height/2f,top=height/2f;
        // One continuous rectangular profile with four non-overlapping segments.
        // Wood plank atlas texture is mapped only inside its own sprite rectangle.
        box(pose,out,sprite,directUv,l,top-t,r,top,FRONT,BACK,light);
        box(pose,out,sprite,directUv,l,b,r,b+t,FRONT,BACK,light);
        box(pose,out,sprite,directUv,l,b+t,l+t,top-t,FRONT,BACK,light);
        box(pose,out,sprite,directUv,r-t,b+t,r,top-t,FRONT,BACK,light);
    }

    private static void box(PoseStack.Pose p,VertexConsumer b,TextureAtlasSprite s, boolean directUv,
                            float x0,float y0,float x1,float y1,float z0,float z1,int light) {
        // A flat wood front, two-pixel real extrusion, dark inner edges.
        // Front is drawn separately using the reference image (nine-slice UV layout).
        quad(p,b,s,directUv,x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,y0,z1,0,0,1,light,0xFFB8B8B8);
        quad(p,b,s,directUv,x0,y0,z1,x0,y1,z1,x0,y1,z0,x0,y0,z0,-1,0,0,light,0xFFC2C2C2);
        quad(p,b,s,directUv,x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1,1,0,0,light,0xFF9B9B9B);
        quad(p,b,s,directUv,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,0,1,0,light,0xFFE5E5E5);
        quad(p,b,s,directUv,x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1,0,-1,0,light,0xFFAFAFAF);
    }
    private static void quad(PoseStack.Pose p,VertexConsumer b,TextureAtlasSprite s, boolean directUv,
                              float ax,float ay,float az,float bx,float by,float bz,
                              float cx,float cy,float cz,float dx,float dy,float dz,
                              int nx,int ny,int nz,int light,int tint) {
        vertex(p,b,s,directUv,ax,ay,az,0,0,nx,ny,nz,light,tint);
        vertex(p,b,s,directUv,bx,by,bz,0,1,nx,ny,nz,light,tint);
        vertex(p,b,s,directUv,cx,cy,cz,1,1,nx,ny,nz,light,tint);
        vertex(p,b,s,directUv,dx,dy,dz,1,0,nx,ny,nz,light,tint);
    }
    /**
     * Four nonoverlapping textured front strips. UV coordinates are sampled
     * from the supplied frame image, with corners preserved and the center cut out.
     * Cropped input: 371 x 461; approximately 32 px horizontal and 37 px vertical border.
     */
    public static void drawFront(PoseStack.Pose p,VertexConsumer b,float w,float h,int pixels,int light) {
        float t=pixels*PIXEL;
        if(2*t>=w || 2*t>=h || t<=0) return;
        float l=-w/2f,r=w/2f,y0=-h/2f,y1=h/2f;
        float z=FRONT-0.001f;
        float ul=32f/371f, ur=336f/371f, vt=37f/461f, vb=427f/461f;
        face(p,b,l,y1-t,r,y1,z,0f,0f,1f,vt,light);
        face(p,b,l,y0,r,y0+t,z,0f,vb,1f,1f,light);
        face(p,b,l,y0+t,l+t,y1-t,z,0f,vt,ul,vb,light);
        face(p,b,r-t,y0+t,r,y1-t,z,ur,vt,1f,vb,light);
    }
    private static void face(PoseStack.Pose p,VertexConsumer b,
                             float x0,float y0,float x1,float y1,float z,
                             float u0,float v0,float u1,float v1,int light) {
        faceVertex(p,b,x0,y0,z,u0,v1,light);
        faceVertex(p,b,x0,y1,z,u0,v0,light);
        faceVertex(p,b,x1,y1,z,u1,v0,light);
        faceVertex(p,b,x1,y0,z,u1,v1,light);
    }
    private static void faceVertex(PoseStack.Pose p,VertexConsumer b,
                                   float x,float y,float z,float u,float v,int light) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,0,-1);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,TextureAtlasSprite s, boolean directUv,
                               float x,float y,float z,float u,float v,
                               int nx,int ny,int nz,int light,int tint) {
        b.addVertex(p,x,y,z).setColor(tint).setUv(directUv ? u : s.getU(u),directUv ? v : s.getV(v))
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
}
