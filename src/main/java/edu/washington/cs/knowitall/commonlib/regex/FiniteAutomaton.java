package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.mutable.MutableInteger;
import edu.washington.cs.knowitall.commonlib.regex.Expression.AssertionExpression;
import edu.washington.cs.knowitall.commonlib.regex.Expression.MatchingGroup;

/***
 * A finite automaton implementation.  There is support for epsilon
 * transitions (NFA) but if those are omitted then this works as an
 * implementation of a DFA.
 * @author schmmd
 *
 */
public class FiniteAutomaton {
    /***
     * A component automaton with a single start state and a single end
     * state.
     * @author schmmd
     *
     * @param <E>
     */
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
        
        /***
         * Retrace the path through the NFA and produce an object that
         * represents the match.
         * @param tokenIterator an iterator over the tokens.
         * @param expression the expression to match.
         * @param index the present index.
         * @param state the present state.
         * @param edgeIterator an iterator over the edges in the solution.
         * @param match the solution.
         * @return
         */
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
            
            if (expression != null && (!newMatch.isEmpty() || expression instanceof MatchingGroup<?>)) {
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
        
        /***
         * A representation of a movement from a state to another, with a 
         * backreference to the previous state.  This is used in building
         * a match object once a solution has been found.
         * @author schmmd
         *
         * @param <E>
         */
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
        
        /***
         * Expand all epsilon transitions for the supplied steps.  That is,
         * add all states available via an epsilon transition from a supplied
         * state to the list.
         * @param steps
         */
        private void expandEpsilons(List<Step<E>> steps) {
            int size = steps.size();
            for (int i = 0; i < size; i++) {
                Step<E> step = steps.get(i);

                expandEpsilon(step, steps);
            }
        }
        
        /***
         * Expand all epsilon transitions for the specified step.  That is,
         * add all states avaiable via an epsilon transition from step.state.
         * @param step
         * @param steps
         */
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
        
        /***
         * Expand any state that has an assertion edge if the assertion passes
         * given the present state.
         * @param steps
         * @param newsteps
         * @param hasStart true iff the tokens contains the start token.
         * @param tokens
         * @param totalTokens
         */
        private void expandAssertions(List<Step<E>> steps, List<Step<E>> newsteps, boolean hasStart, List<E> tokens, int totalTokens) {
            for (Step<E> step : steps) {
                for (final Edge<E> edge : step.state.edges) {
                    if (edge.expression instanceof AssertionExpression<?>) {
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
         * Evaluate the NFA against the list of tokens using the Thompson NFA
         * algorithm.
         * @param tokens the tokens to evaluate against
         * @param steps present list of accessible states.
         * @param hasStart true iff tokens contains the start token.
         * @return a Step object representing the last transition or null.
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
    
    /***
     * Representation of a state in the automaton.
     * @author schmmd
     *
     * @param <E>
     */
    public static class State<E> {
        public final List<Edge<E>> edges = new ArrayList<Edge<E>>();
        public final List<Epsilon<E>> epsilons = new ArrayList<Epsilon<E>>();
        
        /***
         * Add an epsilon transition between this state and dest.
         * @param dest the state to connect
         */
        public void connect(State<E> dest) {
            this.epsilons.add(new Epsilon<E>(dest));
        }
        
        /***
         * Add an edge between this state and dest.
         * @param dest the state to connect
         * @param cost the expression of the edge
         */
        public void connect(State<E> dest, Expression<E> cost) {
            this.edges.add(new Edge<E>(dest, cost));
        }
        
        public String toString() {
            return this.getClass().getSimpleName() + ":" + this.edges.size();
        }
    }
    
    /***
     * A start or end state.
     * @author schmmd
     *
     * @param <E>
     */
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
    
    /***
     * A start state.
     * @author schmmd
     *
     * @param <E>
     */
    public static class StartState<E> extends TerminusState<E> {
        public StartState(Expression<E> expression) {
            super(expression);
        }
    }
    
    /***
     * An end state.
     * @author schmmd
     *
     * @param <E>
     */
    public static class EndState<E> extends TerminusState<E> {
        public EndState(Expression<E> expression) {
            super(expression);
        }
    }
    
    /***
     * An abstract representation of an edge.
     * @author schmmd
     *
     * @param <E>
     */
    public static abstract class AbstractEdge<E> implements Predicate<E> {
        public final State<E> dest;
        
        public AbstractEdge(State<E> dest) {
            this.dest = dest;
        }
    }
    
    /***
     * An edge with cost {@code expression}.
     * @author schmmd
     *
     * @param <E>
     */
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
    
    /***
     * An edge without cost, an epsilon transition.
     * @author schmmd
     *
     * @param <E>
     */
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
