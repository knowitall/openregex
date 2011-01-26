package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;
import edu.washington.cs.knowitall.commonlib.regex.RegularExpression.Match.Pair;

public class RegularExpression<E> implements Predicate<List<E>> {
    List<Expression<E>> expressions;

    public RegularExpression(String expression, ExpressionFactory<E> factory) {
        List<String> tokens = split(expression);
        List<Expression<E>> expressions = tokenize(tokens, factory);

        this.expressions = expressions;
    }

    @Override
    public boolean apply(List<E> tokens) {
        if (this.findDetail(tokens) != null) {
            return true;
        } else {
            return false;
        }
    }

    public List<E> find(List<E> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            int j = tryRegex(this.expressions,
                    tokens.subList(i, tokens.size()), i);
            if (j >= 0) {
                return tokens.subList(i, j);
            }
        }

        return null;
    }

    /***
     * Find the first match of the regular expression against tokens. This
     * method is slightly slower due to additional memory allocations. However,
     * the response has much greater detail and is very useful for
     * writing/debugging regular expressions.
     * 
     * @param tokens
     * @return
     */
    public Match<E> findDetail(List<E> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Match<E> match = tryRegexDetail(this.expressions,
                    tokens.subList(i, tokens.size()), new Match<E>());
            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /***
     * Find all matches of the regular expression against tokens.
     * 
     * @param tokens
     * @return
     */
    public List<List<E>> findAll(List<E> tokens) {
        List<List<E>> results = new ArrayList<List<E>>(5);

        for (int i = 0; i < tokens.size(); i++) {
            int j = tryRegex(this.expressions,
                    tokens.subList(i, tokens.size()), i);
            if (j >= 0) {
                results.add(tokens.subList(i, j));
                i = j;
            }
        }

        return results;
    }

    /***
     * Attempt to match the regular expression to the start of tokens.
     * 
     * @param expressions
     * @param tokens
     * @param tokenIndex
     * @return
     */
    private int tryRegex(List<Expression<E>> expressions, List<E> tokens,
            int tokenIndex) {
        if (expressions.size() == 0) {
            return tokenIndex;
        }
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option)) {
                    return -1;
                }
            }

            return tokenIndex;
        }

        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);

        if (expr instanceof Expression.Star<?>
                || expr instanceof Expression.Plus<?>
                || expr instanceof Expression.Option<?>) {

            if (expr.apply(token)) {
                int index;

                // * and +
                if (expr instanceof Expression.Star<?>
                        || expr instanceof Expression.Plus<?>) {
                    // consume one token
                    index = tryRegex(expressions,
                            tokens.subList(1, tokens.size()), tokenIndex + 1);
                    if (index >= 0) {
                        return index;
                    }
                }

                // consume one token and the expression
                index = tryRegex(expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), tokenIndex + 1);
                if (index >= 0) {
                    return index;
                }
            }

            // * and ?
            if (expr instanceof Expression.Star<?>
                    || expr instanceof Expression.Option<?>) {
                // consume one expression
                return tryRegex(expressions.subList(1, expressions.size()),
                        tokens, tokenIndex);
            }
        }

        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                return tryRegex(expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), tokenIndex + 1);
            }
        }

        return -1;
    }

    private Match<E> tryRegexDetail(List<Expression<E>> expressions,
            List<E> tokens, Match<E> match) {
        // no more expressions, so we have a match
        if (expressions.size() == 0) {
            return match;
        }

        // no more tokens, match iff only optional expressions left
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option)) {
                    return null;
                }

                match.add(new Match.Pair<E>(expr));
            }

            return match;
        }

        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);

        Match<E> result;
        int oldSize;

        if (expr instanceof Expression.Star<?>
                || expr instanceof Expression.Plus<?>
                || expr instanceof Expression.Option<?>) {
            if (expr.apply(token)) {

                // consume one token (* and +)
                if (expr instanceof Expression.Star<?>
                        || expr instanceof Expression.Plus<?>) {

                    oldSize = match.size();
                    match.add(new Pair<E>(expr, token));
                    result = tryRegexDetail(expressions,
                            tokens.subList(1, tokens.size()), match);

                    if (result != null) {
                        return result;
                    } else {
                        match.truncate(oldSize);
                    }
                }

                // consume one token and the expression
                oldSize = match.size();
                match.add(new Pair<E>(expr, token));
                result = tryRegexDetail(
                        expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), match);

                if (result != null) {
                    return result;
                } else {
                    match.truncate(oldSize);
                }
            }

            // consume one expression
            if (expr instanceof Expression.Star<?>
                    || expr instanceof Expression.Option<?>) {

                oldSize = match.size();
                match.add(new Pair<E>(expr));
                result = tryRegexDetail(
                        expressions.subList(1, expressions.size()), tokens,
                        match);

                if (result != null) {
                    return result;
                } else {
                    match.truncate(oldSize);
                }
            }
        }

        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                oldSize = match.size();
                match.add(new Pair<E>(expr, token));
                return tryRegexDetail(
                        expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), match);
            }
        }

        return null;
    }

    /***
     * Convert a list of tokens (<...>) to a list of expressions.
     * 
     * @param tokens
     * @param factory
     *            Factory class to create a BaseExpression from the text between
     *            angled brackets.
     * @return
     */
    public List<Expression<E>> tokenize(List<String> tokens,
            ExpressionFactory<E> factory) {
        List<Expression<E>> expressions = new ArrayList<Expression<E>>();

        for (String token : tokens) {
            int indexOf = token.indexOf('>');

            BaseExpression<E> base = factory
                    .create(token.substring(1, indexOf));
            if (token.length() > indexOf + 1) {
                char nextChar = token.charAt(indexOf + 1);
                if (nextChar == '?') {
                    expressions.add(new Expression.Option<E>(base));
                    continue;
                }
                if (nextChar == '*') {
                    expressions.add(new Expression.Star<E>(base));
                    continue;
                }
                if (nextChar == '+') {
                    expressions.add(new Expression.Plus<E>(base));
                    continue;
                }
            }

            expressions.add(base);
        }

        return expressions;
    }

    /***
     * Split the string into an array of regular expression tokens (<...>).
     * 
     * @param expression
     * @return
     */
    public List<String> split(String expression) {
        List<String> tokens = new ArrayList<String>();

        int indexOf = expression.indexOf('<');
        indexOf = expression.indexOf('<', indexOf + 1);

        String token;
        while (indexOf >= 0) {
            token = expression.substring(0, indexOf).trim();
            if (!token.startsWith("<")) {
                throw new IllegalArgumentException();
            }
            tokens.add(expression.substring(0, indexOf).trim());

            expression = expression.substring(indexOf, expression.length());
            indexOf = expression.indexOf('<', 1);
        }

        token = expression.trim();
        if (!token.startsWith("<")) {
            throw new IllegalArgumentException();
        }
        tokens.add(expression.trim());

        return tokens;
    }

    /***
     * A class to represent a match. Each part of the regular expression is
     * matched to a sequence of tokens.
     * 
     * @author michael
     * 
     * @param <E>
     */
    public static class Match<E> extends ArrayList<Match.Pair<E>> {
        private static final long serialVersionUID = 1L;

        public static class Pair<E> {
            public final Expression<E> expr;
            public final List<E> tokens;

            public Pair(Expression<E> expr, E token) {
                this.expr = expr;

                this.tokens = new ArrayList<E>();
                this.tokens.add(token);
            }

            public Pair(Expression<E> expr, List<E> tokens) {
                this.expr = expr;
                this.tokens = new ArrayList<E>(tokens);
            }

            public Pair(Expression<E> expr) {
                this.expr = expr;
                this.tokens = new ArrayList<E>();
            }

            @Override
            public String toString() {
                return "{"
                        + expr.toString()
                        + ":'"
                        + Joiner.on(" ").join(
                                Lists.transform(tokens,
                                        Functions.toStringFunction())) + "'}";
            }
        }

        public Match() {
            super();
        }

        public void truncate(int length) {
            while (this.size() > length) {
                this.remove(this.size() - 1);
            }
        }

        public Match(Match<E> match) {
            for (Pair<E> pair : match) {
                this.add(new Pair<E>(pair.expr, pair.tokens));
            }
        }

        @Override
        public String toString() {
            return "["
                    + Joiner.on(", ")
                            .join(Lists.transform(this,
                                    Functions.toStringFunction())) + "]";
        }
    }
}
