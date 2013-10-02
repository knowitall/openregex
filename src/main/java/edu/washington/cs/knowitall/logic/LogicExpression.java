package edu.washington.cs.knowitall.logic;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.google.common.base.Predicate;
import com.google.common.base.Function;

import edu.washington.cs.knowitall.logic.LogicException.ApplyLogicException;
import edu.washington.cs.knowitall.logic.LogicException.CompileLogicException;
import edu.washington.cs.knowitall.logic.LogicException.TokenizeLogicException;
import edu.washington.cs.knowitall.logic.Expression.Apply;
import edu.washington.cs.knowitall.logic.Expression.Op;
import edu.washington.cs.knowitall.logic.Expression.Arg;
import edu.washington.cs.knowitall.logic.Expression.Paren;

/**
 * A logic expression engine that operates over user specified objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 *
 * @param  <E>  the type of the base expressions
 */
abstract public class LogicExpression<E> implements Predicate<E> {
    private final Apply<E> expression;

    /***
     *
     * @param input an infix representation of the logic expression.
     * @throws TokenizeLogicException
     * @throws CompileLogicException
     */
    protected LogicExpression(String input)
            throws TokenizeLogicException, CompileLogicException {
        // convert to tokens
        List<Expression<E>> tokens = tokenize(input);

        // put in reverse polish notation
        List<Expression<E>> rpn = rpn(tokens);

        // compile the expression
        expression = compile(rpn);
    }

    /***
     * The factory method creates an argument from the supplied token string.
     * @param  argument  a string representation of a token
     * @return  an evaluatable representation of a token
     */
    public abstract Arg<E> factory(String argument);

    /***
     * The readToken method reads a token from the remaining LogicExpression string.
     *
     * This is a default implementation that may be overriden.
     * @param  remainder  the remaining text to tokenize
     * @return  a token from the beginning on `remaining`
     */
    public String readToken(String remainder) {
        Stack<Character> parens = new Stack<Character>();

        boolean quoted = false;
        char quote = ' ';
        int nextExpression;
        for (nextExpression = 1; nextExpression < remainder.length(); nextExpression++) {
            char c = remainder.charAt(nextExpression);

            if (c == '"' && (!quoted || quote == '"')) {
                quoted = !quoted;
                quote = '"';
            }
            if (c == '\'' & (!quoted || quote == '\'')) {
                quoted = !quoted;
                quote = '\'';
            }
            if (c == '/' & (!quoted || quote == '/')) {
                quoted = !quoted;
                quote = '/';
            }
            else if (quoted) {
                continue;
            }
            else if (c == '(') {
                parens.push(c);
            }
            else if (c == ')') {
                if (parens.isEmpty()) {
                    break;
                }
                else {
                    parens.pop();
                }
            }
            else if (c == '&' || c == '|') {
                break;
            }
        }

        String token = remainder.substring(0, nextExpression).trim();

        if (token.isEmpty()) {
            throw new TokenizeLogicException("zero-length token found.");
        }

        return token;
    }

