package utils;

public enum ChunkStatus {
    EMPTY(0),
    GENERATING_BLOCKS(1),
    BLOCKS_READY(2),
    GENERATING_MESH(3),
    REGENERATING_MESH(4),
    READY(5);

    public int code;
    ChunkStatus(int code) {
        this.code = code;
    }
}