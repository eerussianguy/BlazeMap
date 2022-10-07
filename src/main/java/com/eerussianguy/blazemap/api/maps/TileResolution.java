package com.eerussianguy.blazemap.api.maps;

/**
 * Used to represent a detail level to render tiles on screen.
 * For efficiency's sake (and also to display better results), maps extremely zoomed out will display only one pixel for
 * every N blocks (see pixelWidth). Instead of loading too much data and displaying it wrong, we can load just as much
 * as we need and at render time generate the proper shrunk images. This takes 33% more disk storage and resources at
 * render time.
 */
public enum TileResolution {
    FULL(1),    // zoom 1,     chunk 16, region 512, size   1 MB
    HALF(2),    // zoom 0.5,   chunk  8, region 256, size 256 KB
    QUARTER(4), // zoom 0.25,  chunk  4, region 128, size  64 KB
    EIGHTH(8);  // zoom 0.125, chunk  2, region  64, size  16 KB

    /**
     * The map zoom value associated with this resolution.
     * Smaller values mean a bigger world area on the screen.
     */
    public final double zoom;

    /**
     * Pixel width, in blocks.
     */
    public final int pixelWidth;

    /**
     * Width of a chunk, in pixels.
     */
    public final int chunkWidth;

    /**
     * Width of a region, in pixels.
     * One region is 32 x 32 chunks (1024)
     */
    public final int regionWidth;

    /**
     * Size of a region's tile on disk, in KiloBytes.
     * It is the number of pixels times 4 (each pixel is a 32bit color).
     */
    public final int regionSizeKb;

    TileResolution(int factor) {
        this.zoom = 1.0 / factor;
        this.pixelWidth = factor;
        this.chunkWidth = 16 / factor;
        this.regionWidth = chunkWidth * 32;
        this.regionSizeKb = regionWidth * regionWidth / 256;
    }

    public static TileResolution byZoom(double zoom) {
        for(TileResolution tr : values()) {
            if(tr.zoom == zoom) {
                return tr;
            }
        }
        return null;
    }
}
