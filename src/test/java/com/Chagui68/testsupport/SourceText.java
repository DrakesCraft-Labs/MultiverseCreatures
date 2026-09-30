package com.Chagui68.testsupport;

/**
 * Small readers for the source-reading guards.
 *
 * <p>Two guards grew the same two helpers — "give me the body of this method" and "give me the code
 * without its comments" — and a third copy is exactly the kind of duplication that then drifts. The
 * brace counting is deliberately naive: every assertion built on it looks for text inside the body,
 * so a body that stopped early fails the assertion instead of passing silently.
 */
public final class SourceText {

    private SourceText() {
    }

    /** The body of the first method whose signature starts with {@code signature}. */
    public static String methodBody(String source, String signature) {
        int start = source.indexOf(signature);
        if (start < 0) {
            throw new AssertionError("the source no longer declares " + signature);
        }
        int open = source.indexOf('{', start);
        if (open < 0) {
            throw new AssertionError(signature + " has no body");
        }
        int depth = 0;
        for (int index = open; index < source.length(); index++) {
            char character = source.charAt(index);
            if (character == '{') depth++;
            if (character == '}') {
                depth--;
                if (depth == 0) return source.substring(open + 1, index);
            }
        }
        throw new AssertionError("the body of " + signature + " is not closed");
    }

    /** The source with comments removed, so a javadoc that merely names a type is not a guard hit. */
    public static String codeOnly(String source) {
        StringBuilder code = new StringBuilder(source.length());
        boolean inBlockComment = false;
        for (String original : source.lines().toList()) {
            String line = original;
            String trimmed = line.trim();
            if (inBlockComment) {
                int end = trimmed.indexOf("*/");
                if (end < 0) continue;
                inBlockComment = false;
                line = trimmed.substring(end + 2);
                trimmed = line.trim();
            }
            if (trimmed.startsWith("/*")) {
                inBlockComment = !trimmed.contains("*/");
                continue;
            }
            int comment = line.indexOf("//");
            if (comment >= 0) line = line.substring(0, comment);
            code.append(line).append('\n');
        }
        return code.toString();
    }
}
