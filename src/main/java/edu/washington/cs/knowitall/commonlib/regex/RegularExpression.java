package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public class RegularExpression<E> implements Predicate<List<E>> {
    List<Expression<E>> expressions;
    
    public RegularExpression(String expression, ExpressionFactory<E> factory) {
        List<String> tokens = split(expression);
        List<Expression<E>> expressions = tokenize(tokens, factory);
        
        this.expressions = expressions;
    }
    
    @Override
    public boolean apply(List<E> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tryRegex(this.expressions, tokens.subList(i, tokens.size()))) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean tryRegex(List<Expression<E>> expressions, List<E> tokens) {
        if (expressions.size() == 0) {
            return true;
        }
        
        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);
        
        if (expr instanceof Expression.Star<?>) {
            Expression.Star<E> star = (Expression.Star<E>)expr;
            if (star.apply(token)) {
                // consume one token
                if (tryRegex(expressions, tokens.subList(1, tokens.size()))) {
                    return true;
                }
                
                // consume one token and the expression
                if (tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()))) {
                    return true;
                }
            }
            else {
                // consume one expression
                return tryRegex(expressions.subList(1, expressions.size()), tokens);
            }
        }
        
        else if (expr instanceof Expression.Plus<?>) {
            Expression.Plus<E> star = (Expression.Plus<E>)expr;
            if (star.apply(token)) {
                // consume one token
                if (tryRegex(expressions, tokens.subList(1, tokens.size()))) {
                    return true;
                }
                
                // consume one token and the expression
                if (tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()))) {
                    return true;
                }
            }
            else {
                return false;
            }
        }
        
        else if (expr instanceof Expression.Option<?>) {
            Expression.Option<E> option = (Expression.Option<E>)expr;
            if (option.apply(token)) {
                // consume one token
                return tryRegex(expressions, tokens.subList(1, tokens.size()));
            }
            else {
                // consume one expression
                return tryRegex(expressions.subList(1, expressions.size()), tokens);
            }
        }
        
        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                return tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()));
            }
        }
        
        return false;
    }
    
    public List<Expression<E>> tokenize(List<String> tokens, ExpressionFactory<E> factory) {
        List<Expression<E>> expressions = new ArrayList<Expression<E>>();
        
        for (String token : tokens) {
            int indexOf = token.indexOf('>');
            
            BaseExpression<E> base = factory.create(token.substring(1, indexOf));
            if (token.length() > indexOf + 1) {
                if (token.charAt(indexOf + 1) == '?') {
                    expressions.add(new Expression.Option<E>(base));
                    continue;
                }
                if (token.charAt(indexOf + 1) == '*') {
                    expressions.add(new Expression.Star<E>(base));
                    continue;
                }
            }
            
            expressions.add(base);
        }
        
        return expressions;
    }
    
    public List<String> split(String expression) {
        List<String> tokens = new ArrayList<String>();
        
        int indexOf = expression.indexOf('<');
        indexOf = expression.indexOf('<', indexOf + 1);
        
        while (indexOf >= 0) {
            tokens.add(expression.substring(0, indexOf).trim());
            
            expression = expression.substring(indexOf, expression.length());
            indexOf = expression.indexOf('<', 1);
        }
        
        tokens.add(expression.trim());
        
        return tokens;
    }
}
