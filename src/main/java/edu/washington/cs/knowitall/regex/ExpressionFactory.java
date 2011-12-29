package edu.washington.cs.knowitall.regex;

import com.google.common.base.Function;

import edu.washington.cs.knowitall.regex.Expression.BaseExpression;

/**
 * Wrapper class for a Guava Function.  Used to unpickle a expression string
 * into a part of a logic expression.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public abstract class ExpressionFactory<E> implements Function<String, BaseExpression<E>> {
    public abstract BaseExpression<E> create(String token);
    
    public BaseExpression<E> apply(String token) {
        return this.create(token);
    }
}
