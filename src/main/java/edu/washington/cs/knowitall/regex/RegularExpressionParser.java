package edu.washington.cs.knowitall.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;
import edu.washington.cs.knowitall.regex.Expression.EndAssertion;
import edu.washington.cs.knowitall.regex.Expression.StartAssertion;
import edu.washington.cs.knowitall.regex.RegexException.TokenizationRegexException;

/**
 * A regular expression parser turns strings into RegularExpression
 * objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 *
 * @param  <E>  the type of the sequence elements
 */
public abstract class RegularExpressionParser<E> implements Function<String, RegularExpression<E>> {
    /***
     * The factory method creates an expression from the supplied token string.
     * @param  token  a string representation of a token
     * @return  an evaluatable representation of a token
     */
    public abstract BaseExpression<E> factory(String token);

    public RegularExpression<E> parse(String string) {
        List<Expression<E>> expressions = this.tokenize(string);
        return new RegularExpression<E>(expressions);
    }

    @Override
    public RegularExpression<E> apply(String string) {
        return this.parse(string);
    }

    /***
     * Read a token from the remaining text and return it.
     *
     * This is a default implementation that is overridable.
     * In the default implementation, the starting and ending
     * token characters are not escapable.
     *
     * If this implemenation is overridden, A token MUST ALWAYS
     * start with '<' or '[' and end with '>' or ']'.
     *
     * @param remaining
     * @return
     */
    public String readToken(String remaining) {
        int start = 0;
        char c = remaining.charAt(0);

        int end;
        if (c == '<') {
            end = indexOfClose(remaining, start, '<', '>');
        }
        else if (c == '[' ){
            end = indexOfClose(remaining, start, '[', ']');
        }
        else {
            throw new IllegalStateException();
        }

        // make sure we found the end
        if (end == -1) {
            throw new TokenizationRegexException(
                    "bad token. Non-matching brackets (<> or []): " + start
                    + ":\"" + remaining.substring(start) + "\"");
        }

        String token = remaining.substring(start, end + 1);
        return token;
    }

