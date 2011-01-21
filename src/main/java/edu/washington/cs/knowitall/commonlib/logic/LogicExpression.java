package edu.washington.cs.knowitall.commonlib.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class LogicExpression<E> implements Predicate<E> {
    public static class LogicException extends Exception {
        private static final long serialVersionUID = 1L;

        public LogicException(String message) {
            super(message);
        }
    }

    public static class ApplyLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public ApplyLogicException(String message) {
            super(message);
        }
    }

    public static class CompileLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public CompileLogicException(String message) {
            super(message);
        }
    }

    public static class TokenizeLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public TokenizeLogicException(String message) {
            super(message);
        }
    }

    final List<Tok> expression;

    public LogicExpression(String input, ArgFactory<E> factory)
            throws CompileLogicException, TokenizeLogicException {
        List<Tok> tokens = tokenize(input, factory);
        expression = compile(tokens);
    }

    @SuppressWarnings("unchecked")
    public boolean apply(E target) {
        Stack<Tok> stack = new Stack<Tok>();
        for (Tok tok : expression) {
            if (tok instanceof Tok.Arg<?>) {
                stack.push(tok);
            } else if (tok instanceof Tok.Op) {
                if (tok instanceof Tok.Op.Bin) {
                    Tok.Arg<E> arg1 = (Tok.Arg<E>) stack.pop();
                    Tok.Arg<E> arg2 = (Tok.Arg<E>) stack.pop();
                    stack.push(new Tok.Arg.Value<E>(((Tok.Op.Bin<E>) tok)
                            .apply(target, arg1, arg2)));
                }
            }
        }

        /*
        if (stack.size() > 1) {
            throw new ApplyLogicException(
                    "Stack has multiple elements after apply.");
        }

        if (stack.size() == 0) {
            throw new ApplyLogicException(
                    "Stack has zero elements after apply.");
        }

        if (!(stack.peek() instanceof Tok.Arg<?>)) {
            throw new ApplyLogicException(
                    "Stack contains non-argument after apply.");
        }
        */

        return ((Tok.Arg<E>) stack.pop()).apply(target);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getArgs() {
        List<String> output = new ArrayList<String>(expression.size());
        for (Tok tok : expression) {
            if (tok instanceof Tok.Arg.Pred<?>) {
                output.add(((Tok.Arg.Pred<E>)tok).getDescription());
            }
        }
        
        return output;
    }

    public String toString() {
        return Joiner.on(", ").join(
                Iterables.transform(expression, new Function<Tok, String>() {
                    @Override
                    public String apply(Tok tok) {
                        return tok.toString();
                    }
                }));
    }

    public List<Tok> tokenize(String input, ArgFactory<E> factory) throws TokenizeLogicException {
        List<Tok> tokens = new ArrayList<Tok>();

        int i = 0;
        while (i < input.length()) {
            String substring = input.substring(i);
            if (substring.startsWith(" ")) {
                i++;
                continue;
            } else if (substring.startsWith("(")) {
                tokens.add(new Tok.Paren.L());
                i += 1;
            } else if (substring.startsWith(")")) {
                tokens.add(new Tok.Paren.R());
                i += 1;
            } else if (substring.startsWith("&")) {
                tokens.add(new Tok.Op.Bin.And<E>());
                i += 1;
            } else if (substring.startsWith("|")) {
                tokens.add(new Tok.Op.Bin.Or<E>());
                i += 1;
            } else {
                int nextToken = substring.length();
                int index;

                index = substring.indexOf('(');
                if (index > 0 && index < nextToken)
                    nextToken = index;
                index = substring.indexOf(')');
                if (index > 0 && index < nextToken)
                    nextToken = index;
                index = substring.indexOf("&");
                if (index > 0 && index < nextToken)
                    nextToken = index;
                index = substring.indexOf("|");
                if (index > 0 && index < nextToken)
                    nextToken = index;
                index = substring.indexOf(" ");
                if (index > 0 && index < nextToken)
                    nextToken = index;

                String token = substring.substring(0, nextToken);
                tokens.add(factory.buildArg(token));
                i += token.length();
            }
        }

        return tokens;
    }

    public LinkedList<Tok> compile(List<Tok> tokens)
            throws CompileLogicException {
        Stack<Tok> stack = new Stack<Tok>();
        LinkedList<Tok> output = new LinkedList<Tok>();

        int i = 0;
        for (Tok tok : tokens) {
            if (tok instanceof Tok.Paren.L) {
                stack.push(tok);
            } else if (tok instanceof Tok.Paren.R) {
                Tok top;
                do {
                    top = stack.pop();

                    if (!(top instanceof Tok.Paren.L)) {
                        output.offer(top);
                    }

                } while (!(top instanceof Tok.Paren.L));

                i += 1;
            } else if (tok instanceof Tok.Op.Bin.And
                    || tok instanceof Tok.Op.Bin.Or) {
                stack.push(tok);
            } else if (tok instanceof Tok.Arg) {
                output.offer(tok);
            }
        }

        while (!stack.isEmpty()) {
            Tok top = stack.pop();

            if (top instanceof Tok.Paren.L || top instanceof Tok.Paren.R) {
                throw new CompileLogicException("Unbalanced parentheses.");
            }

            output.offer(top);
        }

        return output;
    }
}
