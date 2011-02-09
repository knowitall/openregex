package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.regex.FiniteAutomaton.Automaton;
import edu.washington.cs.knowitall.commonlib.regex.FiniteAutomaton.State;

public interface Expression<E> extends Predicate<E> {
    
    public Automaton<E> build();
    
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
    
    public static class Or<E> implements Expression<E> {
        Expression<E> expr1;
        Expression<E> expr2;
        
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
        
        public Automaton<E> build() {
            Automaton<E> auto = new Automaton<E>(this);
            
            auto.start.connect(auto.end, this);
            
            return auto;
        }
    }
}
