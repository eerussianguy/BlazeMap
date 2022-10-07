package com.eerussianguy.blazemap.api.markers;

/**
 * Determines if our object is the target of a search.
 */
public enum SearchTargeting {
    /**
     * No search active
     */
    NONE,

    /**
     * Search active, and our object is a result
     */
    HIT,

    /**
     * Search active, and our object is not a result
     */
    MISS;

    /**
     * Helper function to override the render color in fuction of search status
     */
    public int color(int color){
        return this == MISS ? 0x80808080 : color;
    }

    public int color(){
        return color(-1);
    }
}
