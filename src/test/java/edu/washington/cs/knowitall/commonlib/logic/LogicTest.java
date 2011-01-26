package edu.washington.cs.knowitall.commonlib.logic;

import org.junit.Assert;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.logic.LogicExpression.TokenizeLogicException;
import edu.washington.cs.knowitall.commonlib.logic.Tok.Arg;

public class LogicTest {
    @Test
    public void testRegex1() {
        Assert.assertTrue(createLogic("true & true").apply("true"));
        Assert.assertFalse(createLogic("false & true").apply("true"));
        Assert.assertFalse(createLogic("true & false").apply("true"));
        Assert.assertFalse(createLogic("false & false").apply("true"));
        
        Assert.assertTrue(createLogic("true | true").apply("true"));
        Assert.assertTrue(createLogic("false | true").apply("true"));
        Assert.assertTrue(createLogic("true | false").apply("true"));
        Assert.assertFalse(createLogic("false | false").apply("true"));
        
        Assert.assertTrue(createLogic("true & (true & true)").apply("true"));
        Assert.assertFalse(createLogic("true & (false & true)").apply("true"));
        Assert.assertFalse(createLogic("true & (true & false)").apply("true"));
        Assert.assertFalse(createLogic("true & (false & false)").apply("true"));
        
        Assert.assertTrue(createLogic("true & (true | true)").apply("true"));
        Assert.assertTrue(createLogic("true & (false | true)").apply("true"));
        Assert.assertTrue(createLogic("true & (true | false)").apply("true"));
        Assert.assertFalse(createLogic("true & (false | false)").apply("true"));
        
        Assert.assertTrue(createLogic("true | (true & true)").apply("true"));
        Assert.assertTrue(createLogic("true | (false & true)").apply("true"));
        Assert.assertTrue(createLogic("true | (true & false)").apply("true"));
        Assert.assertTrue(createLogic("true | (false & false)").apply("true"));
        
        Assert.assertTrue(createLogic("true | (true | true)").apply("true"));
        Assert.assertTrue(createLogic("true | (false | true)").apply("true"));
        Assert.assertTrue(createLogic("true | (true | false)").apply("true"));
        Assert.assertTrue(createLogic("true | (false | false)").apply("true"));
        
        for (int i = 0; i < 16; i++) {
            Boolean a = (i & 1) != 0;
            Boolean b = (i & 2) != 0;
            Boolean c = (i & 4) != 0;
            Boolean d = (i & 8) != 0;
        }
    }
    
    public LogicExpression<String> createLogic(String logic) {
        LogicExpression<String> expr = new LogicExpression<String>(logic, new ArgFactory<String>() {
            @Override
            public Arg<String> buildArg(final String string)
                    throws TokenizeLogicException {
                return new Arg<String>() {
                    @Override
                    public boolean apply(String entity) {
                        return "true".equals(string);
                    }
                };
            }});
        
        return expr;
    }
}
