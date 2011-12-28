package edu.washington.cs.knowitall.commonlib.logic;


public class LogicException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogicException(String message) {
        super(message);
    }

    /***
     * Exception while applying an expression to an object.
     * @author schmmd
     *
     */
    public static class ApplyLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public ApplyLogicException(String message) {
            super(message);
        }
    }

    /***
     * Exception while converting the tokens into a valid expression.
     * @author schmmd
     *
     */
    public static class CompileLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public CompileLogicException(String message) {
            super(message);
        }
    }

    /***
     * Exception while tokenizing the logic expression string.
     * @author schmmd
     *
     */
    public static class TokenizeLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public TokenizeLogicException(String message) {
            super(message);
        }
    }
}
