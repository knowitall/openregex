package edu.washington.cs.knowitall.openregex;

public interface Function<X, Y> {
    public abstract Y apply(X input);
}
