package edu.washington.cs.knowitall.logic;

import com.google.common.base.Predicate;

/**
 * Superclass for expressions in a Logic Expression.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public abstract class Expression<E> {
    /**
     * An expression that can be applied.
     */
    public static abstract class Apply<E> extends Expression<E> {
        /**
         * Apply this expression to an entity to get true or false.
         */
        public abstract boolean apply(E entity);
    }
    
    /**
     * An operator expression.
     */
    public static abstract class Op<E> extends Apply<E> {
        /**
         * @returns  true if this has precedence over that
         */
        public boolean preceeds(Op<?> that) {
            return this.precedence() < that.precedence();
        }
        
        /**
         * The precedence of this operator.  A smaller number denotes higher
         * precedence.
         *
         * @returns  the precedence level of this operator
         */
        public abstract int precedence();
        
        /**
         * An operator that takes a single argument, such as negation.
         */
        public static abstract class Mon<E> extends Op<E> {
            public Apply<E> sub;
            
            public String toString(String symbol) {
                if (sub == null) {
                    return symbol;
                }
                else {
                    return symbol + "(" + sub.toString() + ")";
                }
            }
            
            /**
             * The negation operator.
             */
            public static class Not<E> extends Mon<E> {
                public String toString() {
                    return super.toString("!");
                }
                
                @Override
                public boolean apply(E entity) {
                    return !sub.apply(entity);
                }

                @Override
                public int precedence() {
                    return 0;
                }
            }
        }
        
        /**
         * An operator that takes two arguments, such as disjunction.
         */
        public static abstract class Bin<E> extends Op<E> {
            public Apply<E> left;
            public Apply<E> right;
            
            public String toString(String symbol) {
                if (left == null || right == null) {
                    return symbol;
                }
                else {
                    return "(" + left.toString() + " " + symbol + " " + right.toString() + ")";
                }
            }

            /**
             * The conjunction (logical and) operator.
             */
            public static class And<E> extends Bin<E> {
                public String toString() {
                    return super.toString("&");
                }

                @Override
                public boolean apply(E entity) {
                    return left.apply(entity) && right.apply(entity);
                }

                @Override
                public int precedence() {
                    return 1;
                }
            }

            /**
             * The disjunction (logical or) operator.
             */
            public static class Or<E> extends Bin<E> {
                public String toString() {
                    return super.toString("|");
                }

                @Override
                public boolean apply(E entity) {
                    return left.apply(entity) || right.apply(entity);
                }

                @Override
                public int precedence() {
                    return 2;
                }
            }
        }
    }

    /**
     * An expression that evaluates to true or false.
     */
    public static abstract class Arg<E> extends Apply<E> implements Predicate<E> {
        /**
         * An expression that evaluates to true or false by applying a
         * predicate to the supplied entity.
         */
        public static abstract class Pred<E> extends Arg<E> {
            private String description;

            public Pred(String description) {
                this.description = description;
            }

            @Override
            public abstract boolean apply(E entity);
            
            public String getDescription() {
                return this.description;
            }

            public String toString() {
                return this.getDescription();
            }
        }

        /**
         * An expression that is a constant value--either true or false.
         */
        public static class Value<E> extends Arg<E> {
            private boolean value;

            public Value(boolean value) {
                this.value = value;
            }

            @Override
            public boolean apply(E entity) {
                return this.apply();
            }

            public boolean apply() {
                return value;
            }
            
            @Override
            public String toString() {
                return Boolean.toString(this.value);
            }
        }
    }

    /**
     * A parenthesis, used for grouping.  These are only uses prior to building
     * the AST.
     */
    public static class Paren<E> extends Expression<E> {
        /**
         * A left parenthesis.
         */
        public static class L<E> extends Paren<E> {
            public String toString() {
                return "(";
            }
        }

        /**
         * A right parenthesis.
         */
        public static class R<E> extends Paren<E> {
            public String toString() {
                return ")";
            }
        }
    }
}
