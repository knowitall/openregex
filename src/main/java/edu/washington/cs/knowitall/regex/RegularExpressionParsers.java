package edu.washington.cs.knowitall.regex;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;

/**
 * Static factories for regular expressions over some basic sequences.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public class RegularExpressionParsers {
    /**
     * Regular expressions over words where sequences are string
     * representations of words.
     */
    public final static RegularExpressionParser<String> word =
        new RegularExpressionParser<String>() {
            @Override public BaseExpression<String> factory(final String string) {
                return new BaseExpression<String>(string) {
                    @Override public boolean apply(final String token) {
                        return string.equals(token);
                    }
                };
            }
        };

    /**
     * Regular expression over characters, as in java.util.Regex.
     */
    public final static RegularExpressionParser<Character> character =
        new RegularExpressionParser<Character>() {
            @Override public BaseExpression<Character> factory(final String string) {
                return new BaseExpression<Character>(string) {
                    @Override public boolean apply(final Character token) {
                        return string.equals(token.toString());
                    }
                };
            }
        };
}
