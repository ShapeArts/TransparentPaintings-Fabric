package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Reference-textured frame with complete closed 3D molding.
 * Four butt-jointed prisms; explicit two-sided faces eliminate winding/culling holes.
 * The front uses eight independently UV-mapped regions to preserve the source corners.
 */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float PX=1f/16f, FRONT=-.03125f-PX, BACK=FRONT+2*PX;
    private static final float U0=17f/406f,U1=51f/406f,U2=352f/406f,U3=384f/406f;
    private static final float V0=11f/487f,V1=47f/487f,V2=438f/487f,V3=471f/487f;

    public static void draw(PoseStack.Pose p,VertexConsumer b,float w,float h,int px,int light){
        float t=px*PX;
        if(t<=0||2*t>=w||2*t>=h)return;
        float x0=-w/2,x1=x0+t,x3=w/2,x2=x3-t;
        float y0=-h/2,y1=y0+t,y3=h/2,y2=y3-t;
        float front=FRONT-.0015f;
        // Eight-slice artwork: corners each sample their own source corner.
        front(p,b,x0,y2,x1,y3,front,U0,V0,U1,V1,light);
        front(p,b,x1,y2,x2,y3,front,U1,V0,U2,V1,light);
        front(p,b,x2,y2,x3,y3,front,U2,V0,U3,V1,light);
        front(p,b,x0,y1,x1,y2,front,U0,V1,U1,V2,light);
        front(p,b,x2,y1,x3,y2,front,U2,V1,U3,V2,light);
        front(p,b,x0,y0,x1,y1,front,U0,V2,U1,V3,light);
        front(p,b,x1,y0,x2,y1,front,U1,V2,U2,V3,light);
        front(p,b,x2,y0,x3,y1,front,U2,V2,U3,V3,light);

        // A genuinely closed rectangular hollow frame, split into four
        // NONoverlapping extruded pieces, so all corners are sealed.
        prism(p,b,x0,y2,x3,y3,U0,V0,U3,V1,light);
        prism(p,b,x0,y0,x3,y1,U0,V2,U3,V3,light);
        prism(p,b,x0,y1,x1,y2,U0,V1,U1,V2,light);
        prism(p,b,x2,y1,x3,y2,U2,V1,U3,V2,light);
    }

    private static void front(PoseStack.Pose p,VertexConsumer b,
        float x0,float y0,float x1,float y1,float z,
        float u0,float v0,float u1,float v1,int l) {
        face(p,b,x0,y0,z,x0,y1,z,x1,y1,z,x1,y0,z,
             u0,v0,u1,v1,0,0,-1,l);
    }
    private static void prism(PoseStack.Pose p,VertexConsumer b,
        float x0,float y0,float x1,float y1,
        float u0,float v0,float u1,float v1,int l) {
        // Front stays just behind the decorative front; preserve proper depth.
        face(p,b,x0,y0,FRONT,x0,y1,FRONT,x1,y1,FRONT,x1,y0,FRONT,
             u0,v0,u1,v1,0,0,-1,l);
        face(p,b,x1,y0,BACK,x1,y1,BACK,x0,y1,BACK,x0,y0,BACK,
             u0,v0,u1,v1,0,0,1,l);
        // Left / right: use their own edge strips, not the full frame artwork.
        face(p,b,x0,y0,BACK,x0,y1,BACK,x0,y1,FRONT,x0,y0,FRONT,
             u0,v0,u0+(u1-u0)*.18f,v1,-1,0,0,l);
        face(p,b,x1,y0,FRONT,x1,y1,FRONT,x1,y1,BACK,x1,y0,BACK,
             u1-(u1-u0)*.18f,v0,u1,v1,1,0,0,l);
        // Top / bottom: texture coordinates match this side's own region.
        face(p,b,x0,y1,FRONT,x0,y1,BACK,x1,y1,BACK,x1,y1,FRONT,
             u0,v0,u1,v0+(v1-v0)*.18f,0,1,0,l);
        face(p,b,x0,y0,BACK,x0,y0,FRONT,x1,y0,FRONT,x1,y0,BACK,
             u0,v1-(v1-v0)*.18f,u1,v1,0,-1,0,l);
    }

    private static void face(PoseStack.Pose p,VertexConsumer b,
        float ax,float ay,float az,float bx,float by,float bz,
        float cx,float cy,float cz,float dx,float dy,float dz,
        float u0,float v0,float u1,float v1,int nx,int ny,int nz,int light) {
        // Both directions: no holes even if render-layer face culling changes
        // with orientation or shader passes. No coplanar duplicated surfaces.
        vertex(p,b,ax,ay,az,u0,v1,nx,ny,nz,light);
        vertex(p,b,bx,by,bz,u0,v0,nx,ny,nz,light);
        vertex(p,b,cx,cy,cz,u1,v0,nx,ny,nz,light);
        vertex(p,b,dx,dy,dz,u1,v1,nx,ny,nz,light);
        vertex(p,b,dx,dy,dz,u1,v1,-nx,-ny,-nz,light);
        vertex(p,b,cx,cy,cz,u1,v0,-nx,-ny,-nz,light);
        vertex(p,b,bx,by,bz,u0,v0,-nx,-ny,-nz,light);
        vertex(p,b,ax,ay,az,u0,v1,-nx,-ny,-nz,light);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,
        float x,float y,float z,float u,float v,int nx,int ny,int nz,int l) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(l).setNormal(p,nx,ny,nz);
    }
}
