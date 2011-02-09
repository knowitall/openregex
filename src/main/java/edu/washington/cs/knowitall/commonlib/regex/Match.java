package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.commonlib.Range;

/***
 * A class to represent a match. Each part of the regular expression is matched
 * to a sequence of tokens.
 * 
 * @author michael
 * 
 * @param <E>
 */
public class Match<E> extends ArrayList<Match.Pair<E>> {
    private static final long serialVersionUID = 1L;
    private int start;
    private int tokenCount;

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return this.getStart() + this.tokenCount;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int tokenCount() {
        return this.tokenCount;
    }

    public List<E> tokens() {
        List<E> tokens = new ArrayList<E>();
        for (Match.Pair<E> pair : this) {
            if (pair.expr instanceof Expression.BaseExpression<?>) {
                tokens.addAll(Lists.transform(pair.tokens,
                        new Function<Match.Pair.Token<E>, E>() {
                            @Override
                            public E apply(Match.Pair.Token<E> token) {
                                return token.entity;
                            }
                        }));
            }
        }

        return tokens;
    }

    public List<Pair<E>> groups() {
        List<Pair<E>> groups = new ArrayList<Pair<E>>();
        for (Pair<E> pair : this) {
            if (pair.expr instanceof Expression.Group<?>) {
                groups.add(pair);
            }
        }

        return groups;
    }

    public static class Pair<E> {
        public static class Token<E> {
            public E entity;
            public int index;

            public Token(E entity, int index) {
                this.entity = entity;
                this.index = index;
            }

            public String toString() {
                return this.entity.toString();
            }
        }

        public final Expression<E> expr;
        public final List<Token<E>> tokens;

        public Pair(Expression<E> expr, E token, int pos) {
            this(expr, Collections.singletonList(new Token<E>(token, pos)));
        }

        public Pair(Expression<E> expr, List<Token<E>> tokens) {
            this.expr = expr;
            this.tokens = new ArrayList<Token<E>>(tokens);
        }

        public Pair(Expression<E> expr) {
            this(expr, new ArrayList<Token<E>>());
        }

        public Range range() {
            Range range = Range.EMPTY;
            for (Token<E> token : this.tokens) {
                range = range.join(new Range(token.index));
            }

            return range;
        }

        public int tokenCount() {
            return this.tokens.size();
        }

        @Override
        public String toString() {
            return expr.toString()
                    + ":'"
                    + Joiner.on(" ").join(
                            Lists.transform(this.tokens,
                                    Functions.toStringFunction())) + "'";
        }
    }

    public Match() {
        super();
    }

    public Match(int size) {
        super(size);
    }

    @Override
    public boolean add(Pair<E> pair) {
        /*
         * // check if the last item is the same expression instance if
         * (this.size() > 0) { Pair<E> last = this.get(this.size() - 1); if
         * (last.expr == pair.expr) { last.parts.addAll(pair.parts); return
         * true; } }
         * 
         * this.tokenCount += pair.tokenCount;
         */
        return super.add(pair);
    }

    public boolean add(Expression<E> expr, E token, int pos) {
        return this.add(new Pair<E>(expr, token, pos));
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
                + Joiner.on(", ").join(
                        Lists.transform(this, Functions.toStringFunction()))
                + "]";
    }

    public String toPrettyString() {
        return Joiner.on("\n").join(
                Lists.transform(this, Functions.toStringFunction()));
    }
}
