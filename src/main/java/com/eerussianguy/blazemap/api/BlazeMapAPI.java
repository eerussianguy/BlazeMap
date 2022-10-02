package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.pipeline.*;

public class BlazeMapAPI {
    /**
     * This registry exists for 2 reasons:
     *
     * 1) It allows us to produce keys that uniquely and unambiguously identify a specific type of MasterDatum. This
     * was not necessary in earlier versions as the only MasterData producer was the Collector, but with the addition
     * of Transformers the panorama has changed, and this is a better solution than using Collector keys in Transformer
     * code.
     *
     * 2) Since we already have a registry for MasterData, pointing at something, we might as well point it at the
     * (de)serializers instead of including (de)serialization code in objects that should only have data.
     * Now, the name DataType doesn't really say "(de)serializer", however most of the references to this will be in the
     * form of registry keys and not (de)serializer registration or invocation, and in the engine code it is a lot nicer
     * to have Key<DataType> to refer to kinds of MasterData than Key<MasterDataSerializer>, and there are a lot of them.
     *
     * Point #2 is also the reason for this registry using DataType in a raw form
     */
    @SuppressWarnings("rawtypes")
    public static final BlazeRegistry<DataType> MASTER_DATA = new BlazeRegistry<>();

    public static final BlazeRegistry<Collector<MasterDatum>> COLLECTORS = new BlazeRegistry<>();
    public static final BlazeRegistry<Transformer<MasterDatum>> TRANSFORMERS = new BlazeRegistry<>();
    public static final BlazeRegistry<Processor> PROCESSORS = new BlazeRegistry<>();
    public static final BlazeRegistry<Layer> LAYERS = new BlazeRegistry<>();

    public static final BlazeRegistry<MapType> MAPTYPES = new BlazeRegistry<>();
}
