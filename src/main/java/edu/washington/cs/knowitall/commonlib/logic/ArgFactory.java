package edu.washington.cs.knowitall.commonlib.logic;

import edu.washington.cs.knowitall.commonlib.logic.LogicExpression.TokenizeLogicException;

public abstract class ArgFactory<E> {
    public abstract Tok.Arg<E> buildArg(String string) throws TokenizeLogicException;
}