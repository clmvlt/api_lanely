package com.lanely.api.email.template;

/**
 * Lanely brand palette for transactional emails (single source of truth).
 * Every color used in an email MUST come from here; never hardcode a hex outside this class.
 * See CLAUDE.md > "Design des e-mails transactionnels".
 */
public final class EmailPalette {

    private EmailPalette() {
    }

    // --- Brand (blue) ---
    public static final String BRAND_50 = "#E8EFFF";
    public static final String BRAND_100 = "#C4D6FF";
    public static final String BRAND_200 = "#8FB0FF";
    public static final String BRAND_400 = "#3D7AFE";
    public static final String BRAND_500 = "#0153FD";
    public static final String BRAND_600 = "#0140C9";
    public static final String BRAND_800 = "#002E94";

    // --- Neutrals ---
    public static final String WHITE = "#FFFFFF";
    public static final String GRAY_50 = "#F7F8FA";
    public static final String GRAY_100 = "#EDEFF3";
    public static final String GRAY_200 = "#D9DDE5";
    public static final String GRAY_400 = "#9AA1AE";
    public static final String GRAY_700 = "#4A5160";
    public static final String GRAY_900 = "#161A22";

    // --- Danger ---
    public static final String DANGER = "#E23B3B";

    // --- Semantic aliases ---
    public static final String PAGE_BG = GRAY_50;
    public static final String CARD_BG = WHITE;
    public static final String CARD_BORDER = GRAY_200;
    public static final String TEXT = GRAY_900;
    public static final String TEXT_SECONDARY = GRAY_700;
    public static final String TEXT_MUTED = GRAY_400;
    public static final String PRIMARY = BRAND_500;
    public static final String PRIMARY_HOVER = BRAND_600;
    public static final String ON_PRIMARY = WHITE;
    public static final String LINK = BRAND_800;
    public static final String DIVIDER = GRAY_100;

    /** System font stack mirroring the web app (no remote webfont). */
    public static final String FONT_FAMILY =
            "-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif";
}
