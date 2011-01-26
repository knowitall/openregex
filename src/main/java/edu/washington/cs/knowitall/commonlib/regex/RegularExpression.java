package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;
import edu.washington.cs.knowitall.commonlib.regex.RegularExpression.Match.Pair;

public class RegularExpression<E> implements Predicate<List<E>> {
    List<Expression<E>> expressions;
    
    public RegularExpression(String expression, ExpressionFactory<E> factory) {
        List<String> tokens = split(expression);
        List<Expression<E>> expressions = tokenize(tokens, factory);
        
        this.expressions = expressions;
    }
    
    @Override
    public boolean apply(List<E> tokens) {
        if (this.findDetail(tokens) != null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public List<E> find(List<E> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            int j = tryRegex(this.expressions, tokens.subList(i, tokens.size()), i); 
            if (j >= 0) {
                return tokens.subList(i, j);
            }
        }
        
        return null;
    }
    
    public static class Match<E> extends ArrayList<Match.Pair<E>> {
        private static final long serialVersionUID = 1L;

        public static class Pair<E> {
            public final Expression<E> expr;
            public final List<E> tokens;
            public Pair(Expression<E> expr, E token) {
                this.expr = expr;
                
                this.tokens = new ArrayList<E>();
                this.tokens.add(token);
            }
            public Pair(Expression<E> expr, List<E> tokens) {
                this.expr = expr;
                this.tokens = new ArrayList<E>(tokens);
            }
            public Pair(Expression<E> expr) {
                this.expr = expr;
                this.tokens = new ArrayList<E>();
            }
            
            @Override
            public String toString() {
                return "{" + expr.toString() + ":'" + 
                    Joiner.on(" ").join(Lists.transform(tokens, Functions.toStringFunction())) + "'}";
            }
        }
        
        public Match() {
            super();
        }
        
        public Match(Match<E> match) {
            for (Pair<E> pair : match) {
                this.add(new Pair<E>(pair.expr, pair.tokens));
            }
        }
        
        @Override
        public String toString() {
            return "[" + Joiner.on(", ").join(Lists.transform(this, Functions.toStringFunction())) + "]";
        }
        
        public Match<E> append(Match<E> other) {
            this.addAll(other);
            return this;
        }
    }
    
    public Match<E> findDetail(List<E> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Match<E> match = tryRegexDetail(this.expressions, tokens.subList(i, tokens.size()), new Match<E>()); 
            if (match != null) {
                return match;
            }
        }
        
        return null;
    }
    
    public List<List<E>> findAll(List<E> tokens) {
        List<List<E>> results = new ArrayList<List<E>>(5);
        
        for (int i = 0; i < tokens.size(); i++) {
            int j = tryRegex(this.expressions, tokens.subList(i, tokens.size()), i); 
            if (j >= 0) {
                results.add(tokens.subList(i, j));
                i = j;
            }
        }
        
        return results;
    }
    
