package edu.washington.cs.knowitall.commonlib.regex;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public abstract class ExpressionFactory<E> {
    public abstract BaseExpression<E> create(String token);
}
