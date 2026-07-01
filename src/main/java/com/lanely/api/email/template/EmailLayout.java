package com.lanely.api.email.template;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized builder for the single, shared Lanely email layout
 * (header + white card + footer) used by EVERY transactional email.
 *
 * <p>The layout owns only structure and style (per the brand charter); all human-facing text
 * is passed in already localized (resolved from the i18n bundles). Output is table-based,
 * inline-styled, Outlook-safe HTML with a hidden preheader and a bulletproof CTA button.
 *
 * <p>All caller-supplied text (titles, paragraphs, labels) is HTML-escaped, so values that may
 * embed user input (e.g. a company name) cannot inject markup.
 *
 * <p>See CLAUDE.md &gt; "Design des e-mails transactionnels".
 */
public final class EmailLayout {

    private static final String BRAND_NAME = "Lanely";

    private String lang = "en";
    private String preheader = "";
    private String title;
    private String badgeHtml;
    private final List<String> paragraphs = new ArrayList<>();
    private String note;
    private String ctaLabel;
    private String ctaUrl;
    private String footer = "";
    private String supportLabel;
    private String supportUrl;

    private EmailLayout() {
    }

    public static EmailLayout builder() {
        return new EmailLayout();
    }

    /** ISO 639-1 language code for the {@code <html lang>} attribute (e.g. "en", "fr"). */
    public EmailLayout lang(String lang) {
        if (lang != null && !lang.isBlank()) {
            this.lang = lang;
        }
        return this;
    }

    /** Hidden inbox-preview text. */
    public EmailLayout preheader(String preheader) {
        this.preheader = preheader == null ? "" : preheader;
        return this;
    }

    public EmailLayout title(String title) {
        this.title = title;
        return this;
    }

    /** Optional delivery-status badge rendered under the title. */
    public EmailLayout badge(DeliveryStatusBadge badge, String label) {
        this.badgeHtml = badge == null ? null : badge.render(label);
        return this;
    }

    /** Adds a primary body paragraph. Ignored when blank. */
    public EmailLayout addParagraph(String text) {
        if (text != null && !text.isBlank()) {
            this.paragraphs.add(text);
        }
        return this;
    }

    /** Muted secondary note shown after the call to action (e.g. "if you didn't request this..."). */
    public EmailLayout note(String note) {
        this.note = note;
        return this;
    }

    /** Primary call to action. Both label and url must be set for the button to render. */
    public EmailLayout cta(String label, String url) {
        this.ctaLabel = label;
        this.ctaUrl = url;
        return this;
    }

    public EmailLayout footer(String footer) {
        this.footer = footer == null ? "" : footer;
        return this;
    }

    public EmailLayout support(String label, String url) {
        this.supportLabel = label;
        this.supportUrl = url;
        return this;
    }

