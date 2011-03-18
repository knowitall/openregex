package edu.washington.cs.knowitall.commonlib.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.StringUtils;
import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;
import edu.washington.cs.knowitall.commonlib.regex.Expression.EndAssertion;
import edu.washington.cs.knowitall.commonlib.regex.Expression.StartAssertion;
import edu.washington.cs.knowitall.commonlib.regex.FiniteAutomaton.Automaton;

public class RegularExpression<E> implements Predicate<List<E>> {
    public final List<Expression<E>> expressions;
    public final Automaton<E> auto;

    public RegularExpression(String expression, ExpressionFactory<E> factory) {
        this.expressions = tokenize(expression, factory);
        this.auto = this.build(this.expressions);
    }
    
    public Automaton<E> build(List<Expression<E>> exprs) {
        Expression.Group<E> group = new Expression.Group<E>(exprs);
        return group.build();
    }

    @Override
    public boolean apply(List<E> tokens) {
        if (this.find(tokens) != null) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * Find the first match of the regular expression against tokens. This
     * method is slightly slower due to additional memory allocations. However,
     * the response has much greater detail and is very useful for
     * writing/debugging regular expressions.
     * 
     * @param tokens
     * @return
     */
    public Match<E> find(List<E> tokens) {
        return this.find(tokens, 0);
    }
    
    public Match<E> find(List<E> tokens, int start) {
        Match<E> match;
        for (int i = start; i < tokens.size(); i++) {
            match = this.lookingAt(tokens, i);
            if (match != null) {
                return match;
            }
        }

        return null;
    }
    
    public Match<E> lookingAt(List<E> tokens) {
        return this.lookingAt(tokens, 0);
    }

    public Match<E> lookingAt(List<E> tokens, int start) {
        return auto.lookingAt(tokens, start);
    }
    
    /*
    /***
     * Find all non-overlapping matches of the regular expression against tokens.
     * 
     * @param tokens
     * @return
     */
    public List<Match<E>> findAll(List<E> tokens) {
        List<Match<E>> results = new ArrayList<Match<E>>();

        int start = 0;
        Match<E> match;
        do {
            match = this.find(tokens, start);
            
            if (match != null) {
                start = match.endIndex();
            
                // match may be empty query string has all optional parts
                if (!match.isEmpty()) {
                    results.add(match);
                }
            }
        } while (match != null);

        return results;
    }

    /***
     * Attempt to match the regular expression to the start of tokens.
     * 
     * @param expressions
     * @param tokens
     * @param tokenIndex
     * @return
     */
    private int tryRegex(List<Expression<E>> expressions, List<E> tokens,
            int tokenIndex) {
        if (expressions.size() == 0) {
            return tokenIndex;
        }
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option)) {
                    return -1;
                }
            }

            return tokenIndex;
        }

        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);

        if (expr instanceof Expression.Star<?>
                || expr instanceof Expression.Plus<?>
                || expr instanceof Expression.Option<?>) {

            if (expr.apply(token)) {
                int index;

                // * and +
                if (expr instanceof Expression.Star<?>
                        || expr instanceof Expression.Plus<?>) {
                    // consume one token
                    index = tryRegex(expressions,
                            tokens.subList(1, tokens.size()), tokenIndex + 1);
                    if (index >= 0) {
                        return index;
                    }
                }

                // consume one token and the expression
                index = tryRegex(expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), tokenIndex + 1);
                if (index >= 0) {
                    return index;
                }
            }

            // * and ?
            if (expr instanceof Expression.Star<?>
                    || expr instanceof Expression.Option<?>) {
                // consume one expression
                return tryRegex(expressions.subList(1, expressions.size()),
                        tokens, tokenIndex);
            }
        }

        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                return tryRegex(expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), tokenIndex + 1);
            }
        }

        return -1;
    }

    private boolean tryRegexDetail(List<Expression<E>> expressions,
            List<E> tokens, int index, Match<E> match) {
        // no more expressions, so we have a match
        if (expressions.size() == 0) {
            return true;
        }

        // no more tokens, match iff only optional expressions left
        if (tokens.size() == 0) {
            // makes sure the rest of the expression is option
            for (Expression<E> expr : expressions) {
                if (!(expr instanceof Expression.Star || expr instanceof Expression.Option)) {
                    return false;
                }

                match.add(new Match.Group<E>(expr));
            }

            return true;
        }

        Expression<E> expr = expressions.get(0);
        E token = tokens.get(0);

        int oldSize;

        if (expr instanceof Expression.Star<?>
                || expr instanceof Expression.Plus<?>
                || expr instanceof Expression.Option<?>) {
            if (expr.apply(token)) {

                // consume one token (* and +)
                if (expr instanceof Expression.Star<?>
                        || expr instanceof Expression.Plus<?>) {

                    oldSize = match.size();
                    match.add(new Match.Group<E>(expr, token, index));
                    if (tryRegexDetail(expressions,
                            tokens.subList(1, tokens.size()), index + 1, match)) {
                        return true;
                    }

                    match.truncate(oldSize);
                }

                // consume one token and the expression
                oldSize = match.size();
                match.add(new Match.Group<E>(expr, token, index));
                if (tryRegexDetail(expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), index + 1, match)) {
                    return true;
                }

                match.truncate(oldSize);
            }

            // consume one expression
            if (expr instanceof Expression.Star<?>
                    || expr instanceof Expression.Option<?>) {

                oldSize = match.size();
                match.add(new Match.Group<E>(expr));
                if (tryRegexDetail(expressions.subList(1, expressions.size()),
                        tokens, index, match)) {
                    return true;
                }

                match.truncate(oldSize);
            }
        }

        else if (expr instanceof Expression.BaseExpression<?>) {
            if (expr.apply(token)) {
                // consume one token and one expression
                oldSize = match.size();
                match.add(new Match.Group<E>(expr, token, index));
                return tryRegexDetail(
                        expressions.subList(1, expressions.size()),
                        tokens.subList(1, tokens.size()), index + 1, match);
            }
        }

        return false;
    }

    /***
     * Convert a list of tokens (<...>) to a list of expressions.
     * 
     * @param tokens
     * @param factory
     *            Factory class to create a BaseExpression from the text between
     *            angled brackets.
     * @return
     */
    public List<Expression<E>> tokenize(String string,
            ExpressionFactory<E> factory) {
        List<Expression<E>> expressions = new ArrayList<Expression<E>>();
        
        final Pattern whitespacePattern = Pattern.compile("\\s+");
        final Pattern unaryPattern = Pattern.compile("[*?+]");
        final Pattern binaryPattern = Pattern.compile("[|]");
        
        List<String> tokens = new ArrayList<String>();
        
        char stack = ' ';
        int start = 0;
        while (start < string.length()) {
            Matcher matcher;
            
            // skip whitespace
            if ((matcher = whitespacePattern.matcher(string)).region(start, string.length()).lookingAt()) {
                start = matcher.end();
            }
            
            // tokenize group
            char c = string.charAt(start);
            if (c == '(' || c == '<' || c == '[') {
                if (string.charAt(start) == '(') {
                    int end = StringUtils.indexOfClose(string, start, '(', ')');
                    if (end == -1) {
                        throw new TokenizeRegexException("Unclosed parenthesis at: " + start);
                    }
                    
                    String group = string.substring(start + 1, end);
                    start = end + 1;
                    
                    final Pattern namedPattern = Pattern.compile("<(\\w*)>:(.*)");
                    final Pattern unnamedPattern = Pattern.compile("\\?:(.*)");
                    
                    if ((matcher = namedPattern.matcher(group)).matches()) {
                        String groupName = matcher.group(1);
                        group = matcher.group(2);
                        List<Expression<E>> groupExpressions = this.tokenize(group,
                                factory);
                        expressions.add(new Expression.NamedGroup<E>(groupName, groupExpressions));
                    }
                    else if ((matcher = unnamedPattern.matcher(group)).matches()) {
                        group = matcher.group(1);
                        List<Expression<E>> groupExpressions = this.tokenize(group,
                                factory);
                        expressions.add(new Expression.UnnamedGroup<E>(groupExpressions));
                    }
                    else {
                        List<Expression<E>> groupExpressions = this.tokenize(group,
                                factory);
                        expressions.add(new Expression.Group<E>(groupExpressions));
                    }
                }
                else if (c == '<' || c == '[') {
                    int end;
                    if (c == '<') {
                        end = StringUtils.indexOfClose(string, start, '<', '>');
                    }
                    else if (c == '[' ){
                        end = StringUtils.indexOfClose(string, start, '[', ']');
                    }
                    else {
                        throw new IllegalStateException();
                    }
                    
                    // make sure we found the end
                    if (end == -1) {
                        throw new TokenizeRegexException("Error parsing group name.  Non-matching brackets (<>) or ([]).");
                    }
                        
                    String token = string.substring(start + 1, end);
                    try {
                        BaseExpression<E> base = factory.create(token);
                        expressions.add(base);
                        
                        start = end + 1;
                    }
                    catch (Exception e) {
                        throw new TokenizeRegexException("Error parsing token: " + token, e);
                    }
                }
                
                // check if we have a floating OR operator
                if (stack == '|') {
                    try {
                        stack = ' ';
                        if (expressions.size() < 2) {
                            throw new IllegalStateException("OR operator is applied to fewer than 2 elements.");
                        }
                        
                        Expression<E> expr1 = expressions.remove(expressions.size() - 1);
                        Expression<E> expr2 = expressions.remove(expressions.size() - 1);
                        expressions.add(new Expression.Or<E>(expr1, expr2));
                    }
                    catch (Exception e) {
                        throw new TokenizeRegexException("Error parsing OR (|) operator.", e);
                    }
                }
            }
            else if (c == '^') {
                expressions.add(new StartAssertion<E>());
                start += 1;
            }
            else if (c == '$') {
                expressions.add(new EndAssertion<E>());
                start += 1;
            }
            else if ((matcher = unaryPattern.matcher(string)).region(start, string.length()).lookingAt()) {
                char operator = matcher.group(0).charAt(0);
                
                // pop the last expression
                Expression<E> base = expressions.remove(expressions.size() - 1);
                
                // add the operator to it
                Expression<E> expr;
                if (operator == '?') {
                    expr = new Expression.Option<E>(base);
                } else if (operator == '*') {
                    expr = new Expression.Star<E>(base);
                } else if (operator == '+') {
                    expr = new Expression.Plus<E>(base);
                }
                else {
                    throw new IllegalStateException();
                }
                
                expressions.add(expr);
                
                start = matcher.end();
            }
            else if ((matcher = binaryPattern.matcher(string)).region(start, string.length()).lookingAt()) {
                tokens.add(matcher.group(0));
                stack = '|';
                start = matcher.end();
            }
            else {
                throw new IllegalArgumentException("No token found: "
                        + string.substring(start));
            }
        }
        
        if (stack == '|') {
            throw new IllegalStateException("OR remains on the stack.");
        }

        return expressions;
    }

    /***
     * Split the string into an array of regular expression tokens (<...>).
     * 
     * @param expression
     * @return
     */
    public List<String> split(String expression) {
        final Pattern tokenPattern = Pattern.compile("\\(?<.*?>\\)?[*?+]?");
        return StringUtils.splitInto(expression, tokenPattern);
    }
    
    @Override
    public boolean equals(Object other) {
        if (! (other instanceof RegularExpression)) {
            return false;
        }
        
        RegularExpression<?> expression = (RegularExpression<?>) other;
        return this.toString().equals(expression.toString());
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        List<String> expressions = new ArrayList<String>(
                this.expressions.size());
        for (Expression<E> expr : this.expressions) {
            expressions.add(expr.toString());
        }

        return Joiner.on(" ").join(expressions);
    }
    
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
                
        RegularExpression<String> regex = new RegularExpression<String>(args[0], new ExpressionFactory<String>() {
            @Override
            public BaseExpression<String> create(final String token) {
                return new BaseExpression<String>(token) {
                    private final Pattern pattern;
                    
                    {
                        pattern = Pattern.compile(token);
                    }
                        
                    @Override
                    public boolean apply(String entity) {
                        return pattern.matcher(entity).matches();
                    }};
            }});
        
        System.out.println(regex);
        
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            
            System.out.println(regex.apply(Arrays.asList(line.split("\\s+"))));
        }
    }
}