    private int tryRegex(List<Expression<E>> expressions, List<E> tokens, int tokenIndex) {
        if (expressions.size() == 0) {
            return tokenIndex;
        }
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option))
                {
                    return -1;
                }
            }
            
            return tokenIndex;
        }
        
        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);
        
        if (expr instanceof Expression.Star<?>) {
            Expression.Star<E> star = (Expression.Star<E>)expr;
            if (star.apply(token)) {
                int index;
                
                // consume one token
                index = tryRegex(expressions, tokens.subList(1, tokens.size()), tokenIndex + 1);
                if (index >= 0) {
                    return index;
                }
                
                // consume one token and the expression
                index = tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), tokenIndex + 1); 
                if (index >= 0) {
                    return index;
                }
            }
            
            // consume one expression
            return tryRegex(expressions.subList(1, expressions.size()), tokens, tokenIndex);
        }
        
        else if (expr instanceof Expression.Plus<?>) {
            Expression.Plus<E> star = (Expression.Plus<E>)expr;
            if (star.apply(token)) {
                int index;
                
                // consume one token
                index = tryRegex(expressions, tokens.subList(1, tokens.size()), tokenIndex + 1);
                if (index >= 0) {
                    return index;
                }
                
                // consume one token and the expression
                index = tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), tokenIndex + 1);
                if (index >= 0) {
                    return index;
                }
            }
            else {
                return -1;
            }
        }
        
        else if (expr instanceof Expression.Option<?>) {
            Expression.Option<E> option = (Expression.Option<E>)expr;
            if (option.apply(token)) {
                // consume one token and one expression
                return tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), tokenIndex + 1);
            }
            
            // consume one expression
            return tryRegex(expressions.subList(1, expressions.size()), tokens, tokenIndex);
        }
        
        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                return tryRegex(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), tokenIndex + 1);
            }
        }
        
        return -1;
    }
    
    private Match<E> tryRegexDetail(List<Expression<E>> expressions, List<E> tokens, Match<E> match) {
        if (expressions.size() == 0) {
            return match;
        }
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option))
                {
                    return null;
                }
                
                match.add(new Match.Pair<E>(expr));
            }
            
            return match;
        }
        
        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);
        
        if (expr instanceof Expression.Star<?>) {
            Expression.Star<E> star = (Expression.Star<E>)expr;
            if (star.apply(token)) {
                // consume one token
                Match<E> result = tryRegexDetail(expressions, tokens.subList(1, tokens.size()), augmentMatch(match, expr, token));
                if (result != null) {
                    return result;
                }
                
                // consume one token and the expression
                result = tryRegexDetail(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), augmentMatch(match, expr, token)); 
                if (result != null) {
                    return result;
                }
            }
            
            // consume one expression
            Match<E> result = tryRegexDetail(expressions.subList(1, expressions.size()), tokens, augmentMatch(match, expr));
            if (result != null) {
                return result;
            }
        }
        
        else if (expr instanceof Expression.Plus<?>) {
            Expression.Plus<E> star = (Expression.Plus<E>)expr;
            if (star.apply(token)) {
                Match<E> result;
                
                // consume one token
                result = tryRegexDetail(expressions, tokens.subList(1, tokens.size()), augmentMatch(match, expr, token));
                if (result != null) {
                    return result;
                }
                
                // consume one token and the expression
                result = tryRegexDetail(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), augmentMatch(match, expr, token));
                if (result != null) {
                    return result;
                }
            }
            else {
                return null;
            }
        }
        
        else if (expr instanceof Expression.Option<?>) {
            Expression.Option<E> option = (Expression.Option<E>)expr;
            if (option.apply(token)) {
                // consume one token and one expression
                Match<E> result = tryRegexDetail(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), augmentMatch(match, expr, token));
                if (result != null) {
                    return result;
                }
            }
            
            // consume one expression
            return tryRegexDetail(expressions.subList(1, expressions.size()), tokens, augmentMatch(match, expr));
        }
        
        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                return tryRegexDetail(expressions.subList(1, expressions.size()), tokens.subList(1, tokens.size()), augmentMatch(match, expr, token));
            }
        }
        
        return null;
    }
    
    private Match<E> augmentMatch(Match<E> match, Expression<E> expr, E token) {
        Match<E> newMatch = new Match<E>(match);
        
        if (newMatch.size() > 0) {
            Match.Pair<E> last = newMatch.get(newMatch.size() - 1);
            if (last.expr == expr) {
                last.tokens.add(token);
                return newMatch;
            }
        }
        
        newMatch.add(new Pair<E>(expr, token));
        return newMatch;
    }
    
    private Match<E> augmentMatch(Match<E> match, Expression<E> expr) {
        Match<E> newMatch = new Match<E>(match);
        
        if (newMatch.size() > 0) {
            Match.Pair<E> last = newMatch.get(newMatch.size() - 1);
            if (last.expr == expr) {
                return newMatch;
            }
        }
        
        newMatch.add(new Pair<E>(expr));
        return newMatch;
    }
    
    public List<Expression<E>> tokenize(List<String> tokens, ExpressionFactory<E> factory) {
        List<Expression<E>> expressions = new ArrayList<Expression<E>>();
        
        for (String token : tokens) {
            int indexOf = token.indexOf('>');
            
            BaseExpression<E> base = factory.create(token.substring(1, indexOf));
            if (token.length() > indexOf + 1) {
                char nextChar = token.charAt(indexOf + 1);
                if (nextChar == '?') {
                    expressions.add(new Expression.Option<E>(base));
                    continue;
                }
                if (nextChar == '*') {
                    expressions.add(new Expression.Star<E>(base));
                    continue;
                }
                if (nextChar == '+') {
                    expressions.add(new Expression.Plus<E>(base));
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
        
        String token;
        while (indexOf >= 0) {
            token = expression.substring(0, indexOf).trim();
            if (!token.startsWith("<")) {
                throw new IllegalArgumentException();
            }
            tokens.add(expression.substring(0, indexOf).trim());
            
            expression = expression.substring(indexOf, expression.length());
            indexOf = expression.indexOf('<', 1);
        }
        
        token = expression.trim();
        if (!token.startsWith("<")) {
            throw new IllegalArgumentException();
        }
        tokens.add(expression.trim());
        
        return tokens;
    }
}
