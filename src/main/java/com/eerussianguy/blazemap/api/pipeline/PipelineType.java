package com.eerussianguy.blazemap.api.pipeline;

public enum PipelineType {
    /**
     * The server side, both dedicated and integrated.
     */
    SERVER(true, false),

    /**
     * A client connected to a server that doesn't have Blaze Map
     */
    CLIENT_STANDALONE(false, false),

    /**
     * A client connected to a server that has Blaze Map
     */
    CLIENT_AND_SERVER(false, true);


    //==================================================================================================================
    public final boolean isServer;
    public final boolean isClient;
    public final boolean hasRemote;
    public final boolean isStandalone;

    PipelineType(boolean isServer, boolean hasRemote) {
        this.isServer = isServer;
        this.isClient = !isServer;
        this.hasRemote = hasRemote;
        this.isStandalone = !hasRemote;
    }
}
