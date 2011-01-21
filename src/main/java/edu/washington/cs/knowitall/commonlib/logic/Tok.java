package edu.washington.cs.knowitall.commonlib.logic;

import com.google.common.base.Predicate;

public class Tok {
    public static abstract class Op<E> extends Tok {
        public static abstract class Bin<E> extends Op<E> {
            public abstract boolean apply(E entity, Tok.Arg<E> arg1,
                    Tok.Arg<E> arg2);

            public static class And<E> extends Bin<E> {
                public String toString() {
                    return "&";
                }

                @Override
                public boolean apply(E entity, Arg<E> arg1, Arg<E> arg2) {
                    return arg1.apply(entity) && arg2.apply(entity);
                }
            }

            public static class Or<E> extends Bin<E> {
                public String toString() {
                    return "|";
                }

                @Override
                public boolean apply(E entity, Arg<E> arg1, Arg<E> arg2) {
                    return arg1.apply(entity) || arg2.apply(entity);
                }
            }
        }
    }

    public static abstract class Arg<E> extends Tok {
        public abstract boolean apply(E entity);

        public static class Pred<E> extends Arg<E> {
            private String description;
            private Predicate<E> predicate;

            public Pred(String description, Predicate<E> predicate) {
                this.description = description;
                this.predicate = predicate;
            }

            public boolean apply(E entity) {
                return predicate.apply(entity);
            }
            
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
        }
    }

    public static class Paren extends Tok {
        public static class L extends Paren {
            public String toString() {
                return "(";
            }
        }

        public static class R extends Tok {
            public String toString() {
                return ")";
            }
        }
    }
}
