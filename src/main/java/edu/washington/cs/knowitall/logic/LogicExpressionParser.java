package edu.washington.cs.knowitall.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.logic.Expression.Arg;
import edu.washington.cs.knowitall.logic.Expression.Op;
import edu.washington.cs.knowitall.logic.Expression.Paren;
import edu.washington.cs.knowitall.logic.LogicException.TokenizeLogicException;

/**
 * A logic expression engine that operates over user specified objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 *
 * @param  <E>  the type of the base expressions
 */
abstract public class LogicExpressionParser<E> implements Function<String, LogicExpression<E>> {
    /***
     * Create a LogicExpression object from the supplied string.
     * @param string
     * @return
     */
    public LogicExpression<E> parse(String string) {
        List<Expression<E>> expressions = this.tokenize(string);
        return new LogicExpression<E>(expressions);
    }

    @Override
    public LogicExpression<E> apply(String string) {
        return this.parse(string);
    }

    /***
     * The factory method creates an argument from the supplied token string.
     * @param  argument  a string representation of a token
     * @return  an evaluatable representation of a token
     */
    public abstract Arg<E> factory(String argument);

    public final static Pattern doubleQuoteStringLiteralRegex =
            Pattern.compile("\"" + "((?:[^\"\\p{Cntrl}\\\\]|\\\\[\\\\'\"bfnrt]|\\\\u[a-fA-F0-9]{4})*)" + "\"");
    public final static Pattern singleQuoteStringLiteralRegex =
            Pattern.compile("'" + "([^']*)" + "'");
    public final static Pattern regexLiteralRegex =
            Pattern.compile("/" + "((?:[^/\\\\]*(?:\\\\)*(?:\\\\/)*)*)" + "/");
    private final static List<Pattern> literalPatterns = Lists.newArrayList(
            doubleQuoteStringLiteralRegex, singleQuoteStringLiteralRegex,
            regexLiteralRegex);

    /***
     * The readToken method reads a token from the remaining LogicExpression string.
     *
     * A token may contain a string.  If it contains parentheses, the token
     * will last until the parentheses are balanced.  And &, |, or unbalanced )
     * will mark the end of a token.
     *
     * This is a default implementation that may be overriden.
     * @param  remainder  the remaining text to tokenize
     * @return  a token from the beginning on `remaining`
     */
    public String readToken(String remainder) {
        final String token;
        try {
            Stack<Character> parens = new Stack<Character>();

            int nextExpression;
            for (nextExpression = 0; nextExpression < remainder.length(); nextExpression++) {
                char c = remainder.charAt(nextExpression);

                // check for quotation
                String match = null;
                for (Pattern pattern : literalPatterns) {
                    Matcher matcher = pattern.matcher(remainder).region(
                            nextExpression, remainder.length());
                    if (matcher.lookingAt()) {
                        match = matcher.group(0);
                        break;
                    }
                }

                if (match != null) {
                    // we found and can consume a quotation
                    nextExpression += match.length() - 1;
                } else if (c == '(') {
                    parens.push(c);
                } else if (c == ')') {
                    if (parens.isEmpty()) {
                        break;
                    } else {
                        parens.pop();
                    }
                } else if (c == '&' || c == '|') {
                    break;
                }
            }

            token = remainder.substring(0, nextExpression).trim();
        } catch (Exception e) {
            throw new TokenizeLogicException("Error parsing token: "
                    + remainder, e);
        }

        if (token.isEmpty()) {
            throw new TokenizeLogicException("zero-length token found.");
        }

        return token;
    }

    /***
     * Convert an infix string logic representation to an infix list of tokens.
     * @param input an infix string logic representation.
     * @param factory a delegate that converts a string representation of an
     * argument into a token object.  @return
     *
     * @throws TokenizeLogicException
     */
    public List<Expression<E>> tokenize(String input)
    throws TokenizeLogicException {
        List<Expression<E>> tokens = new ArrayList<Expression<E>>();

        int i = 0;
        while (i < input.length()) {
            String substring = input.substring(i);
            char firstChar = substring.charAt(0);

            if (firstChar == ' ') {
                i += 1;
                continue;
            }
            else if (firstChar == '(') {
                tokens.add(new Paren.L<E>());
                i += 1;
            } else if (firstChar == ')') {
                tokens.add(new Paren.R<E>());
                i += 1;
            } else if (firstChar == '!') {
                tokens.add(new Op.Mon.Not<E>());
                i += 1;
            } else if (firstChar == '&') {
                tokens.add(new Op.Bin.And<E>());
                i += 1;
            } else if (firstChar == '|') {
                tokens.add(new Op.Bin.Or<E>());
                i += 1;
            } else {
                // parse out the token
                String token = this.readToken(substring);

                tokens.add(factory(token));
                i += token.length();
            }
        }

        return tokens;
    }
}
