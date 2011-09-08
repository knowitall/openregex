package edu.washington.cs.knowitall.commonlib.logic;

import com.google.common.base.Function;

/***
 * An abstract factory class that converts the string representation of
 * an argument into a token.  This token uses the supplied delegate to
 * evaluate the expression against an entity into a boolean.
 * @author schmmd
 *
 * @param <E>
 */
public abstract class ArgFactory<E> implements Function<String, Tok.Arg<E>> {
    /***
     * Converts the supplied string into a token.
     */
    public abstract Tok.Arg<E> create(String string);
    
    /***
     * Method to satisfy abstract superclass.
     */
    @Override
    public Tok.Arg<E> apply(String string) {
        return this.create(string);
    }
}