package edu.washington.cs.knowitall.logic;

/**
 * Static factories for logic expressions over basic objects.
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
class LogicExpressions {
    /**
     * Logic expressions where "true" evaluates to true and "false" evaluates to
     * false. For example:
     *
     * (true | false) & true
     *
     * This logic expression is trivial because it's value is independent of the
     * object it is applied to.
     */
    public static LogicExpression<String> trivial(final String expr) {
        return new LogicExpression<String>(expr) {
            @Override
            public Expression.Arg<String> factory(final String string) {
                return new Expression.Arg.Pred<String>(string) {
                    @Override
                    public boolean apply(String entity) {
                        return "true".equals(string);
                    }
                };
            }
        };
    }

    /**
     * Logic expressions where tokens are strings.  A token is true if it
     * matches the input string.
     */
    public static LogicExpression<String> stringMatch(final String expr) {
        return new LogicExpression<String>(expr) {
            @Override
            public Expression.Arg<String> factory(final String token) {
                return new Expression.Arg.Pred<String>(token) {
                    final String string = token.substring(1, token.length() - 1);

                    @Override
                    public boolean apply(String entity) {
                        return entity.equals(string);
                    }
                };
            }
        };
    }
}
