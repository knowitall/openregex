package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

/***
 * A class to represent a match. Each part of the regular expression is matched
 * to a sequence of tokens.
 * 
 * @author michael
 * 
 * @param <E>
 */
public abstract class Match<E> extends ArrayList<Match.Group<E>> {
    private static final long serialVersionUID = 1L;
    
    public Match() {
        super();
    }

    public Match(int size) {
        super(size);
    }

    public Match(Match<E> match) {
        for (Group<E> pair : match) {
            this.add(new Group<E>(pair.expr, pair.tokens));
        }
    }

    @Override
    public boolean add(Group<E> pair) {
        return super.add(pair);
    }

    /***
     * Convenience method for add(new Group<E>(expr, token, pos)).
     * @param expr
     * @param token
     * @param pos
     * @return
     */
    public boolean add(Expression<E> expr, E token, int pos) {
        return this.add(new Group<E>(expr, token, pos));
    }

    @Override
    public String toString() {
        return "["
                + Joiner.on(", ").join(
                        Lists.transform(this, Functions.toStringFunction()))
                + "]";
    }

    public String toMultilineString() {
        return Joiner.on("\n").join(
                Lists.transform(this, Functions.toStringFunction()));
    }
    
    /***
     * @return the index of the first token matched.
     */
    public abstract int startIndex();
    
    /***
     * @return the index of the last token matched.
     */
    public abstract int endIndex();
    
    /***
     * @return all matching groups (named and unnamed).
     */
    public abstract List<Group<E>> groups();
    
    /***
     * @return all matched tokens.
     */
    public abstract List<E> tokens();
    
    /***
     * The range the match spans.
     * @return
     */
    public Range range() {
        return Range.fromInterval(this.startIndex(), this.endIndex());
    }
    
    /***
     * Retrieve a group by name.
     * @param name the name of the group to retrieve.
     * @return the associated group.
     */
    public Group<E> group(String name) {
        for (Group<E> group : this.groups()) {
            if (group.expr instanceof Expression.NamedGroup<?>) {
                Expression.NamedGroup<E> namedGroup = (Expression.NamedGroup<E>) group.expr;
                if (namedGroup.name.equals(name)) {
                    return group;
                }
            }
        }
        
        return null;
    }
    
    /***
     * A match representation that has efficient method calls but is immutable.
     * @author schmmd
     *
     * @param <E>
     */
    protected static class FinalMatch<E> extends Match<E> {
        private static final long serialVersionUID = 1L;

        private final int startIndex;
        private final List<E> tokens;
        private final List<Group<E>> groups;
 
        public FinalMatch(Match<E> m) {
            super(m);
            this.startIndex = m.startIndex();
            this.tokens = Collections.unmodifiableList(m.tokens());
            this.groups = Collections.unmodifiableList(m.groups());
        }

        public int startIndex() {
            return this.startIndex;
        }

        public int endIndex() {
            return this.startIndex() + this.tokens.size();
        }

        public List<E> tokens() {
            return this.tokens;
        }

        @Override
        public List<Match.Group<E>> groups() {
            return this.groups;
        }
    }

    /***
     * A match representation that is mutable but many method calls compute
     * values instead of returning stored values.  This is a good in-between
     * while building a match object.
     * @author schmmd
     *
     * @param <E>
     */
    protected static class IntermediateMatch<E> extends Match<E> {
        private static final long serialVersionUID = 1L;

        @Override
        public List<E> tokens() {
            List<E> tokens = new ArrayList<E>();
            for (Match.Group<E> pair : this) {
                if (pair.expr instanceof BaseExpression<?>) {
                    tokens.addAll(pair.tokens());
                }
            }
    
            return tokens;
        }

        @Override
        public List<Group<E>> groups() {
            List<Group<E>> groups = new ArrayList<Group<E>>();
            for (Group<E> pair : this) {
                if (pair.expr instanceof Expression.MatchingGroup<?> 
                && !(pair.expr instanceof Expression.NonMatchingGroup<?>)) {
                    groups.add(pair);
                }
            }
    
            return groups;
        }

        @Override
        public int startIndex() {
            for (Match.Group<E> pair : this) {
                if (pair.expr instanceof Expression.BaseExpression<?>) {
                    return pair.tokens.get(0).index;
                }
            }
            
            return -1;
        }
        
        @Override
        public int endIndex() {
            for (Match.Group<E> pair : Iterables.reverse(this)) {
                if (pair.expr instanceof Expression.BaseExpression<?>) {
                    return pair.tokens.get(0).index;
                }
            }
            
            return -1;
        }
    }

    /***
     * A captured group in a matched expression.
     * @author schmmd
     *
     * @param <E>
     */
    public static class Group<E> {
        private static class Token<E> {
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
        private final List<Token<E>> tokens;

        public Group(Expression<E> expr, E token, int pos) {
            this(expr, Collections.singletonList(new Token<E>(token, pos)));
        }

        public Group(Expression<E> expr, List<Token<E>> tokens) {
            this.expr = expr;
            this.tokens = new ArrayList<Token<E>>(tokens);
        }

        public Group(Expression<E> expr) {
            this(expr, new ArrayList<Token<E>>());
        }
        
        /***
         * Add tokens to the group.
         * @param group
         */
        protected void addTokens(Group<E> group) {
            this.tokens.addAll(group.tokens);
        }
        
        /***
         * @return the tokens matched.
         */
        public List<E> tokens() {
            return Lists.transform(this.tokens,
                    new Function<Match.Group.Token<E>, E>() {
                        @Override
                        public E apply(Match.Group.Token<E> token) {
                            return token.entity;
                        }
                    });
        }

        /***
         * @return the range of the tokens matched.
         */
        public Range range() {
            Range range = Range.EMPTY;
            for (Token<E> token : this.tokens) {
                range = range.join(new Range(token.index));
            }

            return range;
        }

        /***
         * @return the number of tokens matched.
         */
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
}
