package io.github.mikip98.del.structures;

public class BlockstateWrapper {
    public String blockstateId;
    public byte defaultEmission;

    public BlockstateWrapper(String blockstateId, byte defaultEmission) {
        this.blockstateId = blockstateId;
        this.defaultEmission = defaultEmission;
    }
}
