package edu.washington.cs.knowitall.commonlib.regex;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public class RegexTest {
    @Test
    public void testRegex1() {

        String input = "<th.*s>* <is>+ <a>* <good>? <test>";
        RegularExpression<String> regex = new RegularExpression<String>(input,
                new ExpressionFactory<String>() {
                    @Override
                    public BaseExpression<String> create(final String string) {
                        return new BaseExpression<String>(string) {

                            @Override
                            public boolean apply(String token) {
                                return token.matches(string);
                            }
                        };
                    }
                });

        // different forms of "this"
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"this", "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"thes", "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"ths", "is", "a", "a", "a", "good", "test" })));
        
        // different numbers of <a>
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is good test").split(" "))));
        
        // god or no good
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a a a test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is test").split(" "))));
        
        // different numbers of <this>
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is is a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is is is test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this a good test").split(" "))));
        
        // these should all fail
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("iz a good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this a good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is a good").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is a good good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is a good good good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is A good test").split(" "))));
    }
    
    @Test
    public void testRegex2() {

        String input = "<th.*s> <is> <a> <good>? <test>*";
        RegularExpression<String> regex = new RegularExpression<String>(input,
                new ExpressionFactory<String>() {
                    @Override
                    public BaseExpression<String> create(final String string) {
                        return new BaseExpression<String>(string) {

                            @Override
                            public boolean apply(String token) {
                                return token.matches(string);
                            }
                        };
                    }
                });

        // different forms of "this"
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"this", "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"thes", "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"ths", "is", "a", "a", "a", "good", "test" })));
        
        Assert.assertFalse(regex.apply(Arrays.asList(
                new String[] {"thiz", "is", "a", "a", "a", "good", "test" })));
        Assert.assertFalse(regex.apply(Arrays.asList(
                new String[] {"theE", "is", "a", "a", "a", "good", "test" })));
        Assert.assertFalse(regex.apply(Arrays.asList(
                new String[] {"thR", "is", "a", "a", "a", "good", "test" })));
        
        // run out of tokens
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"this", "is", "a", "a", "a", "good" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"thes", "is", "a", "a", "a" })));
        Assert.assertTrue(regex.apply(Arrays.asList(
                new String[] {"ths", "is", "a"})));
    }
    
    @Test
    public void testRegex3() {

        String input = "<th.*s> <is> <a> <good> <test>";
        RegularExpression<String> regex = new RegularExpression<String>(input,
                new ExpressionFactory<String>() {
                    @Override
                    public BaseExpression<String> create(final String string) {
                        return new BaseExpression<String>(string) {

                            @Override
                            public boolean apply(String token) {
                                return token.matches(string);
                            }
                        };
                    }
                });
        
        // multiple <a> and fail
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is a a good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is a a a good test").split(" "))));
    }
    
    
    @Test
    public void testRegex4() {

        String input = "<th.*s> <is> <a>+ <good> <test>";
        RegularExpression<String> regex = new RegularExpression<String>(input,
                new ExpressionFactory<String>() {
                    @Override
                    public BaseExpression<String> create(final String string) {
                        return new BaseExpression<String>(string) {

                            @Override
                            public boolean apply(String token) {
                                return token.matches(string);
                            }
                        };
                    }
                });
        
        // multiple <a> and succeed
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("this is a a a good test").split(" "))));
        
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("this is good test").split(" "))));
    }
    
    @Test
    public void testRegex5() {

        String input = "<[aA]>? <A>";
        RegularExpression<String> regex = new RegularExpression<String>(input,
                new ExpressionFactory<String>() {
                    @Override
                    public BaseExpression<String> create(final String string) {
                        return new BaseExpression<String>(string) {

                            @Override
                            public boolean apply(String token) {
                                return token.matches(string);
                            }
                        };
                    }
                });
        
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("A").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(
                ("a").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(
                ("a A").split(" "))));
    }
}
