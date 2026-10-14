package dev.transparentpaintings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** A hollow, connected rectangular molding with 45-degree mitered front corners. */
public final class ClassicFrameGeometry {
    private ClassicFrameGeometry() {}
    private static final float PIXEL = 1f / 16f;
    // -Z faces toward the player, matching the 26.3 painting renderer.
    private static final float FRONT = -0.03125f - PIXEL;
    private static final float BACK = FRONT + 3f * PIXEL;

    /** Draw four connected rings (outer band, bevel, dark inner lip) and the 3D walls. */
    public static void draw(PoseStack.Pose p, VertexConsumer b, TextureAtlasSprite tex,
                            float width, float height, int borderPixels, int light) {
        final float halfW = width / 2f, halfH = height / 2f;
        final float border = borderPixels * PIXEL;
        if (border <= 0f || 2f * border >= width || 2f * border >= height) return;
        float[] inset = {0f, border * .55f, border * .80f, border};
        float[] depth = {FRONT, FRONT, FRONT + .50f * PIXEL, FRONT + PIXEL};
        int[] shade = {0xFFFFFFFF, 0xFFF0F0F0, 0xFFD4D4D4};
        for (int k = 0; k < 3; k++) {
            ring(p,b,tex,halfW,halfH,inset[k],depth[k],
                 inset[k+1],depth[k+1],light,shade[k]);
        }
        // Physical outside faces (3 px deep).
        float x0=-halfW,x1=halfW,y0=-halfH,y1=halfH;
        quad(p,b,tex, x0,y0,BACK, x0,y1,BACK, x0,y1,FRONT, x0,y0,FRONT,
             -1,0,0,light,0xFFC4C4C4);
        quad(p,b,tex, x1,y0,FRONT, x1,y1,FRONT, x1,y1,BACK, x1,y0,BACK,
             1,0,0,light,0xFFC4C4C4);
        quad(p,b,tex, x0,y1,FRONT, x0,y1,BACK, x1,y1,BACK, x1,y1,FRONT,
             0,1,0,light,0xFFE0E0E0);
        quad(p,b,tex, x0,y0,BACK, x0,y0,FRONT, x1,y0,FRONT, x1,y0,BACK,
             0,-1,0,light,0xFFAFAFAF);

        // Interior edge: a genuine recessed wall, leaving the center fully open.
        float ix0=x0+border,ix1=x1-border,iy0=y0+border,iy1=y1-border;
        float innerZ=FRONT+PIXEL;
        quad(p,b,tex, ix0,iy0,innerZ, ix0,iy1,innerZ, ix0,iy1,BACK, ix0,iy0,BACK,
             1,0,0,light,0xFF929292);
        quad(p,b,tex, ix1,iy0,BACK, ix1,iy1,BACK, ix1,iy1,innerZ, ix1,iy0,innerZ,
             -1,0,0,light,0xFF929292);
        quad(p,b,tex, ix0,iy1,BACK, ix0,iy1,innerZ, ix1,iy1,innerZ, ix1,iy1,BACK,
             0,-1,0,light,0xFF929292);
        quad(p,b,tex, ix0,iy0,innerZ, ix0,iy0,BACK, ix1,iy0,BACK, ix1,iy0,innerZ,
             0,1,0,light,0xFF929292);

        // The rear is an annulus, not a solid quad: transparent artwork stays transparent.
        ring(p,b,tex,halfW,halfH,0,BACK,border,BACK,light,0xFFB0B0B0);
    }

    /** Each band is four trapezoids with shared corner vertices: no overlapping rectangular pieces. */
    private static void ring(PoseStack.Pose p, VertexConsumer b, TextureAtlasSprite tex,
                             float hw,float hh,float a,float za,float c,float zc,
                             int light,int tint) {
        float ax=-hw+a,bx=hw-a,ay=-hh+a,by=hh-a;
        float cx=-hw+c,dx=hw-c,cy=-hh+c,dy=hh-c;
        quad(p,b,tex, ax,by,za, bx,by,za, dx,dy,zc, cx,dy,zc,
             0,0,-1,light,tint);  // top
        quad(p,b,tex, ax,ay,za, cx,cy,zc, dx,cy,zc, bx,ay,za,
             0,0,-1,light,tint);  // bottom
        quad(p,b,tex, ax,ay,za, ax,by,za, cx,dy,zc, cx,cy,zc,
             0,0,-1,light,tint);  // left
        quad(p,b,tex, dx,cy,zc, dx,dy,zc, bx,by,za, bx,ay,za,
             0,0,-1,light,tint);  // right
    }

    private static void quad(PoseStack.Pose p,VertexConsumer b,TextureAtlasSprite t,
                             float ax,float ay,float az, float bx,float by,float bz,
                             float cx,float cy,float cz, float dx,float dy,float dz,
                             int nx,int ny,int nz,int light,int color) {
        vertex(p,b,t,ax,ay,az,0,0,nx,ny,nz,light,color);
        vertex(p,b,t,bx,by,bz,0,1,nx,ny,nz,light,color);
        vertex(p,b,t,cx,cy,cz,1,1,nx,ny,nz,light,color);
        vertex(p,b,t,dx,dy,dz,1,0,nx,ny,nz,light,color);
    }
    private static void vertex(PoseStack.Pose pose,VertexConsumer buffer,TextureAtlasSprite sprite,
                               float x,float y,float z,float u,float v,
                               int nx,int ny,int nz,int light,int color) {
        buffer.addVertex(pose,x,y,z).setColor(color)
              .setUv(sprite.getU(u),sprite.getV(v))
              .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
              .setNormal(pose,nx,ny,nz);
    }
}
