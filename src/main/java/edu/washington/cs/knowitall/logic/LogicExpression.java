package edu.washington.cs.knowitall.logic;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.logic.Expression.Apply;
import edu.washington.cs.knowitall.logic.Expression.Arg;
import edu.washington.cs.knowitall.logic.Expression.Op;
import edu.washington.cs.knowitall.logic.Expression.Paren;
import edu.washington.cs.knowitall.logic.LogicException.ApplyLogicException;
import edu.washington.cs.knowitall.logic.LogicException.CompileLogicException;
import edu.washington.cs.knowitall.logic.LogicException.TokenizeLogicException;

/**
 * A logic expression engine that operates over user specified objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 *
 * @param  <E>  the type of the base expressions
 */
public class LogicExpression<E> implements Predicate<E> {
    private final Apply<E> expression;

    /***
     *
     * @param input an infix representation of the logic expression.
     * @throws TokenizeLogicException
     * @throws CompileLogicException
     */
    protected LogicExpression(List<Expression<E>> expressions)
            throws TokenizeLogicException, CompileLogicException {
        // put in reverse polish notation
        List<Expression<E>> rpn = rpn(expressions);

        // compile the expression
        expression = buildAst(rpn);
    }

    /***
     * Compile an infix list of tokens into an expression tree.
     * @param rpn a list of tokens in infix form.
     * @return an expression tree.
     */
    public static <E> LogicExpression<E> compile(
            final List<Expression<E>> expressions) {
        return new LogicExpression<E>(expressions);
    }

    /***
     * Helper factory method to instantiate a LogicExpression.
     * @param  input  The string to parse.
     * @param  factoryDelegate  The factory to build tokens.
     * @return  a new LogicExpression
     */
    public static <E> LogicExpression<E> compile(final String input,
            final Function<String, Arg<E>> factoryDelegate) {
        return new LogicExpressionParser<E>() {
            @Override
            public Arg<E> factory(String argument) {
                return factoryDelegate.apply(argument);
            }
        }.parse(input);
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
    public static <E> Apply<E> buildAst(List<Expression<E>> rpn) {
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

            LogicExpression<String> expr = LogicExpressionParsers.trivial.parse(line);

            System.out.println("string: " + expr.toString());
            System.out.println("value:  " + expr.apply(null));
            System.out.println();
        }

        scan.close();
    }
}
