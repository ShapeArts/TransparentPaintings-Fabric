package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Tiled custom-texture frame, with four continuous, closed 3D rails. */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float P=1f/16f;
    // Both depth planes are in front of the wall and the vanilla picture face.
    private static final float NEAR=-.03125f-3.0f*P;
    private static final float FAR=-.03125f-.20f*P;
    private static final float U0=17f/406f,U1=51f/406f,U2=352f/406f,U3=384f/406f;
    private static final float V0=11f/487f,V1=47f/487f,V2=438f/487f,V3=471f/487f;
    private static final float TILE=1f; // one world block per repeated pattern

    public static void draw(PoseStack.Pose p,VertexConsumer out,float w,float h,int border,int light){
        float t=border*P;
        if(t<=0||2*t>=w||2*t>=h)return;
        float xl=-w/2,xli=xl+t,xr=w/2,xri=xr-t;
        float yb=-h/2,ybi=yb+t,yt=h/2,yti=yt-t;

        // Distinct corner regions stay fixed at every size.
        front(p,out,xl,yti,xli,yt,U0,V0,U1,V1,light);
        front(p,out,xri,yti,xr,yt,U2,V0,U3,V1,light);
        front(p,out,xl,yb,xli,ybi,U0,V2,U1,V3,light);
        front(p,out,xri,yb,xr,ybi,U2,V2,U3,V3,light);

        // Edge texture is tiled over segments; never stretch it to painting size.
        tiledHorizontal(p,out,xli,xri,yti,yt,U1,V0,U2,V1,light);
        tiledHorizontal(p,out,xli,xri,yb,ybi,U1,V2,U2,V3,light);
        tiledVertical(p,out,xl,xli,ybi,yti,U0,V1,U1,V2,light);
        tiledVertical(p,out,xri,xr,ybi,yti,U2,V1,U3,V2,light);

        // Back: four non-overlapping strips only, never a transparent-art backing.
        rear(p,out,xl,yti,xr,yt,U0,V0,U3,V1,light);
        rear(p,out,xl,yb,xr,ybi,U0,V2,U3,V3,light);
        rear(p,out,xl,ybi,xli,yti,U0,V1,U1,V2,light);
        rear(p,out,xri,ybi,xr,yti,U2,V1,U3,V2,light);

        // Only true boundary faces: avoids coplanar seams and missing corners.
        wallY(p,out,xl,xr,yt,true,U1,V0,U2,V1,light);
        wallY(p,out,xl,xr,yb,false,U1,V2,U2,V3,light);
        wallX(p,out,xl,yb,yt,false,U0,V1,U1,V2,light);
        wallX(p,out,xr,yb,yt,true,U2,V1,U3,V2,light);
        wallY(p,out,xli,xri,yti,false,U1,V0,U2,V1,light);
        wallY(p,out,xli,xri,ybi,true,U1,V2,U2,V3,light);
        wallX(p,out,xli,ybi,yti,true,U0,V1,U1,V2,light);
        wallX(p,out,xri,ybi,yti,false,U2,V1,U3,V2,light);
    }

    private static void tiledHorizontal(PoseStack.Pose p,VertexConsumer b,
            float start,float end,float y0,float y1,
            float u0,float v0,float u1,float v1,int light){
        for(float x=start;x<end-.00001f;){
            float next=Math.min(end,x+TILE);
            float fraction=(next-x)/TILE;
            front(p,b,x,y0,next,y1,u0,v0,u0+(u1-u0)*fraction,v1,light);
            x=next;
        }
    }
    private static void tiledVertical(PoseStack.Pose p,VertexConsumer b,
            float x0,float x1,float start,float end,
            float u0,float v0,float u1,float v1,int light){
        for(float y=start;y<end-.00001f;){
            float next=Math.min(end,y+TILE);
            float fraction=(next-y)/TILE;
            front(p,b,x0,y,x1,next,u0,v0+(v1-v0)*(1f-fraction),u1,v1,light);
            y=next;
        }
    }
    private static void front(PoseStack.Pose p,VertexConsumer b,
            float x0,float y0,float x1,float y1,
            float u0,float v0,float u1,float v1,int light){
        quad(p,b,x0,y0,NEAR,x0,y1,NEAR,x1,y1,NEAR,x1,y0,NEAR,
             u0,v0,u1,v1,0,0,-1,light);
    }
    private static void rear(PoseStack.Pose p,VertexConsumer b,
            float x0,float y0,float x1,float y1,
            float u0,float v0,float u1,float v1,int light){
        quad(p,b,x1,y0,FAR,x1,y1,FAR,x0,y1,FAR,x0,y0,FAR,
             u0,v0,u1,v1,0,0,1,light);
    }
    private static void wallX(PoseStack.Pose p,VertexConsumer b,
            float x,float y0,float y1,boolean right,
            float u0,float v0,float u1,float v1,int light){
        if(right)quad(p,b,x,y0,NEAR,x,y1,NEAR,x,y1,FAR,x,y0,FAR,u0,v0,u1,v1,1,0,0,light);
        else quad(p,b,x,y0,FAR,x,y1,FAR,x,y1,NEAR,x,y0,NEAR,u0,v0,u1,v1,-1,0,0,light);
    }
    private static void wallY(PoseStack.Pose p,VertexConsumer b,
            float x0,float x1,float y,boolean top,
            float u0,float v0,float u1,float v1,int light){
        if(top)quad(p,b,x0,y,NEAR,x0,y,FAR,x1,y,FAR,x1,y,NEAR,u0,v0,u1,v1,0,1,0,light);
        else quad(p,b,x0,y,FAR,x0,y,NEAR,x1,y,NEAR,x1,y,FAR,u0,v0,u1,v1,0,-1,0,light);
    }
    private static void quad(PoseStack.Pose p,VertexConsumer b,
            float ax,float ay,float az,float bx,float by,float bz,
            float cx,float cy,float cz,float dx,float dy,float dz,
            float u0,float v0,float u1,float v1,int nx,int ny,int nz,int light){
        vertex(p,b,ax,ay,az,u0,v1,nx,ny,nz,light);
        vertex(p,b,bx,by,bz,u0,v0,nx,ny,nz,light);
        vertex(p,b,cx,cy,cz,u1,v0,nx,ny,nz,light);
        vertex(p,b,dx,dy,dz,u1,v1,nx,ny,nz,light);
        // Culling-safe opposite winding on the exact same boundary face.
        vertex(p,b,dx,dy,dz,u1,v1,-nx,-ny,-nz,light);
        vertex(p,b,cx,cy,cz,u1,v0,-nx,-ny,-nz,light);
        vertex(p,b,bx,by,bz,u0,v0,-nx,-ny,-nz,light);
        vertex(p,b,ax,ay,az,u0,v1,-nx,-ny,-nz,light);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,
            float x,float y,float z,float u,float v,int nx,int ny,int nz,int light){
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
}
