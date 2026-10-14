# Transparent Paintings — Fabric 26.3

**Status: experimental/unverified.** This project is an initial renderer Mixin; it may fail compilation or require further changes to match the Minecraft 26.3 renderer. A successful build does not guarantee in-game functionality.

## Intended behavior

- Alpha cutout for custom transparent painting PNGs, without visible wooden back faces.
- Vanilla paintings still work, but become flat, without wooden sides.
- Iris/shader-pack compatibility is a **test goal**, not a verified claim.
- This mod changes rendering; adding custom painting variants requires a separate painting resource pack/datapack.

## Automated build

Open **Actions** → **Build Fabric mod**. On successful completion, open its run → **Artifacts** → download the ZIP containing the compiled JAR. This requires Minecraft 26.3, Fabric Loader and a Java 25 Minecraft runtime.

## Testing

1. Test cutout painting with shaders OFF. Confirm the wall is visible through transparent pixels.
2. Inspect the art front and sides; check for inverted faces or missing front.
3. Test with Iris, then with individual shader packs (e.g. Complementary, BSL and Bliss).
4. If Gradle fails, use the first **Compile and package mod** error in Actions; do not assume the artifact is installable.

## Limitations

The renderer Mixin signatures have not been verified against a completed 26.3 compilation. This version does not implement fractional alpha blending, only binary cutout transparency. Java 25, Gradle 9.6 and Loom 1.17 follow the official Fabric 26.3 announcement.
