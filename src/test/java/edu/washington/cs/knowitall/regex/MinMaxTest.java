package edu.washington.cs.knowitall.regex;

import org.junit.Test;

import com.google.common.collect.Lists;

import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class MinMaxTest {

    @Test
    public void testMinMax() {
        RegularExpression<String> regExZeroToOne = getAbcRegex(0, 1);
        assertMatch(regExZeroToOne, "a", "c");
        assertMatch(regExZeroToOne, "a", "b", "c");
        assertNoMatch(regExZeroToOne, "a", "b", "b", "c");

        RegularExpression<String> regExOne = getAbcRegex(1, 1);
        assertNoMatch(regExOne, "a", "c");
        assertMatch(regExOne, "a", "b", "c");
        assertNoMatch(regExOne, "a", "b", "b", "c");

        RegularExpression<String> regExTwo = getAbcRegex(2, 2);
        assertNoMatch(regExTwo, "a", "c");
        assertNoMatch(regExTwo, "a", "b", "c");
        assertMatch(regExTwo, "a", "b", "b", "c");
        assertNoMatch(regExTwo, "a", "b", "b", "b", "c");

        RegularExpression<String> regExOneToTwo = getAbcRegex(1, 2);
        assertNoMatch(regExOneToTwo, "a", "c");
        assertMatch(regExOneToTwo, "a", "b", "c");
        assertMatch(regExOneToTwo, "a", "b", "b", "c");
        assertNoMatch(regExOneToTwo, "a", "b", "b", "b", "c");

        RegularExpression<String> regExTwoToFour = getAbcRegex(2, 4);
        assertNoMatch(regExTwoToFour, "a", "c");
        assertNoMatch(regExTwoToFour, "a", "b", "c");
        assertMatch(regExTwoToFour, "a", "b", "b", "c");
        assertMatch(regExTwoToFour, "a", "b", "b", "b", "c");
        assertMatch(regExTwoToFour, "a", "b", "b", "b", "b", "c");
        assertNoMatch(regExTwoToFour, "a", "b", "b", "b", "b", "b", "c");
    }

    private void assertMatch(RegularExpression<String> regex, String... input) {
        assertNotNull(regex.find(Arrays.asList(input)));
    }

    private void assertNoMatch(RegularExpression<String> regex, String... input) {
        assertNull(regex.find(Arrays.asList(input)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException1() {
        getAbcRegex(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2() {
        getAbcRegex(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException3() {
        getAbcRegex(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException4() {
        getAbcRegex(0, -1);
    }

    private RegularExpression<String> getAbcRegex(int min, int max) {
        Expression<String> wordA = RegularExpressionParsers.word.parse("<a>").expressions.get(0);
        Expression<String> wordB = RegularExpressionParsers.word.parse("<b>").expressions.get(0);
        Expression<String> wordC = RegularExpressionParsers.word.parse("<c>").expressions.get(0);
        return RegularExpression.compile(Lists.newArrayList(
                wordA,
                new Expression.MinMax<String>(wordB, min, max),
                wordC)
            );
    }
}
