package utils;

/**
 * Small text utilities.
 */
public class TextUtils {

    /**
     * Removes simple HTML tags from a string.
     */
    public static String stripHtmlTags(String input) {
        if (input == null) return null;
        // Remove tags like <...>
        return input.replaceAll("<[^>]*>", "");
    }
}
