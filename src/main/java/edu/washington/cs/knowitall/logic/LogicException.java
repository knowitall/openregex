package edu.washington.cs.knowitall.logic;

/**
 *
 * @author Michael Schmitz <schmmd@cs.washington.edu>
 */
public class LogicException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogicException(String message) {
        super(message);
    }

    public LogicException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Exception while applying an expression to an object.
     */
    public static class ApplyLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public ApplyLogicException(String message, Exception e) {
            super(message, e);
        }

        public ApplyLogicException(String message) {
            super(message);
        }
    }

    /**
     * Exception while converting the tokens into a valid expression.
     */
    public static class CompileLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public CompileLogicException(String message, Exception e) {
            super(message, e);
        }

        public CompileLogicException(String message) {
            super(message);
        }
    }

    /**
     * Exception while tokenizing the logic expression string.
     */
    public static class TokenizeLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public TokenizeLogicException(String message, Exception e) {
            super(message, e);
        }

        public TokenizeLogicException(String message) {
            super(message);
        }
    }
}
