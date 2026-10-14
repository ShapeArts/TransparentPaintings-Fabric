package dev.transparentpaintings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.painting.Painting;

public final class TransparentPaintingsClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide() && hand==InteractionHand.MAIN_HAND && player.isShiftKeyDown() && entity instanceof Painting painting) {
                Minecraft client=Minecraft.getInstance();
                client.execute(()->client.gui.setScreen(new FrameEditorScreen(FrameStyles.key(painting))));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
