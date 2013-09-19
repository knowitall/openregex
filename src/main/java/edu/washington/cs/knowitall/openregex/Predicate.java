package edu.washington.cs.knowitall.openregex;

public interface Predicate<X> {
    boolean apply(X input);
}
