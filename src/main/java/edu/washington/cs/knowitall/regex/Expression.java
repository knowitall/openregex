package edu.washington.cs.knowitall.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.regex.FiniteAutomaton.Automaton;
import edu.washington.cs.knowitall.regex.FiniteAutomaton.State;

/**
 * Interface for a component of a regular expression.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public interface Expression<E> extends Predicate<E> {

    public Automaton<E> build();

    /**
     * Represents a matching group that is referred to by order number.
     *     {@code (<foo> <bar>+)}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public class MatchingGroup<E> implements Expression<E> {
        public final List<Expression<E>> expressions;

        public MatchingGroup(List<Expression<E>> expressions) {
            this.expressions = expressions;
        }

        @Override
        public boolean apply(E entity) {
            throw new UnsupportedOperationException();
        }

        public String subexpString() {
            List<String> subs = new ArrayList<String>(this.expressions.size());
            for (Expression<E> expr : this.expressions) {
                subs.add(expr.toString());
            }

            return Joiner.on(" ").join(subs);
        }

        @Override
        public String toString() {
            return "(" + subexpString() + ")";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            Iterator<Expression<E>> exprIterator = this.expressions.iterator();
            Automaton<E> sub;

            // connect the start to the first subexpression
            State<E> prev = auto.start;
            if (exprIterator.hasNext()) {
                sub = exprIterator.next().build();
                auto.start.connect(sub.start);
                prev = sub.end;
            }
            while (exprIterator.hasNext()) {
                Expression<E> expr = exprIterator.next();
                sub = expr.build();

                State<E> connector = new State<E>();

                prev.connect(connector);
                connector.connect(sub.start);
                prev = sub.end;
            }

            prev.connect(auto.end);

            return auto;
        }
    }

    /**
     * Represents a matching group that is referred to by name.
     *     {@code (<name>:<foo> <bar>+)}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public class NamedGroup<E> extends MatchingGroup<E> {
        public final String name;

        public NamedGroup(String name, List<Expression<E>> expressions) {
            super(expressions);
            this.name = name;
        }

        @Override
        public String toString() {
            return "(<"+this.name+">:" + super.subexpString() + ")";
        }
    }

    /**
     * Represents a non-matching group.
     *     {@code (?:<foo> <bar>+)}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public class NonMatchingGroup<E> extends MatchingGroup<E> {
        public NonMatchingGroup(List<Expression<E>> expressions) {
            super(expressions);
        }

        @Override
        public String toString() {
            return "(?:" + super.subexpString() + ")";
        }
    }

    /**
     * Disjunction of two experssions.
     *     {@code <foo>|<bar>}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public static class Or<E> implements Expression<E> {
        public final Expression<E> expr1;
        public final Expression<E> expr2;

        public Or(Expression<E> expr1, Expression<E> expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public boolean apply(E entity) {
            return true;
        }

        @Override
        public String toString() {
            return this.expr1.toString() + " | " + this.expr2.toString();
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            Automaton<E> sub1 = this.expr1.build();
            Automaton<E> sub2 = this.expr2.build();

            // attach the sub automata
            auto.start.connect(sub1.start);
            auto.start.connect(sub2.start);
            sub1.end.connect(auto.end);
            sub2.end.connect(auto.end);

            return auto;
        }
    }

    /**
     * Kleene-star: zero or more of the enclosed expression.
     *     {@code <foo>*}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public static class Star<E> implements Expression<E> {
        public final Expression<E> expr;

        public Star(Expression<E> expr) {
            this.expr = expr;
        }

        @Override
        public boolean apply(E entity) {
            return this.expr.apply(entity);
        }

        @Override
        public String toString() {
            return this.expr.toString() + "*";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            Automaton<E> sub = this.expr.build();

            // run it again
            sub.end.connect(sub.start);

            // attach the sub automaton
            auto.start.connect(sub.start);
            sub.end.connect(auto.end);

            // skip it completely
            auto.start.connect(auto.end);

            return auto;
        }
    }

    /**
     * One or more of the enclosed expression.  Plus(expr) is equivalent to
     * expr followed by Star(expr).
     *     {@code <foo>+} is the same as {@code <foo> <foo>*}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public static class Plus<E> implements Expression<E> {
        public final Expression<E> expr;

        public Plus(Expression<E> expr) {
            this.expr = expr;
        }

        @Override
        public boolean apply(E entity) {
            return this.expr.apply(entity);
        }

        @Override
        public String toString() {
            return this.expr.toString() + "+";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            Automaton<E> sub = this.expr.build();

            // run it again
            sub.end.connect(sub.start);

            // attach the sub automaton
            auto.start.connect(sub.start);
            sub.end.connect(auto.end);

            return auto;
        }
    }

    /**
     * Zero or one of the enclosed expression.
     *     {@code <foo>?}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    public static class Option<E> implements Expression<E> {
        Expression<E> expr;

        public Option(Expression<E> expr) {
            this.expr = expr;
        }

        @Override
        public boolean apply(E entity) {
            return this.expr.apply(entity);
        }

        @Override
        public String toString() {
            return this.expr.toString() + "?";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            Automaton<E> sub = this.expr.build();

            // attach the sub automaton
            auto.start.connect(sub.start);
            sub.end.connect(auto.end);

            // skip it completely
            auto.start.connect(auto.end);

            return auto;
        }
    }

    /**
     * A minimum to maximum number of occurrences of the enclosed expression.
     *     {@code <foo>{1,3}}
     * @author Daniel Naber
     *
     * @param <E>
     */
    public static class MinMax<E> implements Expression<E> {
        Expression<E> expr;
        final int minOccurrences;
        final int maxOccurrences;

        /**
         * @param minOccurrences minimum occurrences, must be >= 0
         * @param maxOccurrences maximum occurrences, must be >= 1, you should prefer small values, as
         *                       the use of large values will create a large automaton that takes a lot of memory
         */
        public MinMax(Expression<E> expr, short minOccurrences, short maxOccurrences) {
            this.expr = expr;
            if (minOccurrences < 0 || maxOccurrences < 1) {
                throw new IllegalArgumentException("minOccurrences must be >= 0 and maxOccurrences must be >= 1: "
                        + minOccurrences + ", " + maxOccurrences);
            }
            if (minOccurrences > maxOccurrences) {
                throw new IllegalArgumentException("minOccurrences must be <= maxOccurrences: "
                        + minOccurrences + " > " + maxOccurrences);
            }
            this.minOccurrences = minOccurrences;
            this.maxOccurrences = maxOccurrences;
        }

        @Override
        public boolean apply(E entity) {
            return this.expr.apply(entity);
        }

        @Override
        public String toString() {
            return this.expr.toString() + "{" + minOccurrences + "," + maxOccurrences + "}";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            List<Automaton<E>> subAutos = new ArrayList<Automaton<E>>();
            int numberOfNodes = maxOccurrences;
            for (int i = 0; i < numberOfNodes; i++) {
                Automaton<E> sub = this.expr.build();
                subAutos.add(sub);
            }

            // attach the first sub automaton
            auto.start.connect(subAutos.get(0).start);

            // attach the sub automatons among themselves and with the end
            for (int i = 0; i < subAutos.size(); i++) {
                Automaton<E> sub = subAutos.get(i);
                if (i >= minOccurrences - 1) {
                    sub.end.connect(auto.end);
                }
                if (i < subAutos.size() - 1) {
                    Automaton<E> nextSub = subAutos.get(i + 1);
                    sub.end.connect(nextSub.start);
                }
            }

            if (minOccurrences == 0) {
                // skip it completely
                auto.start.connect(auto.end);
            }

            return auto;
        }
    }

    /**
     * An expression with no subexpression that is evaluated against a token
     * using the supplied delegate.
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    static abstract class BaseExpression<E> implements Expression<E> {
        public final String source;

        public BaseExpression(String source) {
            this.source = source;
        }

        /**
         * The delegate to evaluate the expression against a token.
         */
        @Override
        public abstract boolean apply(E entity);

        public String toString() {
            return "<" + this.source + ">";
        }

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            auto.start.connect(auto.end, this);

            return auto;
        }
    }

    /**
     * A non-consuming expression that matches a token against a property of
     * the text, such as the start or end of a line.
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    static abstract class AssertionExpression<E> implements Expression<E> {
        @Override
        public boolean apply(E entity) {
            return false;
        }

        public abstract boolean apply(boolean hasStart, List<E> tokens, int count);

        /**
         * Convert the expression into a NFA.
         */
        @Override
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);

            auto.start.connect(auto.end, this);

            return auto;
        }
    }

    /**
     * A non-consuming expression that matches the start of a line.
     *     {@code ^<foo>}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    static class StartAssertion<E> extends AssertionExpression<E> {
        @Override
        public boolean apply(boolean hasStart, List<E> tokens, int count) {
            return hasStart && tokens.size() == count;
        }

        @Override
        public String toString() {
            return "^";
        }
    }

    /**
     * A non-consuming expression that matches the end of a line.
     *     {@code <foo>$}
     * @author Michael Schmitz <schmmd@cs.washington.edu>
     *
     * @param <E>
     */
    static class EndAssertion<E> extends AssertionExpression<E> {
        @Override
        public boolean apply(boolean hasStart, List<E> tokens, int count) {
            return tokens.isEmpty();
        }

        @Override
        public String toString() {
            return "$";
        }
    }
}