    public static <E> LogicExpression<E> compile(final String input,
            final Function<String, Arg<E>> factoryDelegate) {
        return new LogicExpression<E>(input) {
            @Override
            public Arg<E> factory(String argument) {
                return factoryDelegate.apply(argument);
            }
        };
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "(empty)";
        }
        else {
            return expression.toString();
        }
    }


    /***
     * If the expression is empty, it returns true for all inputs.
     * @return true iff the expression is empty.
     */
    public boolean isEmpty() {
        return this.expression == null;
    }

    @Override
    public boolean apply(E entity) {
        if (this.isEmpty()) {
            return true;
        }
        else {
            return this.expression.apply(entity);
        }
    }

    /***
     * Compile a rpn list of tokens into an expression tree.
     * @param rpn a list of tokens in infix form.
     * @return an expression tree.
     */
    public Apply<E> compile(List<Expression<E>> rpn) {
        if (rpn.isEmpty()) {
            return null;
        }

        Stack<Apply<E>> stack = new Stack<Apply<E>>();
        for (Expression<E> tok : rpn) {
            if (tok instanceof Arg<?>) {
                stack.push((Arg<E>) tok);
            } else if (tok instanceof Op<?>) {
                try {
                    if (tok instanceof Op.Mon<?>){
                       Apply<E> sub = stack.pop();

                        Op.Mon<E> mon = (Op.Mon<E>) tok;

                        mon.sub = sub;

                        stack.push(mon);
                    }
                    if (tok instanceof Op.Bin<?>) {
                        Apply<E> arg2 = stack.pop();
                        Apply<E> arg1 = stack.pop();

                        Op.Bin<E> bin = (Op.Bin<E>) tok;

                        bin.left = arg1;
                        bin.right = arg2;

                        stack.push(bin);
                    }
                }
                catch (EmptyStackException e) {
                    throw new CompileLogicException(
                            "No argument for operator (stack empty): "
                            + tok.toString());
                }
            }
        }

        if (stack.size() > 1) {
            throw new ApplyLogicException(
                    "Stack has multiple elements after apply: " + stack.toString());
        }

        if (stack.size() == 0) {
            throw new ApplyLogicException(
                    "Stack has zero elements after apply.");
        }

        if (!(stack.peek() instanceof Apply<?>)) {
            throw new ApplyLogicException(
                    "Stack contains non-appliable tokens after apply: " + stack.toString());
        }

        return (stack.pop());
    }

    /***
     * Return a list of the arguments contained in the expression.
     * @return
     */
    public List<String> getArgs() {
        List<String> args = new ArrayList<String>();
        getArgs(this.expression, args);

        return args;
    }

    /***
     * Private helper method to recursively find arguments.
     * @param apply the expression tree to search.
     * @param args the resulting list of arguments.
     */
    private void getArgs(Apply<?> apply, List<String> args) {
        if (apply instanceof Op.Bin<?>) {
            Op.Bin<?> bin = (Op.Bin<?>) apply;

            getArgs(bin.left, args);
            getArgs(bin.right, args);
        }
        else if (apply instanceof Arg.Pred<?>) {
            args.add(((Arg.Pred<?>)apply).getDescription());
        }
    }

    /***
     * Convert an infix string logic representation to an infix list of tokens.
     * @param input an infix string logic representation.
     * @param factory a delegate that converts a string representation of an
     * argument into a token object.  @return
     *
     * @throws TokenizeLogicException
     */
    public List<Expression<E>> tokenize(String input)
    throws TokenizeLogicException {
        List<Expression<E>> tokens = new ArrayList<Expression<E>>();

        int i = 0;
        while (i < input.length()) {
            String substring = input.substring(i);
            char firstChar = substring.charAt(0);

            if (firstChar == ' ') {
                i += 1;
                continue;
            }
            else if (firstChar == '(') {
                tokens.add(new Paren.L<E>());
                i += 1;
            } else if (firstChar == ')') {
                tokens.add(new Paren.R<E>());
                i += 1;
            } else if (firstChar == '!') {
                tokens.add(new Op.Mon.Not<E>());
                i += 1;
            } else if (firstChar == '&') {
                tokens.add(new Op.Bin.And<E>());
                i += 1;
            } else if (firstChar == '|') {
                tokens.add(new Op.Bin.Or<E>());
                i += 1;
            } else {
                // parse out the token
                String token = this.readToken(substring);
                // drop those characters from the remaining text
                substring.substring(token.length());

                tokens.add(factory(token));
                i += token.length();
            }
        }

        return tokens;
    }

    /***
     * Converts an infix logic representation into a postfix logic representation.
     * @param tokens a list of tokens in infix form.
     * @return a list of tokens in postfix (rpn) form.
     * @throws CompileLogicException
     */
    public List<Expression<E>> rpn(List<Expression<E>> tokens)
            throws CompileLogicException {
        // intermediate storage
        Stack<Expression<E>> stack = new Stack<Expression<E>>();

        // final rpn output
        LinkedList<Expression<E>> output = new LinkedList<Expression<E>>();

        for (Expression<E> tok : tokens) {
            if (tok instanceof Paren.L<?>) {
                stack.push(tok);
            } else if (tok instanceof Paren.R<?>) {
                Expression<E> top;
                do {
                    top = stack.pop();

                    if (!(top instanceof Paren.L<?>)) {
                        output.offer(top);
                    }

                } while (!(top instanceof Paren.L<?>));

            } else if (tok instanceof Op.Mon<?>) {
                stack.push(tok);
            } else if (tok instanceof Op.Bin<?>) {
                // higher precedence
                while (!stack.isEmpty() && stack.peek() instanceof Op<?>
                        && ((Op<?>)stack.peek()).preceeds((Op<?>)tok)) {
                    output.offer(stack.pop());
                }

                stack.push(tok);
            } else if (tok instanceof Arg<?>) {
                output.offer(tok);
            }
        }

        // empty out items remaining ni the stack
        while (!stack.isEmpty()) {
            Expression<E> top = stack.pop();

            if (top instanceof Paren.L<?> || top instanceof Paren.R<?>) {
                throw new CompileLogicException("Unbalanced parentheses.");
            }

            output.offer(top);
        }

        return output;
    }


    /***
     * Iteractively interpret logic statements from stdin such as "true | (true & false)".
     * @param args
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            LogicExpression<String> expr = LogicExpressions.trivial(line);

            System.out.println("string: " + expr.toString());
            System.out.println("value:  " + expr.apply(null));
            System.out.println();
        }

        scan.close();
    }
}
