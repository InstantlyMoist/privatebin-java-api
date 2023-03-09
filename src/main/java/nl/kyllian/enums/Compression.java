package nl.kyllian.enums;

public enum Compression {

    NONE("none"),
    ZLIB("zlib");

    private String compression;

    Compression(String compression) {
        this.compression = compression;
    }

    public String getCompression() {
        return compression;
    }
}
