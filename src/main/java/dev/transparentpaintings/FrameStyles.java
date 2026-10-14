package dev.transparentpaintings;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.decoration.painting.Painting;

/** Local client settings. Dedicated-server synchronization is intentionally not implemented yet. */
public final class FrameStyles {
    public static final String[] WOODS = {
        "No Frame", "Oak", "Spruce", "Birch", "Jungle", "Acacia", "Dark Oak",
        "Mangrove", "Cherry", "Bamboo", "Pale Oak", "Crimson", "Warped"
    };
    public record Style(int wood, int width) {
        public Style { wood = Math.max(0, Math.min(WOODS.length-1, wood)); width = Math.max(1, Math.min(6, width)); }
    }
    private static final Properties DATA = new Properties();
    private static boolean loaded;
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("transparentpaintings-frames.properties"); }
    private static void load() {
        if (loaded) return;
        loaded = true;
        if (Files.exists(file())) try (Reader r=Files.newBufferedReader(file())) { DATA.load(r); } catch(IOException ignored) { }
    }
    public static String key(Painting painting) {
        return key(painting.level().dimension().identifier().toString(), painting.getX(), painting.getY(), painting.getZ());
    }
    public static String key(String dimension,double x,double y,double z) {
        return dimension+":"+Math.floor(x)+":"+Math.floor(y)+":"+Math.floor(z);
    }
    public static Style get(String key) {
        load();
        String[] v=DATA.getProperty(key, "0,2").split(",");
        try { return new Style(Integer.parseInt(v[0]),Integer.parseInt(v[1])); }
        catch(RuntimeException e) { return new Style(0,2); }
    }
    public static void set(String key,Style style) {
        load(); DATA.setProperty(key,style.wood()+","+style.width());
        try { Files.createDirectories(file().getParent());
            try (Writer w=Files.newBufferedWriter(file())) {DATA.store(w,"Transparent Paintings frames - client local");}
        }catch(IOException ignored) {}
    }
}
