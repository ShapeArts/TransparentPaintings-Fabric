package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Blockbench-inspired tile model, based on the user's Wooden Framed Paintings pack.
 *
 * Unlike the old floating frame, each 16x16 painting tile owns short, fixed-size
 * pieces of solid wood (outer rail + inner trim). No geometry is stretched to
 * the overall painting dimensions, and all frame vertices stay in front of
 * the painting's rear plane. The center remains open for transparent artwork.
 */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float PX = 1f/16f;
    private static final float REAR = -.03125f-.015f;
    private static final float FRONT = REAR-2f*PX;
    private static final float LIP_FRONT = REAR-1.35f*PX;

    public static void draw(PoseStack.Pose pose,VertexConsumer output,
                            float width,float height,int requestedWidth,int light) {
        int nx=Math.max(1,Math.round(width)), ny=Math.max(1,Math.round(height));
        int border=Math.max(1,Math.min(6,requestedWidth));
        int outer=Math.max(1,(int)Math.ceil(border*.67));
        int inner=border-outer;
        // All coordinates are in painting-oriented block units, tiled 1 block each.
        for(int ty=0;ty<ny;ty++)for(int tx=0;tx<nx;tx++){
            float dx=-width/2f+tx,dy=-height/2f+ty;
            boolean left=tx==0,right=tx==nx-1,bottom=ty==0,top=ty==ny-1;
            // Outer perimeter: horizontal rails span their full tile;
            // vertical rails fit between horizontal rails, with no shared corners.
            if(top) cube(pose,output,dx,dy+(16-outer)*PX,dx+1,dy+1,
                         FRONT,REAR, 3.2f,3.2f,0,light);
            if(bottom)cube(pose,output,dx,dy,dx+1,dy+outer*PX,
                           FRONT,REAR,3.2f,3.2f,0,light);
            if(left)cube(pose,output,dx,dy+(bottom?outer:0)*PX,
                          dx+outer*PX,dy+(top?16-outer:16)*PX,
                          FRONT,REAR,3.2f,3.2f,0,light);
            if(right)cube(pose,output,dx+(16-outer)*PX,dy+(bottom?outer:0)*PX,
                           dx+1,dy+(top?16-outer:16)*PX,
                           FRONT,REAR,3.2f,3.2f,0,light);
            if(inner>0){
                if(top)cube(pose,output,dx+((left?outer:0)*PX),
                    dy+(16-border)*PX,
                    dx+((right?16-outer:16)*PX),dy+(16-outer)*PX,
                    LIP_FRONT,REAR,1.0f,1.0f,1,light);
                if(bottom)cube(pose,output,dx+((left?outer:0)*PX),dy+outer*PX,
                    dx+((right?16-outer:16)*PX),dy+border*PX,
                    LIP_FRONT,REAR,1.0f,1.0f,1,light);
                if(left)cube(pose,output,dx+outer*PX,dy+(bottom?border:0)*PX,
                    dx+border*PX,dy+(top?16-border:16)*PX,
                    LIP_FRONT,REAR,1.0f,1.0f,1,light);
                if(right)cube(pose,output,dx+(16-border)*PX,dy+(bottom?border:0)*PX,
                    dx+(16-outer)*PX,dy+(top?16-border:16)*PX,
                    LIP_FRONT,REAR,1.0f,1.0f,1,light);
            }
        }
    }

    private static void cube(PoseStack.Pose p,VertexConsumer b,
            float x0,float y0,float x1,float y1,float z0,float z1,
            float baseU,float baseV,int kind,int light) {
        if(x1<=x0||y1<=y0||z1<=z0)return;
        // UV dimensions correspond to 5 pixels per model-UV unit in the supplied
        // 80x80 atlas. Each block gets one fixed pattern rather than stretching.
        float tileX=x0-(float)Math.floor(x0),tileY=y0-(float)Math.floor(y0);
        float u=baseU+tileX*3.2f, v=baseV+(1f-tileY)*3.2f;
        float du=(x1-x0)*3.2f,dv=(y1-y0)*3.2f,dz=(z1-z0)*3.2f;
        // All surfaces sampled from valid 0..16 UVs of the original 80x80 atlas.
        // Front face. Unlike v0.10, orientation is never extrapolated along a wall.
        face(p,b,x0,y0,z0,x0,y1,z0,x1,y1,z0,x1,y0,z0,
             u,v-dv,u+du,v,0,0,-1,light);
        face(p,b,x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,y0,z1,
             u,v-dv,u+du,v,0,0,1,light);
        face(p,b,x0,y0,z1,x0,y1,z1,x0,y1,z0,x0,y0,z0,
             kind==0?2f:1.2f,v-dv,(kind==0?2f:1.2f)+dz,v,-1,0,0,light);
        face(p,b,x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1,
             kind==0?2.6f:1.4f,v-dv,(kind==0?2.6f:1.4f)+dz,v,1,0,0,light);
        face(p,b,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,
             u,kind==0?2f:1.2f,u+du,(kind==0?2f:1.2f)+dz,0,1,0,light);
        face(p,b,x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1,
             u,kind==0?2.6f:1.4f,u+du,(kind==0?2.6f:1.4f)+dz,0,-1,0,light);
    }
    private static void face(PoseStack.Pose p,VertexConsumer b,
            float ax,float ay,float az,float bx,float by,float bz,
            float cx,float cy,float cz,float dx,float dy,float dz,
            float u0,float v0,float u1,float v1,int nx,int ny,int nz,int light) {
        // Atlas is 80x80; model UV coordinates are expressed on a 16-unit grid.
        vertex(p,b,ax,ay,az,u0/16f,v1/16f,nx,ny,nz,light);
        vertex(p,b,bx,by,bz,u0/16f,v0/16f,nx,ny,nz,light);
        vertex(p,b,cx,cy,cz,u1/16f,v0/16f,nx,ny,nz,light);
        vertex(p,b,dx,dy,dz,u1/16f,v1/16f,nx,ny,nz,light);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer b,
            float x,float y,float z,float u,float v,int nx,int ny,int nz,int light) {
        b.addVertex(p,x,y,z).setColor(-1).setUv(u,v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
}
