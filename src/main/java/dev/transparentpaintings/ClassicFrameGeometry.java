package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** v0.8: reference-image eight-slice front, individually UV-mapped closed 3D edges. */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float P=1f/16f, FRONT=-.03125f-P, BACK=FRONT+2*P;
    // Full reference source is 406 x 487. Real wood frame: x=17..383,y=11..470.
    // Opening starts x=51..351 and y=47..437. Use source-relative UVs.
    private static final float U0=17f/406f,U1=51f/406f,U2=352f/406f,U3=384f/406f;
    private static final float V0=11f/487f,V1=47f/487f,V2=438f/487f,V3=471f/487f;

    public static void draw(PoseStack.Pose p,VertexConsumer b,float w,float h,int width,int light) {
        float t=width*P;
        if(t<=0||t*2>=w||t*2>=h)return;
        float x0=-w/2,x1=x0+t,x3=w/2,x2=x3-t;
        float y0=-h/2,y1=y0+t,y3=h/2,y2=y3-t;
        float z=FRONT-.001f;
        // All four corners are independently mapped and never stretched along an edge.
        front(p,b,x0,y2,x1,y3,z,U0,V0,U1,V1,light);
        front(p,b,x1,y2,x2,y3,z,U1,V0,U2,V1,light);
        front(p,b,x2,y2,x3,y3,z,U2,V0,U3,V1,light);
        front(p,b,x0,y1,x1,y2,z,U0,V1,U1,V2,light);
        front(p,b,x2,y1,x3,y2,z,U2,V1,U3,V2,light);
        front(p,b,x0,y0,x1,y1,z,U0,V2,U1,V3,light);
        front(p,b,x1,y0,x2,y1,z,U1,V2,U2,V3,light);
        front(p,b,x2,y0,x3,y1,z,U2,V2,U3,V3,light);

        // The frame is four non-overlapping extruded strips. The full rear,
        // exterior perimeter and interior perimeter are explicitly closed.
        // Each 3D face uses its own corresponding narrow UV strip, not the full painting.
        rear(p,b,x0,y2,x3,y3,U0,V0,U3,V1,light);
        rear(p,b,x0,y0,x3,y1,U0,V2,U3,V3,light);
        rear(p,b,x0,y1,x1,y2,U0,V1,U1,V2,light);
        rear(p,b,x2,y1,x3,y2,U2,V1,U3,V2,light);

        wallY(p,b,x0,x3,y3,1,U1,V0,U2,V1,light); // upper outside
        wallY(p,b,x0,x3,y0,-1,U1,V2,U2,V3,light); // lower outside
        wallX(p,b,x0,y0,y3,-1,U0,V1,U1,V2,light); // left outside
        wallX(p,b,x3,y0,y3,1,U2,V1,U3,V2,light); // right outside
        wallY(p,b,x1,x2,y2,-1,U1,V0,U2,V1,light); // upper inner reveal
        wallY(p,b,x1,x2,y1,1,U1,V2,U2,V3,light); // lower inner reveal
        wallX(p,b,x1,y1,y2,1,U0,V1,U1,V2,light); // left inner reveal
        wallX(p,b,x2,y1,y2,-1,U2,V1,U3,V2,light); // right inner reveal
    }

    private static void front(PoseStack.Pose p,VertexConsumer b,float x0,float y0,float x1,float y1,float z,
                              float u0,float v0,float u1,float v1,int light) {
        quad(p,b,x0,y0,z,x0,y1,z,x1,y1,z,x1,y0,z,u0,v0,u1,v1,0,0,-1,light);
    }
    private static void rear(PoseStack.Pose p,VertexConsumer b,float x0,float y0,float x1,float y1,
                             float u0,float v0,float u1,float v1,int light) {
        quad(p,b,x1,y0,BACK,x1,y1,BACK,x0,y1,BACK,x0,y0,BACK,u0,v0,u1,v1,0,0,1,light);
    }
    private static void wallX(PoseStack.Pose p,VertexConsumer b,float x,float y0,float y1,int normal,
                              float u0,float v0,float u1,float v1,int light) {
        if(normal>0) quad(p,b,x,y0,FRONT,x,y1,FRONT,x,y1,BACK,x,y0,BACK,u0,v0,u1,v1,1,0,0,light);
        else quad(p,b,x,y0,BACK,x,y1,BACK,x,y1,FRONT,x,y0,FRONT,u0,v0,u1,v1,-1,0,0,light);
    }
    private static void wallY(PoseStack.Pose p,VertexConsumer b,float x0,float x1,float y,int normal,
                              float u0,float v0,float u1,float v1,int light) {
        if(normal>0) quad(p,b,x0,y,FRONT,x0,y,BACK,x1,y,BACK,x1,y,FRONT,u0,v0,u1,v1,0,1,0,light);
        else quad(p,b,x0,y,BACK,x0,y,FRONT,x1,y,FRONT,x1,y,BACK,u0,v0,u1,v1,0,-1,0,light);
    }
    private static void quad(PoseStack.Pose p,VertexConsumer b,
                             float ax,float ay,float az,float bx,float by,float bz,
                             float cx,float cy,float cz,float dx,float dy,float dz,
                             float u0,float v0,float u1,float v1,int nx,int ny,int nz,int light) {
        v(p,b,ax,ay,az,u0,v1,nx,ny,nz,light);
        v(p,b,bx,by,bz,u0,v0,nx,ny,nz,light);
        v(p,b,cx,cy,cz,u1,v0,nx,ny,nz,light);
        v(p,b,dx,dy,dz,u1,v1,nx,ny,nz,light);
    }
    private static void v(PoseStack.Pose p,VertexConsumer b,float x,float y,float z,float u,float v,
                          int nx,int ny,int nz,int light) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
}
