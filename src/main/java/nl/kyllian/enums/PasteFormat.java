package nl.kyllian.enums;

public enum PasteFormat {

    PLAINTEXT("plaintext"),
    SYNTAX_HIGHLIGHTING("syntaxhighlighting"),
    MARKDOWN("markdown");

    private String pasteFormat;

    PasteFormat(String pasteFormat) {
        this.pasteFormat = pasteFormat;
    }

    public String getPasteFormat() {
        return pasteFormat;
    }
}
