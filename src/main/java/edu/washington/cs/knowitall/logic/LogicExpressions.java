package edu.washington.cs.knowitall.logic;

import com.google.common.base.Function;

/**
 * Static factories for logic expressions over basic objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
class LogicExpressions {
    /**
     * Logic expressions where "true" evaluates to true and "false" evaluates
     * to false.  For example:
     *
     *   (true | false) & true
     *
     * This logic expression is trivial because it's value is independent of
     * the object it is applied to.
     */
    public static LogicExpression<String> trivial(final String expr) {
        return new LogicExpression<String>(expr, new Function<String, Tok.Arg<String>>() {
            @Override
            public Tok.Arg<String> apply(final String string) {
                return new Tok.Arg.Pred<String>(string) {
                    @Override
                    public boolean apply(String entity) {
                        return "true".equals(string);
                    }
                };
            }});
    }
}
