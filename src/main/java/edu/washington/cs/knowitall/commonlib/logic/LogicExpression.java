package edu.washington.cs.knowitall.commonlib.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Predicate;

public class LogicExpression<E> implements Predicate<E> {
    public static class LogicException extends RuntimeException {
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

    final Tok.Apply<E> expression;

    public LogicExpression(String input, ArgFactory<E> factory)
            throws CompileLogicException, TokenizeLogicException {
        // convert to tokens
        List<Tok> tokens = tokenize(input, factory);
        
        // put in reverse polish notation
        List<Tok> rpn = rpn(tokens);
        
        // compile the expression
        expression = compile(rpn);
    }
    
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

    @SuppressWarnings("unchecked")
    public Tok.Apply<E> compile(List<Tok> rpn) {
        if (rpn.isEmpty()) {
            return null;
        }
        
        Stack<Tok.Apply<E>> stack = new Stack<Tok.Apply<E>>();
        for (Tok tok : rpn) {
            if (tok instanceof Tok.Arg<?>) {
                stack.push((Tok.Arg<E>) tok);
            } else if (tok instanceof Tok.Op) {
                if (tok instanceof Tok.Op.Bin) {
                    Tok.Apply<E> arg1 = (Tok.Apply<E>) stack.pop();
                    Tok.Apply<E> arg2 = (Tok.Apply<E>) stack.pop();
                    
                    Tok.Op.Bin<E> bin = (Tok.Op.Bin<E>) tok;
                    
                    bin.left = arg1;
                    bin.right = arg2;
                    
                    stack.push(bin);
                }
            }
        }

        if (stack.size() > 1) {
            throw new ApplyLogicException(
                    "Stack has multiple elements after apply.");
        }

        if (stack.size() == 0) {
            throw new ApplyLogicException(
                    "Stack has zero elements after apply.");
        }

        if (!(stack.peek() instanceof Tok.Apply<?>)) {
            throw new ApplyLogicException(
                    "Stack contains non-applies after apply.");
        }

        return ((Tok.Apply<E>) stack.pop());
    }
    
    public List<String> getArgs() {
        List<String> args = new ArrayList<String>();
        getArgs(this.expression, args);
        
        return args;
    }
    
    public void getArgs(Tok.Apply<?> apply, List<String> args) {
        if (apply instanceof Tok.Op.Bin<?>) {
            Tok.Op.Bin<?> bin = (Tok.Op.Bin<?>) apply;
            
            getArgs(bin.left, args);
            getArgs(bin.right, args);
        }
        else if (apply instanceof Tok.Arg.Pred<?>) {
            args.add(((Tok.Arg.Pred<?>)apply).getDescription());
        }
    }

    public String toString() {
        return expression.toString();
    }

    public List<Tok> tokenize(String input, ArgFactory<E> factory) throws TokenizeLogicException {
        List<Tok> tokens = new ArrayList<Tok>();

        int i = 0;
        while (i < input.length()) {
            String substring = input.substring(i);
            char firstChar = substring.charAt(0);
            if (firstChar == '(') {
                tokens.add(new Tok.Paren.L());
                i += 1;
            } else if (firstChar == ')') {
                tokens.add(new Tok.Paren.R());
                i += 1;
            } else if (firstChar == '&') {
                tokens.add(new Tok.Op.Bin.And<E>());
                i += 1;
            } else if (firstChar == '|') {
                tokens.add(new Tok.Op.Bin.Or<E>());
                i += 1;
            } else {
                Stack<Character> parens = new Stack<Character>();
                
                boolean quoted = false;
                int nextToken;
                for (nextToken = 1; nextToken < substring.length(); nextToken++) {
                    char c = substring.charAt(nextToken);
                    
                    if (c == '"') {
                        quoted = !quoted;
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

                String token = substring.substring(0, nextToken).trim();
                tokens.add(factory.buildArg(token));
                i += token.length();
            }
        }

        return tokens;
    }

    public List<Tok> rpn(List<Tok> tokens)
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
