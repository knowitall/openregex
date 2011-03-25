package edu.washington.cs.knowitall.commonlib.logic;


public class LogicException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogicException(String message) {
        super(message);
    }

    public static class ApplyLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public ApplyLogicException(String message) {
            super(message);
        }
    }

    public static class CompileLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public CompileLogicException(String message) {
            super(message);
        }
    }

    public static class TokenizeLogicException extends LogicException {
        private static final long serialVersionUID = 1L;

        public TokenizeLogicException(String message) {
            super(message);
        }
    }
}
