package edu.washington.cs.knowitall.regex;

/**
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public class RegexException extends RuntimeException {
    private static final long serialVersionUID = -3534531866062810681L;

    public RegexException(String message, Exception e) {
        super(message, e);
    }

    public RegexException(String message) {
        super(message);
    }

    public static class TokenizationRegexException extends RegexException {
        private static final long serialVersionUID = 7064825496455884721L;

        public TokenizationRegexException(String message, Exception e) {
            super(message, e);
        }

        public TokenizationRegexException(String message) {
            super(message);
        }
    }
}
