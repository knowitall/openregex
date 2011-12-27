package edu.washington.cs.knowitall.commonlib.regex;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.regex.Expression.BaseExpression;

public class RegexTest {
    @Test
    public void testRegexMatches() {
        String input = "<th.*s>* <is>+ <a>* <good>? <test>";
        RegularExpression<String> regex = 
                makeRegex(input);

        // different forms of "this"
        Assert.assertTrue(regex.matches(split("this is a a a good test")));
        Assert.assertTrue(regex.matches(split("thes is a a a good test")));
        Assert.assertTrue(regex.matches(split("ths is a a a good test")));
        Assert.assertFalse(regex.matches(split("thiz is a a a good test")));
        Assert.assertFalse(regex.matches(split("theE is a a a good test")));
        Assert.assertFalse(regex.matches(split("thR is a a a good test")));

        // different numbers of <a>
        Assert.assertTrue(regex.matches(split("this is a good test ")));
        Assert.assertTrue(regex.matches(split("this is a a good test ")));
        Assert.assertTrue(regex.matches(split("this is a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is good test")));

        // with good
        Assert.assertTrue(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("this is good test")));
        
        // without good
        Assert.assertTrue(regex.matches(split("this is a a a a test")));
        Assert.assertTrue(regex.matches(split("this is a test")));
        Assert.assertTrue(regex.matches(split("this is test")));
        
