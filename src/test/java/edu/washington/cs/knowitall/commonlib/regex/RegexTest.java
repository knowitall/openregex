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

        Match<String> match;

        match = regex.lookingAt(Arrays.asList(new String[] { "this", "is", "a",
                "a", "a", "good", "test" }));
        // Assert.assertEquals(match.toString(),
        // "[{<th.*s>*:'this'}, {<is>+:'is'}, {<a>*:'a a a'}, {<good>?:'good'}, {<test>:'test'}]");

        // different forms of "this"
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "this",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "thes",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "ths", "is",
                "a", "a", "a", "good", "test" })));

        // different numbers of <a>
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a a a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays
                .asList(("this is a a a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is good test")
                .split(" "))));

        // god or no good
        Assert.assertTrue(regex.apply(Arrays
                .asList(("this is a a a a good test").split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a a a a test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is test").split(" "))));

        // different numbers of <this>
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is is a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is is is test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this a good test")
                .split(" "))));

        // these should all fail
        Assert.assertFalse(regex.apply(Arrays.asList(("iz a good test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this a good test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this is a good")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays
                .asList(("this is a good good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays
                .asList(("this is a good good good test").split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this is A good test")
                .split(" "))));
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
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "this",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "thes",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "ths", "is",
                "a", "a", "a", "good", "test" })));

        Assert.assertFalse(regex.apply(Arrays.asList(new String[] { "thiz",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertFalse(regex.apply(Arrays.asList(new String[] { "theE",
                "is", "a", "a", "a", "good", "test" })));
        Assert.assertFalse(regex.apply(Arrays.asList(new String[] { "thR",
                "is", "a", "a", "a", "good", "test" })));

        // run out of tokens
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "this",
                "is", "a", "a", "a", "good" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "thes",
                "is", "a", "a", "a" })));
        Assert.assertTrue(regex.apply(Arrays.asList(new String[] { "ths", "is",
                "a" })));
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
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this is good test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays.asList(("this is a a good test")
                .split(" "))));
        Assert.assertFalse(regex.apply(Arrays
                .asList(("this is a a a good test").split(" "))));
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
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a a good test")
                .split(" "))));
        Assert.assertTrue(regex.apply(Arrays.asList(("this is a a a good test")
                .split(" "))));

        Assert.assertFalse(regex.apply(Arrays.asList(("this is good test")
                .split(" "))));
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

        Assert.assertTrue(regex.lookingAt(Arrays.asList(("A").split(" "))) != null);
        Assert.assertFalse(regex.lookingAt(Arrays.asList(("a").split(" "))) != null);
        Assert.assertTrue(regex.lookingAt(Arrays.asList(("a A").split(" "))) != null);
    }

    @Test
    public void testGreedyRegex() {

        String input = "<a>+ <a> <b>* <b>";
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

        Assert.assertTrue(regex
                .lookingAt(Arrays.asList(("a a a a b b").split(" "))).range()
                .getLength() == 6);
        Assert.assertTrue(regex
                .lookingAt(Arrays.asList(("a a a a a b b b b").split(" ")))
                .range().getLength() == 9);
    }

    @Test
    public void testGroupRegex1() {
        String input = "(<th.*s>* <is>+) (<a>* <good>? <test>) (?:<right>)";
        RegularExpression<String> regex = makeRegex(input);

        Match<String> match;
        match = regex.lookingAt(Arrays.asList(new String[] { "this", "is", "a",
                "a", "a", "good", "test", "right" }));

        Assert.assertNotNull(match);
        Assert.assertEquals(match.groups().size(), 3);
        Assert.assertEquals(match.groups().get(1).tokens().size(), 2);
        Assert.assertEquals(match.groups().get(2).tokens().size(), 5);
    }
    
    @Test
    public void testNamedGroupRegex1() {
        String input = "(<this>:<th.*s>* <is>+) (<good>:<a>* <good>? <test>) (?:<right>)";
        RegularExpression<String> regex = makeRegex(input);

        Match<String> match;
        match = regex.lookingAt(Arrays.asList(new String[] { "this", "is", "a",
                "a", "a", "good", "test", "right" }));

        Assert.assertNotNull(match);
        Assert.assertEquals(match.groups().size(), 3);
        Assert.assertEquals(match.group("this").tokens().size(), 2);
        Assert.assertEquals(match.group("good").tokens().size(), 5);
    }

    @Test
    public void testSimpleAutomata() {

        String input = "<this> <is> <a>";
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

        FiniteAutomaton.Automaton<String> auto = regex.build(regex.expressions);

        auto.lookingAt(Arrays.asList(("this is a").split(" ")));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a").split(" "))));
    }

    @Test
    public void testAutomata() {

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

        FiniteAutomaton.Automaton<String> auto = regex.build(regex.expressions);

        // different forms of "this"
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "this", "is",
                "a", "a", "a", "good", "test" })));
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "thes", "is",
                "a", "a", "a", "good", "test" })));
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "ths", "is",
                "a", "a", "a", "good", "test" })));

        // different numbers of <a>
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays
                .asList(("this is a a a a good test").split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is good test")
                .split(" "))));

        // god or no good
        Assert.assertTrue(auto.apply(Arrays
                .asList(("this is a a a a good test").split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a a a a test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is test").split(" "))));

        // different numbers of <this>
        Assert.assertTrue(auto.apply(Arrays.asList(("this is a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is is a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this is is is test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this test").split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this a good test")
                .split(" "))));

        // these should all fail
        Assert.assertFalse(auto.apply(Arrays.asList(("iz a good test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this a good test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this is a good")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays
                .asList(("this is a good good test").split(" "))));
        Assert.assertFalse(auto.apply(Arrays
                .asList(("this is a good good good test").split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this is A good test")
                .split(" "))));
    }

    @Test
    public void testAutomataOr() {

        String input = "(<th.*s>) | (<is>+ <a>*)+ <good>? <test>";
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

        FiniteAutomaton.Automaton<String> auto = regex.build(regex.expressions);

        Match<String> match = auto.lookingAt(Arrays.asList(new String[] {
                "this", "good", "test" }));

        // different forms of "this"
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "this",
                "good", "test" })));
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "is", "a",
                "a", "a", "good", "test" })));
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "is", "a",
                "a", "a", "is", "good", "test" })));
        Assert.assertTrue(auto.apply(Arrays.asList(new String[] { "is", "a",
                "a", "a", "is", "a", "good", "test" })));
        Assert.assertFalse(auto.apply(Arrays.asList(new String[] { "a", "is",
                "a", "a", "a", "is", "good", "test" })));

        // different numbers of <a>
        Assert.assertTrue(auto.apply(Arrays.asList(("is a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("is a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("is a a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("is a a a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("this good test")
                .split(" "))));

        // god or no good
        Assert.assertTrue(auto.apply(Arrays.asList(("is a a a a good test")
                .split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("is good test").split(" "))));
        Assert.assertTrue(auto.apply(Arrays.asList(("is a a a a test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("test").split(" "))));

        // different numbers of <this>
        Assert.assertFalse(auto.apply(Arrays.asList(("this a good test")
                .split(" "))));

        // these should all fail
        Assert.assertFalse(auto.apply(Arrays.asList(("iz a good test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this a good test")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this is a good")
                .split(" "))));
        Assert.assertFalse(auto.apply(Arrays
                .asList(("this is a good good test").split(" "))));
        Assert.assertFalse(auto.apply(Arrays
                .asList(("this is a good good good test").split(" "))));
        Assert.assertFalse(auto.apply(Arrays.asList(("this is A good test")
                .split(" "))));
    }

    public RegularExpression<String> makeRegex(String input) {
        return new RegularExpression<String>(input,
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
    }
}
