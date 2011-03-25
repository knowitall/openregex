package edu.washington.cs.knowitall.commonlib.regex;

public class RegexException extends RuntimeException {
    private static final long serialVersionUID = -3534531866062810681L;
    
    public RegexException(String message, Exception e) {
        super(message, e);
    }
    
    public RegexException(String message) {
        super(message);
    }
    
    public static class TokenizeRegexException extends RegexException {
        private static final long serialVersionUID = 7064825496455884721L;

        public TokenizeRegexException(String message, Exception e) {
            super(message, e);
        }
        
        public TokenizeRegexException(String message) {
            super(message);
        }
    }
}
