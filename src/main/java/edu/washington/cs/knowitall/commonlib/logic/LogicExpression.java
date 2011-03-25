package edu.washington.cs.knowitall.commonlib.logic;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.logic.LogicException.ApplyLogicException;
import edu.washington.cs.knowitall.commonlib.logic.LogicException.CompileLogicException;
import edu.washington.cs.knowitall.commonlib.logic.LogicException.TokenizeLogicException;
import edu.washington.cs.knowitall.commonlib.logic.Tok.Arg;

public class LogicExpression<E> implements Predicate<E> {
    private final Tok.Apply<E> expression;
    
    /***
     * 
     * @param input an infix representation of the logic expression.
     * @param factory a delegate to convert the string representation of an
     * expression to a token.
     * @throws TokenizeLogicException
     * @throws CompileLogicException
     */
    public LogicExpression(String input, ArgFactory<E> factory)
            throws TokenizeLogicException, CompileLogicException {
        // convert to tokens
        List<Tok<E>> tokens = tokenize(input, factory);
        
        // put in reverse polish notation
        List<Tok<E>> rpn = rpn(tokens);
        
        // compile the expression
        expression = compile(rpn);
    }
    
    public String toString() {
        return expression.toString();
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
    public Tok.Apply<E> compile(List<Tok<E>> rpn) {
        if (rpn.isEmpty()) {
            return null;
        }
        
        Stack<Tok.Apply<E>> stack = new Stack<Tok.Apply<E>>();
        for (Tok<E> tok : rpn) {
            if (tok instanceof Tok.Arg<?>) {
                stack.push((Tok.Arg<E>) tok);
            } else if (tok instanceof Tok.Op) {
                try {
                    if (tok instanceof Tok.Op.Mon){
                       Tok.Apply<E> sub = (Tok.Apply<E>) stack.pop();
                       
                        Tok.Op.Mon<E> mon = (Tok.Op.Mon<E>) tok;
                        
                        mon.sub = sub;
                        
                        stack.push(mon);
                    }
                    if (tok instanceof Tok.Op.Bin) {
                        Tok.Apply<E> arg2 = (Tok.Apply<E>) stack.pop();
                        Tok.Apply<E> arg1 = (Tok.Apply<E>) stack.pop();
                        
                        Tok.Op.Bin<E> bin = (Tok.Op.Bin<E>) tok;
                        
                        bin.left = arg1;
                        bin.right = arg2;
                        
                        stack.push(bin);
                    }
                }
                catch (EmptyStackException e) {
                    throw new CompileLogicException("No argument for operator (stack empty): " + tok.toString());
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

        if (!(stack.peek() instanceof Tok.Apply<?>)) {
            throw new ApplyLogicException(
                    "Stack contains non-appliable tokens after apply: " + stack.toString());
        }

        return ((Tok.Apply<E>) stack.pop());
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
    private void getArgs(Tok.Apply<?> apply, List<String> args) {
        if (apply instanceof Tok.Op.Bin<?>) {
            Tok.Op.Bin<?> bin = (Tok.Op.Bin<?>) apply;
            
            getArgs(bin.left, args);
            getArgs(bin.right, args);
        }
        else if (apply instanceof Tok.Arg.Pred<?>) {
            args.add(((Tok.Arg.Pred<?>)apply).getDescription());
        }
    }

    /***
     * Convert an infix string logic representation to an infix list of tokens.
     * @param input an infix string logic representation.
     * @param factory a delegate that converts a string representation of an argument into a token object.
     * @return 
     * @throws TokenizeLogicException
     */
    public List<Tok<E>> tokenize(String input, ArgFactory<E> factory) throws TokenizeLogicException {
        List<Tok<E>> tokens = new ArrayList<Tok<E>>();

        int i = 0;
        while (i < input.length()) {
            String substring = input.substring(i);
            char firstChar = substring.charAt(0);
            
            if (firstChar == ' ') {
                i += 1;
                continue;
            }
            else if (firstChar == '(') {
                tokens.add(new Tok.Paren.L<E>());
                i += 1;
            } else if (firstChar == ')') {
                tokens.add(new Tok.Paren.R<E>());
                i += 1;
            } else if (firstChar == '!') {
                tokens.add(new Tok.Op.Mon.Not<E>());
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
                char quote = ' ';
                int nextToken;
                for (nextToken = 1; nextToken < substring.length(); nextToken++) {
                    char c = substring.charAt(nextToken);
                    
                    if (c == '"' && (!quoted || quote == '"')) {
                        quoted = !quoted;
                        quote = '"';
                    }
                    if (c == '\'' & (!quoted || quote == '\'')) {
                        quoted = !quoted;
                        quote = '\'';
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
                
                if (token.isEmpty()) {
                    throw new TokenizeLogicException("zero-length token found.");
                }
                
                tokens.add(factory.apply(token));
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
    public List<Tok<E>> rpn(List<Tok<E>> tokens)
            throws CompileLogicException {
        // intermediate storage
        Stack<Tok<E>> stack = new Stack<Tok<E>>();
        
        // final rpn output
        LinkedList<Tok<E>> output = new LinkedList<Tok<E>>();

        int i = 0;
        for (Tok<E> tok : tokens) {
            if (tok instanceof Tok.Paren.L) {
                stack.push(tok);
            } else if (tok instanceof Tok.Paren.R) {
                Tok<E> top;
                do {
                    top = stack.pop();

                    if (!(top instanceof Tok.Paren.L)) {
                        output.offer(top);
                    }

                } while (!(top instanceof Tok.Paren.L));

                i += 1;
            } else if (tok instanceof Tok.Op.Mon) {
                stack.push(tok);
            } else if (tok instanceof Tok.Op.Bin) {
                // higher precedence
                while (!stack.isEmpty() && stack.peek() instanceof Tok.Op 
                        && ((Tok.Op<?>)stack.peek()).preceeds((Tok.Op<?>)tok)) {
                    output.offer(stack.pop());
                }
                
                stack.push(tok);
            } else if (tok instanceof Tok.Arg) {
                output.offer(tok);
            }
        }

        // empty out items remaining ni the stack
        while (!stack.isEmpty()) {
            Tok<E> top = stack.pop();

            if (top instanceof Tok.Paren.L || top instanceof Tok.Paren.R) {
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
            
            LogicExpression<String> expr = new LogicExpression<String>(line, new ArgFactory<String>() {
                @Override
                public Arg<String> create(final String string) {
                    return new Arg.Pred<String>(string) {
                        @Override
                        public boolean apply(String entity) {
                            return "true".equals(string);
                        }
                    };
                }});
            
            System.out.println(expr.toString());
            System.out.println(expr.apply(""));
        }
    }
}
