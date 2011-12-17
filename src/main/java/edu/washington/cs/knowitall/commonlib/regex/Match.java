package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<E> list = new ArrayList<E>();
        for (Match.Pair<E> pair : this) {
            list.addAll(pair.tokens());
        }
        
        return list;
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

    public static abstract class Part<E> {
        public abstract Range range();
    }
    
    public static class Base<E> extends Part<E> {
        public final E token;
        public final int pos;
        
        public Base(E token, int pos) {
            this.token = token;
            this.pos = pos;
        }
        
        @Override
        public Range range() {
            return new Range(pos);
        }
        
        @Override
        public String toString() {
            return this.token.toString();
        }
    }

    public static class Pair<E> extends Part<E> {
        public final Expression<E> expr;
        public final List<Part<E>> parts;
        public final int tokenCount;
        
        public Pair(Expression<E> expr, E token, int pos) {
            this(expr, new Base<E>(token, pos));
        }

        public Pair(Expression<E> expr, Part<E> part) {
            this(expr, Collections.singletonList(part));
        }

        public Pair(Expression<E> expr, List<Part<E>> parts) {
            this.expr = expr;
            this.parts = new ArrayList<Part<E>>(parts);
            
            this.tokenCount = tokenCount(parts);
        }

        public Pair(Expression<E> expr) {
            this(expr, new ArrayList<Part<E>>());
        }
        
        @Override
        public Range range() {
            Range range = Range.EMPTY;
            for (Part<E> part : this.parts) {
                range = range.join(part.range());
            }
            
            return range;
        }
        
        private int tokenCount(final List<Part<E>> parts) {
            int count = 0;
            
            for (Part<E> part : parts) {
                if (part instanceof Base<?>) {
                    count++;
                }
                else if (part instanceof Pair<?>) {
                    count += tokenCount(((Pair<E>)part).parts);
                }
            }
            
            return count;
        }
        
        protected List<E> tokens() {
            List<E> tokens = new ArrayList<E>();
            tokens(this.parts, tokens);
            return tokens;
        }
        
        protected void tokens(final List<Part<E>> parts, final List<E> tokens) {
            for (Part<E> part : parts) {
                if (part instanceof Base<?>) {
                    tokens.add(((Base<E>)part).token);
                }
                else if (part instanceof Pair<?>) {
                    this.tokens(((Pair<E>)part).parts, tokens);
                }
            }
        }

        @Override
        public String toString() {
            return "{"
                    + expr.toString()
                    + ":'"
                    + Joiner.on(" ").join(
                            Lists.transform(parts,
                                    Functions.toStringFunction())) + "'}";
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
        // check if the last item is the same expression instance
        if (this.size() > 0) {
            Pair<E> last = this.get(this.size() - 1);
            if (last.expr == pair.expr) {
                last.parts.addAll(pair.parts);
                return true;
            }
        }
        
        this.tokenCount += pair.tokenCount;

        return super.add(pair);
    }

    public void truncate(int length) {
        while (this.size() > length) {
            this.remove(this.size() - 1);
        }
    }

    public Match(Match<E> match) {
        for (Pair<E> pair : match) {
            this.add(new Pair<E>(pair.expr, pair.parts));
        }
    }

    @Override
    public String toString() {
        return "["
                + Joiner.on(", ").join(
                        Lists.transform(this, Functions.toStringFunction()))
                + "]";
    }
}
