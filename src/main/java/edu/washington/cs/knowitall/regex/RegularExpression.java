package edu.washington.cs.knowitall.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Predicate;
import com.google.common.base.Joiner;
import com.google.common.base.Function;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;
import edu.washington.cs.knowitall.regex.FiniteAutomaton.Automaton;

/**
 * A regular expression engine that operates over sequences of user-specified
 * objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 *
 * @param  <E>  the type of the sequence elements
 */
public class RegularExpression<E> implements Predicate<List<E>> {
    public final List<Expression<E>> expressions;
    public final Automaton<E> auto;

    public RegularExpression(List<Expression<E>> expressions) {
        this.expressions = expressions;
        this.auto = RegularExpression.build(this.expressions);
    }

    /***
     * Create a regular expression without tokenization support.
     * @param expressions
     * @return
     */
    public static <E> RegularExpression<E> compile(List<Expression<E>> expressions) {
        return new RegularExpression<E>(expressions);
    }

    /***
     * Create a regular expression from the specified string.
     * @param expression
     * @param factoryDelegate
     * @return
     */
    public static <E> RegularExpression<E> compile(final String expression,
            final Function<String, BaseExpression<E>> factoryDelegate) {
        return new RegularExpressionParser<E>() {
            @Override
            public BaseExpression<E> factory(String token) {
                return factoryDelegate.apply(token);
            }
        }.parse(expression);
    }

    @Override
    public boolean equals(Object other) {
        if (! (other instanceof RegularExpression<?>)) {
            return false;
        }

        RegularExpression<?> expression = (RegularExpression<?>) other;
        return this.toString().equals(expression.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        List<String> expressions = new ArrayList<String>(
                this.expressions.size());
        for (Expression<E> expr : this.expressions) {
            expressions.add(expr.toString());
        }

        return Joiner.on(" ").join(expressions);
    }

    /**
     * Build an NFA from the list of expressions.
     * @param exprs
     * @return
     */
    public static <E> Automaton<E> build(List<Expression<E>> exprs) {
        Expression.MatchingGroup<E> group = new Expression.MatchingGroup<E>(exprs);
        return group.build();
    }

    /**
     * Apply the expression against a list of tokens.
     *
     * @return true iff the expression if found within the tokens.
     */
    @Override
    public boolean apply(List<E> tokens) {
        if (this.find(tokens) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Apply the expression against a list of tokens.
     *
     * @return true iff the expression matches all of the tokens.
     */
    public boolean matches(List<E> tokens) {
        Match<E> match = this.lookingAt(tokens, 0);
        return match != null && match.endIndex() == tokens.size();
    }

    /**
     * Find the first match of the regular expression against tokens. This
     * method is slightly slower due to additional memory allocations. However,
     * the response has much greater detail and is very useful for
     * writing/debugging regular expressions.
     *
     * @param tokens
     * @return an object representing the match, or null if no match is found.
     */
    public Match<E> find(List<E> tokens) {
        return this.find(tokens, 0);
    }

    /**
     * Find the first match of the regular expression against tokens, starting
     * at the specified index.
     *
     * @param tokens tokens to match against.
     * @param start index to start looking for a match.
     * @return an object representing the match, or null if no match is found.
     */
    public Match<E> find(List<E> tokens, int start) {
        Match<E> match;
        for (int i = start; i <= tokens.size() - auto.minMatchingLength(); i++) {
            match = this.lookingAt(tokens, i);
            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /**
     * Determine if the regular expression matches the beginning of the
     * supplied tokens.
     *
     * @param tokens the list of tokens to match.
     * @return an object representing the match, or null if no match is found.
     */
    public Match<E> lookingAt(List<E> tokens) {
        return this.lookingAt(tokens, 0);
    }

    /**
     * Determine if the regular expression matches the supplied tokens,
     * starting at the specified index.
     *
     * @param tokens the list of tokens to match.
     * @param start the index where the match should begin.
     * @return an object representing the match, or null if no match is found.
     */
    public Match<E> lookingAt(List<E> tokens, int start) {
        return auto.lookingAt(tokens, start);
    }

    public Match<E> match(List<E> tokens) {
        Match<E> match = this.lookingAt(tokens);
        if (match != null && match.endIndex() == tokens.size()) {
            return match;
        }
        else {
            return null;
        }
    }

    /**
     * Find all non-overlapping matches of the regular expression against tokens.
     *
     * @param tokens
     * @return an list of objects representing the match.
     */
    public List<Match<E>> findAll(List<E> tokens) {
        List<Match<E>> results = new ArrayList<Match<E>>();

        int start = 0;
        Match<E> match;
        do {
            match = this.find(tokens, start);

            if (match != null) {
                start = match.endIndex();

                // match may be empty query string has all optional parts
                if (!match.isEmpty()) {
                    results.add(match);
                }
            }
        } while (match != null);

        return results;
    }

    /**
     * An interactive program that compiles a word-based regular expression
     * specified in arg1 and then reads strings from stdin, evaluating them
     * against the regular expression.
     * @param args
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        RegularExpression<String> regex = RegularExpressionParsers.word.parse(args[0]);
        System.out.println("regex: " + regex);
        System.out.println();

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            System.out.println("contains: " + regex.apply(Arrays.asList(line.split("\\s+"))));
            System.out.println("matches:  " + regex.matches(Arrays.asList(line.split("\\s+"))));
            System.out.println();
        }

        scan.close();
    }
}