    public String render() {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("<!doctype html>\n");
        sb.append("<html lang=\"").append(escapeAttr(lang)).append("\">\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"utf-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        sb.append("<meta name=\"color-scheme\" content=\"light\">\n");
        sb.append("<!--[if mso]><style>body,table,td{font-family:Arial,Helvetica,sans-serif!important;}</style><![endif]-->\n");
        sb.append("</head>\n");
        sb.append("<body style=\"margin:0;padding:0;background:").append(EmailPalette.PAGE_BG)
                .append(";\">\n");

        // Hidden preheader (inbox preview)
        sb.append("<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;")
                .append("mso-hide:all;font-size:1px;line-height:1px;color:").append(EmailPalette.PAGE_BG)
                .append(";\">").append(escape(preheader)).append("</div>\n");

        // Canvas
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
                .append("style=\"background:").append(EmailPalette.PAGE_BG).append(";\">\n");
        sb.append("<tr><td align=\"center\" style=\"padding:32px 16px;\">\n");

        // Card
        sb.append("<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
                .append("style=\"width:480px;max-width:100%;background:").append(EmailPalette.CARD_BG)
                .append(";border:1px solid ").append(EmailPalette.CARD_BORDER)
                .append(";border-radius:10px;font-family:").append(EmailPalette.FONT_FAMILY).append(";\">\n");

        // Header (brand)
        sb.append("<tr><td style=\"padding:32px 32px 0 32px;\">")
                .append("<span style=\"font-size:18px;font-weight:700;color:").append(EmailPalette.PRIMARY)
                .append(";letter-spacing:-0.2px;\">").append(BRAND_NAME).append("</span>")
                .append("</td></tr>\n");

        // Body (title + optional badge + paragraphs)
        sb.append("<tr><td style=\"padding:24px 32px 0 32px;\">\n");
        if (title != null && !title.isBlank()) {
            sb.append("<h1 style=\"margin:0 0 12px 0;font-size:22px;font-weight:600;line-height:1.3;color:")
                    .append(EmailPalette.TEXT).append(";\">").append(escape(title)).append("</h1>\n");
        }
        if (badgeHtml != null) {
            sb.append("<div style=\"margin:0 0 16px 0;\">").append(badgeHtml).append("</div>\n");
        }
        for (String p : paragraphs) {
            sb.append("<p style=\"margin:0 0 12px 0;font-size:15px;line-height:1.6;color:")
                    .append(EmailPalette.TEXT_SECONDARY).append(";\">").append(escape(p)).append("</p>\n");
        }
        sb.append("</td></tr>\n");

        // CTA (bulletproof button with VML fallback for Outlook)
        if (ctaLabel != null && !ctaLabel.isBlank() && ctaUrl != null && !ctaUrl.isBlank()) {
            String href = escapeAttr(ctaUrl);
            sb.append("<tr><td style=\"padding:24px 32px 0 32px;\">\n");
            sb.append("<!--[if mso]>")
                    .append("<v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" ")
                    .append("href=\"").append(href).append("\" style=\"height:44px;v-text-anchor:middle;width:220px;\" ")
                    .append("arcsize=\"23%\" strokecolor=\"").append(EmailPalette.PRIMARY).append("\" fillcolor=\"")
                    .append(EmailPalette.PRIMARY).append("\">")
                    .append("<w:anchorlock/><center style=\"color:").append(EmailPalette.ON_PRIMARY)
                    .append(";font-family:").append(EmailPalette.FONT_FAMILY)
                    .append(";font-size:15px;font-weight:600;\">").append(escape(ctaLabel))
                    .append("</center></v:roundrect><![endif]-->\n");
            sb.append("<!--[if !mso]><!-->")
                    .append("<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">")
                    .append("<tr><td style=\"background:").append(EmailPalette.PRIMARY)
                    .append(";border-radius:10px;\">")
                    .append("<a href=\"").append(href).append("\" target=\"_blank\" ")
                    .append("style=\"display:inline-block;padding:12px 20px;font-size:15px;font-weight:600;color:")
                    .append(EmailPalette.ON_PRIMARY).append(";text-decoration:none;border-radius:10px;font-family:")
                    .append(EmailPalette.FONT_FAMILY).append(";\">").append(escape(ctaLabel)).append("</a>")
                    .append("</td></tr></table>")
                    .append("<!--<![endif]-->\n");
            sb.append("</td></tr>\n");
        }

        // Muted note
        if (note != null && !note.isBlank()) {
            sb.append("<tr><td style=\"padding:24px 32px 0 32px;\">")
                    .append("<p style=\"margin:0;font-size:13px;line-height:1.5;color:")
                    .append(EmailPalette.TEXT_MUTED).append(";\">").append(escape(note)).append("</p>")
                    .append("</td></tr>\n");
        }

        // Footer
        sb.append("<tr><td style=\"padding:32px;\">\n");
        sb.append("<hr style=\"border:none;border-top:1px solid ").append(EmailPalette.DIVIDER)
                .append(";margin:0 0 16px 0;\">\n");
        sb.append("<p style=\"margin:0;font-size:12px;line-height:1.5;color:").append(EmailPalette.TEXT_MUTED)
                .append(";\">").append(escape(footer));
        if (supportLabel != null && !supportLabel.isBlank() && supportUrl != null && !supportUrl.isBlank()) {
            sb.append(" &middot; <a href=\"").append(escapeAttr(supportUrl)).append("\" style=\"color:")
                    .append(EmailPalette.LINK).append(";text-decoration:underline;\">")
                    .append(escape(supportLabel)).append("</a>");
        }
        sb.append("</p>\n");
        sb.append("</td></tr>\n");

        sb.append("</table>\n");      // card
        sb.append("</td></tr>\n");
        sb.append("</table>\n");      // canvas
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    /** HTML-escapes element text content. */
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /** HTML-escapes an attribute value (e.g. an href). */
    private static String escapeAttr(String s) {
        return escape(s);
    }
}
