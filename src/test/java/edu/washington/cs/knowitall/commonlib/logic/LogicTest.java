package edu.washington.cs.knowitall.commonlib.logic;

import org.junit.Assert;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.logic.Tok.Arg;

public class LogicTest {
    @Test
    public void testLogicOrderOfOperations() {
        Assert.assertEquals("(false & (false & false))", createLogic("false & false & false").toString());
        Assert.assertEquals("((false & false) | false)", createLogic("false & false | false").toString());
        Assert.assertEquals("(false | (false & false))", createLogic("false | false & false").toString());
    }
    
    @Test
    public void testLogic2Variables() {
        for (int i = 0; i < 4; i++) {
            boolean a = (i & 1) != 0;
            boolean b = (i & 2) != 0;

            Assert.assertEquals(a | b,
                createLogic(createExpression("a | b", a, b))
                    .apply("true"));

            Assert.assertEquals(a & b,
                createLogic(createExpression("a & b", a, b))
                    .apply("true"));
        }
    }

    @Test
    public void testLogic3Variables() {
        for (int i = 0; i < 8; i++) {
            boolean a = (i & 1) != 0;
            boolean b = (i & 2) != 0;
            boolean c = (i & 4) != 0;

            Assert.assertEquals(a | (b & c),
                createLogic(createExpression("a | (b & c)", a, b, c))
                    .apply("true"));

            Assert.assertEquals(a | (b | c),
                createLogic(createExpression("a | (b | c)", a, b, c))
                    .apply("true"));

            Assert.assertEquals(a & (b & c),
                createLogic(createExpression("a & (b & c)", a, b, c))
                    .apply("true"));

            Assert.assertEquals(a & (b | c),
                createLogic(createExpression("a & (b | c)", a, b, c))
                    .apply("true"));
        }
    }


    @Test
    public void testLogic4Variables() {
        for (int i = 0; i < 16; i++) {
            boolean a = (i & 1) != 0;
            boolean b = (i & 2) != 0;
            boolean c = (i & 4) != 0;
            boolean d = (i & 8) != 0;
            
            Assert.assertEquals(a | (b & c & d),
                createLogic(createExpression("a | (b & c & d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a | (b & c | d),
                createLogic(createExpression("a | (b & c | d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a | (b | c & d),
                createLogic(createExpression("a | (b | c & d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a | (b | c | d),
                createLogic(createExpression("a | (b | c | d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a & (b & c & d),
                createLogic(createExpression("a & (b & c & d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a & (b & c | d),
                createLogic(createExpression("a & (b & c | d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a & (b | c & d),
                createLogic(createExpression("a & (b | c & d)", a, b, c, d))
                    .apply("true"));

            Assert.assertEquals(a & (b | c | d),
                createLogic(createExpression("a & (b | c | d)", a, b, c, d))
                    .apply("true"));
            
            Assert.assertEquals((a | b) & (c | d),
                createLogic(createExpression("(a | b) & (c | d)", a, b, c, d))
                    .apply("true"));
            
            Assert.assertEquals((a & b) | (c & d),
                createLogic(createExpression("(a & b) | (c & d)", a, b, c, d))
                    .apply("true"));
            
            Assert.assertEquals(!(a | b) & (c | d),
                createLogic(createExpression("!(a | b) & (c | d)", a, b, c, d))
                    .apply("true"));
            
            Assert.assertEquals((a | b) & !(c | d),
                createLogic(createExpression("(a | b) & !(c | d)", a, b, c, d))
                    .apply("true"));
            
            Assert.assertEquals(!((a | b) & !(c | d)),
                createLogic(createExpression("!((a | b) & !(c | d))", a, b, c, d))
                    .apply("true"));
        }
    }

    public String createExpression(String expr, Boolean... varargs) {
        for (int i = 0; i < varargs.length; i++) {
            Character v = (char)('a' + i);
            expr = expr.replace(v.toString(), Boolean.toString(varargs[i]));
        }
        
        return expr;
    }
    
    public LogicExpression<String> createLogic(String logic) {
        LogicExpression<String> expr = new LogicExpression<String>(logic, new ArgFactory<String>() {
            @Override
            public Arg<String> create(final String string) {
                return new Arg.Pred<String>(string) {
                    @Override
                    public boolean apply(String entity) {
                        return "true".equals(string);
                    }
                };
            }});
        
        return expr;
    }
}
