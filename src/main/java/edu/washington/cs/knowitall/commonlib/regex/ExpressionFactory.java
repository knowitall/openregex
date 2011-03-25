package edu.washington.cs.knowitall.commonlib.regex;

import com.google.common.base.Function;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public abstract class ExpressionFactory<E> implements Function<String, BaseExpression<E>> {
    public abstract BaseExpression<E> create(String token);
    
    public BaseExpression<E> apply(String token) {
        return this.create(token);
    }
}
