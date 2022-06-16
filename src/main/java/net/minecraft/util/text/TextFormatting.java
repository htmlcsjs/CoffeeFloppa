package net.minecraft.util.text;

public enum TextFormatting {
    DARK_GRAY('a'),
    GRAY('a'),
    AQUA('a'),
    GOLD('a'),
    DARK_PURPLE('a'),
    DARK_BLUE('a'),
    LIGHT_PURPLE('a'),
    WHITE('a'),
    DARK_AQUA('a'),
    DARK_RED('a'),
    GREEN('a'),
    DARK_GREEN('a'),
    YELLOW('a'),
    BLUE('a'),
    RED('a');

    private TextFormatting(char formattingCode) {
        this.formattingCode = formattingCode;
    }

    private final char formattingCode;

}