    /**
     * Convert a list of tokens (<...>) to a list of expressions.
     *
     * @param tokens
     * @param factory
     *            Factory class to create a BaseExpression from the text between
     *            angled brackets.
     * @return
     */
    public List<Expression<E>> tokenize(String string) {
        List<Expression<E>> expressions = new ArrayList<Expression<E>>();

        final Pattern whitespacePattern = Pattern.compile("\\s+");
        final Pattern unaryPattern = Pattern.compile("[*?+]");
        final Pattern minMaxPattern = Pattern.compile("\\{(\\d+),(\\d+)\\}");
        final Pattern binaryPattern = Pattern.compile("[|]");

        List<String> tokens = new ArrayList<String>();

        char stack = ' ';
        int start = 0;
        while (start < string.length()) {
            Matcher matcher;

            // skip whitespace
            if ((matcher = whitespacePattern.matcher(string))
                .region(start, string.length()).lookingAt()) {
                start = matcher.end();
                continue;
            }

            char c = string.charAt(start);
            // group, assertion, or token
            if (c == '(' || c == '<' || c == '[' || c == '$' || c == '^') {
                // group
                if (string.charAt(start) == '(') {
                    int end = indexOfClose(string, start, '(', ')');
                    if (end == -1) {
                        throw new TokenizationRegexException("unclosed parenthesis: " + start
                                + ":\"" + string.substring(start) + ")\"");
                    }

                    String group = string.substring(start + 1, end);
                    start = end + 1;

                    final Pattern namedPattern = Pattern.compile("<(\\w*)>:(.*)");
                    final Pattern unnamedPattern = Pattern.compile("\\?:(.*)");

                    // named group (matching)
                    if ((matcher = namedPattern.matcher(group)).matches()) {
                        String groupName = matcher.group(1);
                        group = matcher.group(2);
                        List<Expression<E>> groupExpressions = this.tokenize(group);
                        expressions.add(new Expression.NamedGroup<E>(groupName, groupExpressions));
                    }
                    // unnamed group
                    else if ((matcher = unnamedPattern.matcher(group)).matches()) {
                        group = matcher.group(1);
                        List<Expression<E>> groupExpressions = this.tokenize(group);
                        expressions.add(new Expression.NonMatchingGroup<E>(groupExpressions));
                    }
                    // group (matching)
                    else {
                        List<Expression<E>> groupExpressions = this.tokenize(group);
                        expressions.add(new Expression.MatchingGroup<E>(groupExpressions));
                    }
                }

                // token
                else if (c == '<' || c == '[') {
                    String token = readToken(string.substring(start));
                    try {
                        // strip off enclosing characters
                        String tokenInside = token.substring(1, token.length() - 1);
                        BaseExpression<E> base = factory(tokenInside);
                        expressions.add(base);

                        start += token.length();
                    }
                    catch (Exception e) {
                        throw new TokenizationRegexException("error parsing token: " + token, e);
                    }
                }

                // assertion (^)
                else if (c == '^') {
                    expressions.add(new StartAssertion<E>());
                    start += 1;
                }

                // assertion ($)
                else if (c == '$') {
                    expressions.add(new EndAssertion<E>());
                    start += 1;
                }

                // check if we have a floating OR operator
                if (stack == '|') {
                    try {
                        stack = ' ';
                        if (expressions.size() < 2) {
                            throw new IllegalStateException(
                                    "OR operator is applied to fewer than 2 elements.");
                        }

                        Expression<E> expr1 = expressions.remove(expressions.size() - 1);
                        Expression<E> expr2 = expressions.remove(expressions.size() - 1);
                        expressions.add(new Expression.Or<E>(expr1, expr2));
                    }
                    catch (Exception e) {
                        throw new TokenizationRegexException("error parsing OR (|) operator.", e);
                    }
                }
            }
            // unary operator
            else if ((matcher = unaryPattern.matcher(string))
                     .region(start, string.length()).lookingAt()) {
                char operator = matcher.group(0).charAt(0);

                // pop the last expression
                Expression<E> base = expressions.remove(expressions.size() - 1);

                // add the operator to it
                Expression<E> expr;
                if (operator == '?') {
                    expr = new Expression.Option<E>(base);
                } else if (operator == '*') {
                    expr = new Expression.Star<E>(base);
                } else if (operator == '+') {
                    expr = new Expression.Plus<E>(base);
                }
                else {
                    throw new IllegalStateException();
                }

                expressions.add(expr);

                start = matcher.end();
            }
            // min/max operator "{x,y}"
            else if ((matcher = minMaxPattern.matcher(string))
                    .region(start, string.length()).lookingAt()) {
                int minOccurrences = Integer.parseInt(matcher.group(1));
                int maxOccurrences = Integer.parseInt(matcher.group(2));

                // pop the last expression and add operator
                Expression<E> base = expressions.remove(expressions.size() - 1);
                Expression<E> expr = new Expression.MinMax<E>(base, minOccurrences, maxOccurrences);

                expressions.add(expr);

                start = matcher.end();
            }
            // binary operator (alternation)
            else if ((matcher = binaryPattern.matcher(string))
                     .region(start, string.length()).lookingAt()) {
                tokens.add(matcher.group(0));
                stack = '|';
                start = matcher.end();
            }
            else {
                throw new TokenizationRegexException("unknown symbol: "
                        + string.substring(start));
            }
        }

        if (stack == '|') {
            throw new TokenizationRegexException("OR remains on the stack.");
        }

        return expressions;
    }

    private static int indexOfClose(String string, int start, char open, char close) {
        start--;

        int count = 0;
        do {
            start++;

            // we hit the end
            if (start >= string.length()) {
                return -1;
            }

            char c = string.charAt(start);

            // we hit an open/close
            if (c == open) {
                count++;
            } else if (c == close) {
                count--;
            }

        } while (count > 0);

        return start;
    }
}
