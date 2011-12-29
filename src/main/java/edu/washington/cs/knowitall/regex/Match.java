package edu.washington.cs.knowitall.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;

/***
 * A class to represent a match. Each part of the regular expression is matched
 * to a sequence of tokens.   A match also stores information about the range
 * of tokens matched and the matching groups in the match.
 * 
 * @author michael
 * 
 * @param <E>
 */
public abstract class Match<E> {
    private static final long serialVersionUID = 1L;

    protected List<Match.Group<E>> pairs;
    
    protected Match() {
      pairs = new ArrayList<Match.Group<E>>();
    }

    protected Match(Match<E> match) {
        this();
        for (Group<E> pair : match.pairs) {
            this.add(new Group<E>(pair.expr, pair.tokens));
        }
    }

    public boolean add(Group<E> pair) {
        return this.pairs.add(pair);
    }

    public boolean addAll(Collection<Group<E>> pairs) {
        boolean result = true;
        for (Group<E> pair : pairs) {
            result &= this.add(pair);
        }

        return result;
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

    /***
     * True iff this match contains no pairs.  This should only happen on an
     * IntermediateMatch that has not had any pairs added to it yet.
     */
    public boolean isEmpty() {
        return this.pairs.isEmpty();
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(", ").join(
          Lists.transform(this.pairs, Functions.toStringFunction())) + "]";
    }

    public String toMultilineString() {
        return Joiner.on("\n").join(Lists.transform(this.pairs, 
          Functions.toStringFunction()));
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
     * Pairs differ from the matching groups in that each regular expression
     * element has a pair to associate the element with the text matched.
     * For example, 'a*' might be associated with 'a a a a'.
     *
     * @return all pairs in this match.
     */
    public List<Group<E>> pairs() {
        return Collections.unmodifiableList(this.pairs);
    }
    
    /***
     * @return all matching groups (named and unnamed).
     */
    public abstract List<Group<E>> groups();
    
    /***
     * @return all matched tokens.
     */
    public abstract List<E> tokens();
    
    /***
     * @return the number of tokens in the match.
     */
    public int length() {
        return this.tokens().size();
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
    protected final static class FinalMatch<E> extends Match<E> {
        private static final long serialVersionUID = 1L;

        private final int startIndex;
        private final List<E> tokens;
        private final List<Group<E>> groups;
 
        protected FinalMatch(Match<E> m) {
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
    protected final static class IntermediateMatch<E> extends Match<E> {
        private static final long serialVersionUID = 1L;
        
        protected IntermediateMatch() {
            super();
        }

        @Override
        public List<E> tokens() {
            List<E> tokens = new ArrayList<E>();
            for (Match.Group<E> pair : this.pairs) {
                if (pair.expr instanceof BaseExpression<?>) {
                    tokens.addAll(pair.tokens());
                }
            }
    
            return tokens;
        }

        @Override
        public List<Group<E>> groups() {
            List<Group<E>> groups = new ArrayList<Group<E>>();
            for (Group<E> pair : this.pairs) {
                if (pair.expr instanceof Expression.MatchingGroup<?> 
                && !(pair.expr instanceof Expression.NonMatchingGroup<?>)) {
                    groups.add(pair);
                }
            }
    
            return groups;
        }

        @Override
        public int startIndex() {
            for (Match.Group<E> pair : this.pairs) {
                if (pair.expr instanceof Expression.BaseExpression<?>) {
                    return pair.tokens.get(0).index;
                }
            }
            
            return -1;
        }
        
        @Override
        public int endIndex() {
            for (Match.Group<E> pair : Lists.reverse(this.pairs)) {
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
         * A string representation of the group.
         * This is a lighter-weight representation than toString.
         */
        public String text() {
            return Joiner.on(" ").join(this.tokens());
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