        // different numbers of "this" and "is"
        // multiple "this"
        Assert.assertTrue(regex.matches(split("this ths is a good test")));
        Assert.assertTrue(regex.matches(split("this ths is is a good test")));
        Assert.assertTrue(regex.matches(split("this ths is is is test")));
        Assert.assertFalse(regex.matches(split("this ths test")));
        Assert.assertFalse(regex.matches(split("this ths a good test")));
        // single "this"
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("this is is a good test")));
        Assert.assertTrue(regex.matches(split("this is is is test")));
        Assert.assertFalse(regex.matches(split("this test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        // no "this"
        Assert.assertTrue(regex.matches(split("is a good test")));
        Assert.assertTrue(regex.matches(split("is is a good test")));
        Assert.assertTrue(regex.matches(split("is is is test")));
        Assert.assertFalse(regex.matches(split("test")));
        Assert.assertFalse(regex.matches(split("a good test")));

        // these should all fail
        Assert.assertFalse(regex.matches(split("iz a good test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        Assert.assertFalse(regex.matches(split("this is a good")));
        Assert.assertFalse(regex.matches(split("this is a good good test")));
        Assert.assertFalse(regex.matches(split("this is a good good good test")));
        Assert.assertFalse(regex.matches(split("this is A good test")));
    }
    
    @Test
    public void testRegexFind() {
        String input = "^ <th.*s>* <is>+ <a>* <good>? <test> $";
        RegularExpression<String> regex = 
                makeRegex(input);

        // different forms of "this"
        Assert.assertTrue(regex.apply(split("this is a a a good test")));
        Assert.assertTrue(regex.apply(split("thes is a a a good test")));
        Assert.assertTrue(regex.apply(split("ths is a a a good test")));
        Assert.assertFalse(regex.apply(split("thiz is a a a good test")));
        Assert.assertFalse(regex.apply(split("theE is a a a good test")));
        Assert.assertFalse(regex.apply(split("thR is a a a good test")));

        // different numbers of <a>
        Assert.assertTrue(regex.apply(split("this is a good test ")));
        Assert.assertTrue(regex.apply(split("this is a a good test ")));
        Assert.assertTrue(regex.apply(split("this is a a a good test")));
        Assert.assertTrue(regex.apply(split("this is a a a a good test")));
        Assert.assertTrue(regex.apply(split("this is good test")));

        // with good
        Assert.assertTrue(regex.apply(split("this is a a a a good test")));
        Assert.assertTrue(regex.apply(split("this is a good test")));
        Assert.assertTrue(regex.apply(split("this is good test")));
        
        // without good
        Assert.assertTrue(regex.apply(split("this is a a a a test")));
        Assert.assertTrue(regex.apply(split("this is a test")));
        Assert.assertTrue(regex.apply(split("this is test")));
        
        // different numbers of "this" and "is"
        // multiple "this"
        Assert.assertTrue(regex.apply(split("this ths is a good test")));
        Assert.assertTrue(regex.apply(split("this ths is is a good test")));
        Assert.assertTrue(regex.apply(split("this ths is is is test")));
        Assert.assertFalse(regex.apply(split("this ths test")));
        Assert.assertFalse(regex.apply(split("this ths a good test")));
        // single "this"
        Assert.assertTrue(regex.apply(split("this is a good test")));
        Assert.assertTrue(regex.apply(split("this is is a good test")));
        Assert.assertTrue(regex.apply(split("this is is is test")));
        Assert.assertFalse(regex.apply(split("this test")));
        Assert.assertFalse(regex.apply(split("this a good test")));
        // no "this"
        Assert.assertTrue(regex.apply(split("is a good test")));
        Assert.assertTrue(regex.apply(split("is is a good test")));
        Assert.assertTrue(regex.apply(split("is is is test")));
        Assert.assertFalse(regex.apply(split("test")));
        Assert.assertFalse(regex.apply(split("a good test")));

        // these should all fail
        Assert.assertFalse(regex.apply(split("iz a good test")));
        Assert.assertFalse(regex.apply(split("this a good test")));
        Assert.assertFalse(regex.apply(split("this is a good")));
        Assert.assertFalse(regex.apply(split("this is a good good test")));
        Assert.assertFalse(regex.apply(split("this is a good good good test")));
        Assert.assertFalse(regex.apply(split("this is A good test")));
    }
    
    @Test
    public void testRegexMatchesNoStar() {

        // <a> is required
        String input = "<th.*s>* <is>+ <a> <good>? <test>*";
        RegularExpression<String> regex = makeRegex(input);

        // different forms of "this"
        Assert.assertFalse(regex.matches(split("this is a a a good test")));
        Assert.assertFalse(regex.matches(split("thes is a a a good test")));
        Assert.assertFalse(regex.matches(split("ths is a a a good test")));
        Assert.assertFalse(regex.matches(split("thiz is a a a good test")));
        Assert.assertFalse(regex.matches(split("theE is a a a good test")));
        Assert.assertFalse(regex.matches(split("thR is a a a good test")));

        // different numbers of <a>
        Assert.assertTrue(regex.matches(split("this is a good test ")));
        Assert.assertFalse(regex.matches(split("this is a a good test ")));
        Assert.assertFalse(regex.matches(split("this is a a a good test")));
        Assert.assertFalse(regex.matches(split("this is a a a a good test")));
        Assert.assertFalse(regex.matches(split("this is good test")));

        // with good
        Assert.assertFalse(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertFalse(regex.matches(split("this is good test")));
        
        // without good
        Assert.assertFalse(regex.matches(split("this is a a a a test")));
        Assert.assertTrue(regex.matches(split("this is a test")));
        Assert.assertFalse(regex.matches(split("this is test")));

        // different numbers of "this" and "is"
        // multiple "this"
        Assert.assertTrue(regex.matches(split("this ths is a good test")));
        Assert.assertTrue(regex.matches(split("this ths is is a good test")));
        Assert.assertFalse(regex.matches(split("this ths is is is test")));
        Assert.assertFalse(regex.matches(split("this ths test")));
        Assert.assertFalse(regex.matches(split("this ths a good test")));
        // single "this"
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("this is is a good test")));
        Assert.assertFalse(regex.matches(split("this is is is test")));
        Assert.assertFalse(regex.matches(split("this test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        // no "this"
        Assert.assertTrue(regex.matches(split("is a good test")));
        Assert.assertTrue(regex.matches(split("is is a good test")));
        Assert.assertFalse(regex.matches(split("is is is test")));
        Assert.assertFalse(regex.matches(split("test")));
        Assert.assertFalse(regex.matches(split("a good test")));

        // these should all fail
        Assert.assertFalse(regex.matches(split("iz a good test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        Assert.assertTrue(regex.matches(split("this is a good")));
        Assert.assertFalse(regex.matches(split("this is a good good test")));
        Assert.assertFalse(regex.matches(split("this is a good good good test")));
        Assert.assertFalse(regex.matches(split("this is A good test")));
    }

    @Test
    public void testRegexMatchesAllTokens() {

        String input = "<th.*s> <is> <a> <good> <test>";
        RegularExpression<String> regex = makeRegex(input);
        
        // different forms of "this"
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("thes is a good test")));
        Assert.assertTrue(regex.matches(split("ths is a good test")));
        Assert.assertFalse(regex.matches(split("thiz is a good test")));
        Assert.assertFalse(regex.matches(split("theE is a good test")));
        Assert.assertFalse(regex.matches(split("thR is a good test")));

        // different numbers of <a>
        Assert.assertTrue(regex.matches(split("this is a good test ")));
        Assert.assertFalse(regex.matches(split("this is a a good test ")));
        Assert.assertFalse(regex.matches(split("this is a a a good test")));
        Assert.assertFalse(regex.matches(split("this is a a a a good test")));
        Assert.assertFalse(regex.matches(split("this is good test")));

        // with good
        Assert.assertFalse(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertFalse(regex.matches(split("this is good test")));
        
        // without good
        Assert.assertFalse(regex.matches(split("this is a a a a test")));
        Assert.assertFalse(regex.matches(split("this is a test")));
        Assert.assertFalse(regex.matches(split("this is test")));

        // different numbers of "this" and "is"
        // multiple "this"
        Assert.assertFalse(regex.matches(split("this ths is a good test")));
        Assert.assertFalse(regex.matches(split("this ths is is a good test")));
        Assert.assertFalse(regex.matches(split("this ths is is is test")));
        Assert.assertFalse(regex.matches(split("this ths test")));
        Assert.assertFalse(regex.matches(split("this ths a good test")));
        // single "this"
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertFalse(regex.matches(split("this is is a good test")));
        Assert.assertFalse(regex.matches(split("this is is is test")));
        Assert.assertFalse(regex.matches(split("this test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        // no "this"
        Assert.assertFalse(regex.matches(split("is a good test")));
        Assert.assertFalse(regex.matches(split("is is a good test")));
        Assert.assertFalse(regex.matches(split("is is is test")));
        Assert.assertFalse(regex.matches(split("test")));
        Assert.assertFalse(regex.matches(split("a good test")));

        // these should all fail
        Assert.assertFalse(regex.matches(split("iz a good test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        Assert.assertFalse(regex.matches(split("this is a good")));
        Assert.assertFalse(regex.matches(split("this is a good good test")));
        Assert.assertFalse(regex.matches(split("this is a good good good test")));
        Assert.assertFalse(regex.matches(split("this is A good test")));
    }

    @Test
    public void testRegexMatchesFirstRequired() {

        String input = "<th.*s> <is>+ <a>* <good>? <test>";
        RegularExpression<String> regex = makeRegex(input);


        // different forms of "this"
        Assert.assertTrue(regex.matches(split("this is a a a good test")));
        Assert.assertTrue(regex.matches(split("thes is a a a good test")));
        Assert.assertTrue(regex.matches(split("ths is a a a good test")));
        Assert.assertFalse(regex.matches(split("thiz is a a a good test")));
        Assert.assertFalse(regex.matches(split("theE is a a a good test")));
        Assert.assertFalse(regex.matches(split("thR is a a a good test")));

        // different numbers of <a>
        Assert.assertTrue(regex.matches(split("this is a good test ")));
        Assert.assertTrue(regex.matches(split("this is a a good test ")));
        Assert.assertTrue(regex.matches(split("this is a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is good test")));

        // with good
        Assert.assertTrue(regex.matches(split("this is a a a a good test")));
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("this is good test")));
        
        // without good
        Assert.assertTrue(regex.matches(split("this is a a a a test")));
        Assert.assertTrue(regex.matches(split("this is a test")));
        Assert.assertTrue(regex.matches(split("this is test")));

        // different numbers of "this" and "is"
        // multiple "this"
        Assert.assertFalse(regex.matches(split("this ths is a good test")));
        Assert.assertFalse(regex.matches(split("this ths is is a good test")));
        Assert.assertFalse(regex.matches(split("this ths is is is test")));
        Assert.assertFalse(regex.matches(split("this ths test")));
        Assert.assertFalse(regex.matches(split("this ths a good test")));
        // single "this"
        Assert.assertTrue(regex.matches(split("this is a good test")));
        Assert.assertTrue(regex.matches(split("this is is a good test")));
        Assert.assertTrue(regex.matches(split("this is is is test")));
        Assert.assertFalse(regex.matches(split("this test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        // no "this"
        Assert.assertFalse(regex.matches(split("is a good test")));
        Assert.assertFalse(regex.matches(split("is is a good test")));
        Assert.assertFalse(regex.matches(split("is is is test")));
        Assert.assertFalse(regex.matches(split("test")));
        Assert.assertFalse(regex.matches(split("a good test")));

        // these should all fail
        Assert.assertFalse(regex.matches(split("iz a good test")));
        Assert.assertFalse(regex.matches(split("this a good test")));
        Assert.assertFalse(regex.matches(split("this is a good")));
        Assert.assertFalse(regex.matches(split("this is a good good test")));
        Assert.assertFalse(regex.matches(split("this is a good good good test")));
        Assert.assertFalse(regex.matches(split("this is A good test")));
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
    public void testUnnamedGroupRegex() {
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
    public void testNamedGroupRegex() {
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
        RegularExpression<String> regex = makeRegex(input);

        FiniteAutomaton.Automaton<String> auto = regex.build(regex.expressions);

        auto.lookingAt(split("this is a"));
        Assert.assertTrue(auto.apply(split(("this is a"))));
    }

    @Test
    public void testAutomataOr() {

        String input = "(<th.*s>) | (<is>+ <a>*)+ <good>? <test>";
        RegularExpression<String> regex = makeRegex(input);

        FiniteAutomaton.Automaton<String> auto = regex.build(regex.expressions);

        // different forms of "this"
        Assert.assertTrue(auto.apply(split("this good test")));
        Assert.assertTrue(auto.apply(split("is a a a good test")));
        Assert.assertTrue(auto.apply(split("is a a a is good test")));
        Assert.assertTrue(auto.apply(split("is a a a is a good test")));
        Assert.assertFalse(auto.apply(split("a is a a a is good test")));

        // different numbers of <a>
        Assert.assertTrue(auto.apply(split("is a good test")));
        Assert.assertTrue(auto.apply(split("is a a good test")));
        Assert.assertTrue(auto.apply(split("is a a a good test")));
        Assert.assertTrue(auto.apply(split("is a a a a good test")));
        Assert.assertTrue(auto.apply(split("this good test")));

        // god or no good
        Assert.assertTrue(auto.apply(split("is a a a a good test")));
        Assert.assertTrue(auto.apply(split("is good test")));
        Assert.assertTrue(auto.apply(split("is a a a a test")));
        Assert.assertFalse(auto.apply(split("test")));
        // different numbers of <this>
        Assert.assertFalse(auto.apply(split("this a good test")));

        // these should all fail
        Assert.assertFalse(auto.apply(split("iz a good test")));
        Assert.assertFalse(auto.apply(split("this a good test")));
        Assert.assertFalse(auto.apply(split("this is a good")));
        Assert.assertFalse(auto.apply(split("this is a good good test")));
        Assert.assertFalse(auto.apply(split("this is a good good good test")));
        Assert.assertFalse(auto.apply(split("this is A good test")));
    }

    public static RegularExpression<String> makeRegex(String input) {
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
    
    public static List<String> split(String input) {
        return Arrays.asList(input.split(" "));
    }
}
