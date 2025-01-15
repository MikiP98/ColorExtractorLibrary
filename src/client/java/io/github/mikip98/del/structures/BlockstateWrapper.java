package io.github.mikip98.del.structures;

public class BlockstateWrapper {
    public String blockstateId;
    public byte defaultEmission;
    public Double volume;

    public BlockstateWrapper(String blockstateId, byte defaultEmission, Double volume) {
        this.blockstateId = blockstateId;
        this.defaultEmission = defaultEmission;
        this.volume = volume;
    }
}
