package edu.washington.cs.knowitall.regex;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;

class RegularExpressions {
    public static RegularExpression<String> word(final String expr) {
        return RegularExpression.compile(expr, new ExpressionFactory<String>() {
            @Override public BaseExpression<String> create(final String string) {
                return new BaseExpression<String>(string) {
                    @Override public boolean apply(final String token) {
                        return string.equals(token);
                    }
                };
            }
        });
    }

    public static RegularExpression<Character> character(final String expr) {
        return RegularExpression.compile(expr, new ExpressionFactory<Character>() {
            @Override public BaseExpression<Character> create(final String string) {
                return new BaseExpression<Character>(string) {
                    @Override public boolean apply(final Character token) {
                        return string.equals(token.toString());
                    }
                };
            }
        });
    }
}
