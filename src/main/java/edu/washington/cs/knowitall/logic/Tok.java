package edu.washington.cs.knowitall.commonlib.logic;

import com.google.common.base.Predicate;

public class Tok<E> {
    public static abstract class Apply<E> extends Tok<E> {
        public abstract boolean apply(E entity);
    }
    
    public static abstract class Op<E> extends Apply<E> {
        public boolean preceeds(Op<?> other) {
            return this.precedence() < other.precedence();
        }
        
        public abstract int precedence();
        
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

    public static abstract class Arg<E> extends Apply<E> implements Predicate<E> {
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

    public static class Paren<E> extends Tok<E> {
        public static class L<E> extends Paren<E> {
            public String toString() {
                return "(";
            }
        }

        public static class R<E> extends Paren<E> {
            public String toString() {
                return ")";
            }
        }
    }
}
