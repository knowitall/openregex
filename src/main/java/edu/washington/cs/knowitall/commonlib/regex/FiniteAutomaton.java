package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import Mutable.MutableInteger;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public class FiniteAutomaton {
    public static class Automaton<E> {
        public final StartState<E> start;
        public final EndState<E> end;
        
        public Automaton(StartState<E> start, EndState<E> end) {
            this.start = start;
            this.end = end;
        }
        
        public Automaton(Expression<E> expr) {
            this.start = new StartState<E>(expr);
            this.end = new EndState<E>(expr);
        }
        
        public boolean apply(List<E> tokens) {
            return this.lookingAt(tokens, this.start, null);
        }
        
        public Match.FinalMatch<E> lookingAt(List<E> tokens) {
            return lookingAt(tokens, 0);
        }
        
        public Match.FinalMatch<E> lookingAt(List<E> tokens, int startIndex) {
            List<E> sublist = tokens.subList(startIndex, tokens.size());
            Stack<Edge<E>> edges = new Stack<Edge<E>>();
            if (!this.lookingAt(sublist, this.start, edges)) {
                return null;
            }
            
            Match.IntermediateMatch<E> match = new Match.IntermediateMatch<E>();
            buildMatch(sublist.iterator(), null, new MutableInteger(startIndex), this.start, edges.iterator(), match);
            return new Match.FinalMatch<E>(match);
        }
        
        private State<E> buildMatch(Iterator<E> tokenIterator, Expression<E> expression, MutableInteger index, State<E> state, Iterator<Edge<E>> edgeIterator, Match.IntermediateMatch<E> match) {
            Match.IntermediateMatch<E> newMatch = new Match.IntermediateMatch<E>();
            
            while (edgeIterator.hasNext() && !((state instanceof EndState<?>) && ((EndState<E>)state).expression == expression)) {
                Edge<E> edge = edgeIterator.next();
                
                // run the sub-automaton
                if (edge.expression != null) {
                    // consume a token, this is the base case
                    E token = tokenIterator.next();
                    newMatch.add(edge.expression, token, index.value());
                    index.increment();
                    
                    state = edge.dest;
                }
                else if (state instanceof StartState<?>) {
                    state = buildMatch(tokenIterator, ((StartState<E>)state).expression, index, edge.dest, edgeIterator, newMatch);
                }
                else {
                    state = edge.dest;
                }
            }
            
            if (expression != null && !newMatch.isEmpty()) {
                Match.Pair<E> pair = new Match.Pair<E>(expression);
                for (Match.Pair<E> p : newMatch) {
                    if (p.expr instanceof Expression.BaseExpression<?>) {
                        pair.tokens.addAll(p.tokens);
                    }
                }
                match.add(pair);
            }
            match.addAll(newMatch);
 
            return state;
        }
        
        private boolean lookingAt(List<E> tokens, State<E> state, Stack<Edge<E>> edges) {
            // check if at end
            if (state == this.end) {
                return true;
            }
            
            // loop over edges
            for (Edge<E> edge : state.edges) {                
                // try free edges
                if (edge.isEpsilon()) {
                    if (edges != null) edges.push(edge);
                    if (lookingAt(tokens, edge.dest, edges)) {
                        return true;
                    }
                    if (edges != null) edges.pop();
                }
                // try other edges if they match the current token
                else if (tokens.size() > 0 && edge.apply(tokens.get(0))) {
                    if (edges != null) edges.push(edge);
                    if (lookingAt(tokens.subList(1, tokens.size()), edge.dest, edges)) {
                        return true;
                    }
                    if (edges != null) edges.pop();
                }
            }
            
            return false;
        }
    }
    
    public static class State<E> {
        public final List<Edge<E>> edges = new ArrayList<Edge<E>>();
        
        public void connect(State<E> dest) {
            this.edges.add(new Edge<E>(dest));
        }
        
        public void connect(State<E> dest, BaseExpression<E> cost) {
            this.edges.add(new Edge<E>(dest, cost));
        }
        
        public String toString() {
            return this.getClass().getSimpleName() + ":" + this.edges.size();
        }
    }
    
    public static class TerminusState<E> extends State<E> {
        public final Expression<E> expression;
        public TerminusState(Expression<E> expression) {
            super();
            this.expression = expression;
        }
        
        public String toString() {
            return this.getClass().getSimpleName() + "("+this.expression.toString()+"):" + this.edges.size();
        }
    }
    
    public static class StartState<E> extends TerminusState<E> {
        public StartState(Expression<E> expression) {
            super(expression);
        }
    }
    
    public static class EndState<E> extends TerminusState<E> {
        public EndState(Expression<E> expression) {
            super(expression);
        }
    }
    
    public static class Edge<E> implements Predicate<E> {
        public final BaseExpression<E> expression;
        public final State<E> dest;
        
        public Edge(State<E> dest, BaseExpression<E> base) {
            this.dest = dest;
            this.expression = base;
        }
        
        public Edge(State<E> dest) {
            this(dest, null);
        }

        @Override
        public boolean apply(E entity) {
            if (expression == null) {
                return true;
            }
            else {
                return expression.apply(entity);
            }
        }
        
        public boolean isEpsilon() {
            return this.expression == null;
        }
    }
}
