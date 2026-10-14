package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Model-based picture frame. The structure follows the uploaded Fast Paintings
 * wooden-framed block models: outer wooden rails, inset lip, and open center.
 * The transparent painting itself continues to be rendered independently.
 */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float PIXEL = 1f / 16f;
    private static final float FRONT = -0.03125f - PIXEL;
    private static final float BACK = FRONT + 3f * PIXEL;
    private static final float U0=17f/406f,U1=51f/406f,U2=352f/406f,U3=384f/406f;
    private static final float V0=11f/487f,V1=47f/487f,V2=438f/487f,V3=471f/487f;

    public static void draw(PoseStack.Pose p, VertexConsumer b, float w, float h, int width, int light) {
        float t=width*PIXEL;
        if(t<=0 || t*2>=w || t*2>=h)return;
        float x0=-w/2,x1=x0+t,x3=w/2,x2=x3-t;
        float y0=-h/2,y1=y0+t,y3=h/2,y2=y3-t;
        float z=FRONT-0.001f;

        // Precisely eight independent texture areas, matching the uploaded reference.
        front(p,b,x0,y2,x1,y3,z,U0,V0,U1,V1,light);
        front(p,b,x1,y2,x2,y3,z,U1,V0,U2,V1,light);
        front(p,b,x2,y2,x3,y3,z,U2,V0,U3,V1,light);
        front(p,b,x0,y1,x1,y2,z,U0,V1,U1,V2,light);
        front(p,b,x2,y1,x3,y2,z,U2,V1,U3,V2,light);
        front(p,b,x0,y0,x1,y1,z,U0,V2,U1,V3,light);
        front(p,b,x1,y0,x2,y1,z,U1,V2,U2,V3,light);
        front(p,b,x2,y0,x3,y1,z,U2,V2,U3,V3,light);

        // Solid top and bottom rails, then left/right rails fitting exactly between.
        // Each rail is a complete rectangular prism: all edges are sealed.
        cuboid(p,b,x0,y2,x3,y3,U0,V0,U3,V1,light);
        cuboid(p,b,x0,y0,x3,y1,U0,V2,U3,V3,light);
        cuboid(p,b,x0,y1,x1,y2,U0,V1,U1,V2,light);
        cuboid(p,b,x2,y1,x3,y2,U2,V1,U3,V2,light);
    }

    private static void cuboid(PoseStack.Pose p, VertexConsumer b,
            float x0,float y0,float x1,float y1,
            float u0,float v0,float u1,float v1,int light) {
        // Back face. No opaque panel across the painting's opening.
        face(p,b,x1,y0,BACK,x1,y1,BACK,x0,y1,BACK,x0,y0,BACK,
             u0,v0,u1,v1,0,0,1,light);
        // Full outside / inside walls, including corners and bottom.
        // Texture UV is a narrow corresponding strip, not the entire image.
        float du=(u1-u0)*.23f,dv=(v1-v0)*.23f;
        face(p,b,x0,y0,BACK,x0,y1,BACK,x0,y1,FRONT,x0,y0,FRONT,
             u0,v0,u0+du,v1,-1,0,0,light);
        face(p,b,x1,y0,FRONT,x1,y1,FRONT,x1,y1,BACK,x1,y0,BACK,
             u1-du,v0,u1,v1,1,0,0,light);
        face(p,b,x0,y1,FRONT,x0,y1,BACK,x1,y1,BACK,x1,y1,FRONT,
             u0,v0,u1,v0+dv,0,1,0,light);
        face(p,b,x0,y0,BACK,x0,y0,FRONT,x1,y0,FRONT,x1,y0,BACK,
             u0,v1-dv,u1,v1,0,-1,0,light);
    }

    private static void front(PoseStack.Pose p,VertexConsumer b,
            float x0,float y0,float x1,float y1,float z,
            float u0,float v0,float u1,float v1,int light) {
        face(p,b,x0,y0,z,x0,y1,z,x1,y1,z,x1,y0,z,
             u0,v0,u1,v1,0,0,-1,light);
    }
    private static void face(PoseStack.Pose p,VertexConsumer b,
            float ax,float ay,float az,float bx,float by,float bz,
            float cx,float cy,float cz,float dx,float dy,float dz,
            float u0,float v0,float u1,float v1,int nx,int ny,int nz,int light) {
        // One face, correct winding; no duplicate coplanar triangles.
        vertex(p,b,ax,ay,az,u0,v1,nx,ny,nz,light);
        vertex(p,b,bx,by,bz,u0,v0,nx,ny,nz,light);
        vertex(p,b,cx,cy,cz,u1,v0,nx,ny,nz,light);
        vertex(p,b,dx,dy,dz,u1,v1,nx,ny,nz,light);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,
            float x,float y,float z,float u,float v,int nx,int ny,int nz,int light) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
}
