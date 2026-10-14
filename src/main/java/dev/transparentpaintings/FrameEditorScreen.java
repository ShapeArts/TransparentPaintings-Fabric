package dev.transparentpaintings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class FrameEditorScreen extends Screen {
    private final String key;
    private FrameStyles.Style style;
    private Button frameButton, widthButton;
    public FrameEditorScreen(String key) {
        super(Component.literal("Painting Frame"));
        this.key=key;
        this.style=FrameStyles.get(key);
    }
    private void change(int wood,int width) {
        style=new FrameStyles.Style(wood,width);
        FrameStyles.set(key,style);
        frameButton.setMessage(Component.literal("Frame: "+FrameStyles.WOODS[style.wood()]));
        widthButton.setMessage(Component.literal("Width: "+style.width()+" px"));
    }
    @Override protected void init() {
        int left=width/2-110, y=height/2-55;
        frameButton=addRenderableWidget(Button.builder(Component.literal("Frame: "+FrameStyles.WOODS[style.wood()]), b -> change((style.wood()+1)%FrameStyles.WOODS.length,style.width())).bounds(left,y,220,20).build());
        widthButton=addRenderableWidget(Button.builder(Component.literal("Width: "+style.width()+" px"), b -> change(style.wood(),1+style.width()%6)).bounds(left,y+30,220,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(left,y+70,220,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mx,int my,float dt) {
        super.extractRenderState(graphics,mx,my,dt);
    }
    @Override public boolean isPauseScreen() { return false; }
}
