package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.mutable.MutableInteger;
import edu.washington.cs.knowitall.commonlib.regex.Expression.AssertionExpression;

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
            return this.evaluate(tokens, true) != null;
        }
        
        public Match.FinalMatch<E> lookingAt(List<E> tokens) {
            return lookingAt(tokens, 0);
        }
        
        public Match.FinalMatch<E> lookingAt(List<E> tokens, int startIndex) {
            List<E> sublist = tokens.subList(startIndex, tokens.size());
            
            Step<E> path = this.evaluate(sublist, startIndex == 0);
            if (path == null) {
                return null;
            }
            
            // build list of edges
            List<AbstractEdge<E>> edges = new ArrayList<AbstractEdge<E>>();
            while (path.state != this.start) {
                edges.add(path.path);
                path = path.prev;
            }
            
            Match.IntermediateMatch<E> match = new Match.IntermediateMatch<E>();
            buildMatch(sublist.iterator(), null, new MutableInteger(startIndex), this.start, Iterables.reverse(edges).iterator(), match);
            return new Match.FinalMatch<E>(match);
        }
        
        private State<E> buildMatch(Iterator<E> tokenIterator, Expression<E> expression, MutableInteger index, State<E> state, Iterator<AbstractEdge<E>> edgeIterator, Match.IntermediateMatch<E> match) {
            Match.IntermediateMatch<E> newMatch = new Match.IntermediateMatch<E>();
            
            while (edgeIterator.hasNext() && !((state instanceof EndState<?>) && ((EndState<E>)state).expression == expression)) {
                AbstractEdge<E> edge = edgeIterator.next();
                
                // run the sub-automaton
                if (edge instanceof Edge<?> && !(((Edge<?>) edge).expression instanceof AssertionExpression<?>)) {
                    // consume a token, this is the base case
                    E token = tokenIterator.next();
                    newMatch.add(((Edge<E>)edge).expression, token, index.value());
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
                Match.Group<E> pair = new Match.Group<E>(expression);
                for (Match.Group<E> p : newMatch) {
                    if (p.expr instanceof Expression.BaseExpression<?>) {
                        pair.addTokens(p);
                    }
                }
                match.add(pair);
            }
            match.addAll(newMatch);
 
            return state;
        }
        
        private static class Step<E> {
            public final State<E> state;
            public final Step<E> prev;
            public final AbstractEdge<E> path;
            
            public Step(State<E> state) {
                this(state, null, null);
            }
            
            public Step(State<E> state, Step<E> prev, AbstractEdge<E> path) {
                this.state = state;
                this.prev = prev;
                this.path = path;
            }
            
            public String toString() {
                return this.state.toString();
            }
        }
        
        private void expandEpsilons(List<Step<E>> steps) {
            int size = steps.size();
            for (int i = 0; i < size; i++) {
                Step<E> step = steps.get(i);

                expandEpsilon(step, steps);
            }
        }
        
        private void expandEpsilon(Step<E> step, List<Step<E>> steps) {
            // loop over edges
            for (final Epsilon<E> edge : step.state.epsilons) {

                // try free edges if they do not lead to an existing
                // step
                if (!Iterables.any(steps,
                                new Predicate<Step<E>>() {
                                    @Override
                                    public boolean apply(Step<E> step) {
                                        return step.state == edge.dest;
                                    }
                                })) {
                    Step<E> newstep = new Step<E>(edge.dest, step, edge);
                    steps.add(newstep);
                    expandEpsilon(newstep, steps);
                }
            }
        }
        
        private void expandAssertions(List<Step<E>> steps, List<Step<E>> newsteps, boolean hasStart, List<E> tokens, int totalTokens) {
            for (Step<E> step : steps) {
                for (final Edge<E> edge : step.state.edges) {
                    if (edge.expression instanceof AssertionExpression) {
                        AssertionExpression<E> assertion = (AssertionExpression<E>)edge.expression;
                        
                        if (assertion.apply(hasStart, tokens, totalTokens)) {
                            newsteps.add(new Step<E>(edge.dest, step, edge));
                        }
                    }
                }
            }
        }
        
        private Step<E> evaluate(List<E> tokens, boolean hasStart) {
            List<Step<E>> steps = new ArrayList<Step<E>>();
            steps.add(new Step<E>(this.start));
            return evaluate(tokens, steps, hasStart);
        }
        
        /***
         * Use a more efficient method that keeps track of all active states.
         * @param tokens
         * @param steps
         * @return
         */
        private Step<E> evaluate(List<E> tokens, List<Step<E>> steps, boolean hasStart) {
            int totalTokens = tokens.size();
            
            int solutionTokensLeft = totalTokens;
            Step<E> solution = null;
            while (!steps.isEmpty()) {

                expandEpsilons(steps);
                
                List<Step<E>> intermediate = new ArrayList<Step<E>>(steps);
                List<Step<E>> newsteps = new ArrayList<Step<E>>(steps.size() * 2);
                do {
                    
                    // check if at end
                    for (Step<E> step : intermediate) {
                        if (step.state == this.end) {
                            if (tokens.size() == totalTokens) {
                                // can't succeed if no tokens are consumed
                            }
                            else {
                                // we have reached the end
                                if (tokens.size() < solutionTokensLeft) {
                                    solution = step;
                                    solutionTokensLeft = tokens.size();
                                }
                            }
                        }
                    }
                    
                    // handle assertions
                    newsteps.clear();
                    expandAssertions(intermediate, newsteps, hasStart, tokens, totalTokens);
                    expandEpsilons(newsteps);
                    
                    intermediate.clear();
                    intermediate.addAll(newsteps);
                    
                    steps.addAll(newsteps);
                } while (newsteps.size() > 0);

                newsteps.clear();
                if (!tokens.isEmpty()) {
                    for (Step<E> step : steps) {
                        for (final Edge<E> edge : step.state.edges) {
                            // try other edges if they match the current token
                            if (edge.apply(tokens.get(0))) {
                                newsteps.add(new Step<E>(edge.dest, step, edge));
                            }
                        }
                    }
                
                    // consume a token
                    tokens = tokens.subList(1, tokens.size());
                }
                
                steps = newsteps;
            }
            
            return solution;
        }
        
        /***
         * Bad recursive solution.
         * @param tokens
         * @param state
         * @param edges
         * @return
         */
        /*
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
        */
    }
    
    public static class State<E> {
        public final List<Edge<E>> edges = new ArrayList<Edge<E>>();
        public final List<Epsilon<E>> epsilons = new ArrayList<Epsilon<E>>();
        
        public void connect(State<E> dest) {
            this.epsilons.add(new Epsilon<E>(dest));
        }
        
        public void connect(State<E> dest, Expression<E> cost) {
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
    
    public static abstract class AbstractEdge<E> implements Predicate<E> {
        public final State<E> dest;
        
        public AbstractEdge(State<E> dest) {
            this.dest = dest;
        }
    }
    
    public static class Edge<E> extends AbstractEdge<E> {
        public final Expression<E> expression;
        
        public Edge(State<E> dest, Expression<E> base) {
            super(dest);
            this.expression = base;
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
    }
    
    public static class Epsilon<E> extends AbstractEdge<E> {
        public Epsilon(State<E> dest) {
            super(dest);
        }

        @Override
        public boolean apply(E entity) {
            return true;
        }
    }
}
