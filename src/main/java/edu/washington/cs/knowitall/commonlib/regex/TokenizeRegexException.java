package edu.washington.cs.knowitall.commonlib.regex;

public class TokenizeRegexException extends RegexException {
    private static final long serialVersionUID = 7064825496455884721L;

    public TokenizeRegexException(String message, Exception e) {
        super(message, e);
    }
    
    public TokenizeRegexException(String message) {
        super(message);
    }
}
