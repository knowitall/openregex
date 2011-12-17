package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

public interface Expression<E> extends Predicate<E> {
    
    public class Group<E> implements Expression<E> {
        public final List<Expression<E>> expressions;

        public Group(List<Expression<E>> expressions) {
            this.expressions = expressions;
        }
        
        @Override
        public boolean apply(E entity) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String toString() {
            List<String> subs = new ArrayList<String>(this.expressions.size());
            for (Expression<E> expr : this.expressions) {
                subs.add(expr.toString());
            }
            
            return "(" + Joiner.on(" ").join(subs) + ")";
        }
    }
    
    public static class Star<E> implements Expression<E> {
        Expression<E> expr;
        
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
    }
    
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
    }
    
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
    }
    
    static abstract class BaseExpression<E> implements Expression<E> {
        private final String source;
        
        public BaseExpression(String source) {
            this.source = source;
        }

        public abstract boolean apply(E entity);
        
        public String toString() {
            if (this.source.length() > 40) {
                return "<" + this.source.substring(0, 40) + "...>";
            }
            else {
                return "<" + this.source + ">";
            }
        }
    }
}
