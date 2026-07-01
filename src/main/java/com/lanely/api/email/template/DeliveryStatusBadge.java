package com.lanely.api.email.template;

/**
 * Reusable delivery-status badge for notification emails. Color pairs are fixed by the
 * Lanely charter (see CLAUDE.md > "Design des e-mails transactionnels").
 * The label is localized by the caller and passed in; this component owns only the styling.
 */
public enum DeliveryStatusBadge {

    PENDING(EmailPalette.GRAY_400, EmailPalette.GRAY_100, EmailPalette.GRAY_700),
    COLLECTED(EmailPalette.BRAND_500, EmailPalette.BRAND_50, EmailPalette.BRAND_800),
    TRANSIT("#F59E0B", "#FEF3DC", "#854F0B"),
    DELIVERED("#16A05C", "#E3F5EC", "#0D6E3D"),
    FAILED("#E23B3B", "#FCEAEA", "#9B2727");

    private final String strong;
    private final String background;
    private final String text;

    DeliveryStatusBadge(String strong, String background, String text) {
        this.strong = strong;
        this.background = background;
        this.text = text;
    }

    public String strong() {
        return strong;
    }

    public String background() {
        return background;
    }

    public String text() {
        return text;
    }

    /**
     * Renders the badge as inline-styled HTML (dot + label). The label is HTML-escaped.
     */
    public String render(String label) {
        return "<span style=\"display:inline-block;padding:4px 10px;border-radius:6px;"
                + "font-size:13px;font-weight:600;line-height:1;background:" + background
                + ";color:" + text + ";font-family:" + EmailPalette.FONT_FAMILY + ";\">"
                + "<span style=\"color:" + strong + ";\">&#9679;</span>&nbsp;"
                + EmailLayout.escape(label)
                + "</span>";
    }
}
