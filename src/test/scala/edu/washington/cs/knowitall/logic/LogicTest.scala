package edu.washington.cs.knowitall.logic;

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.ScalaCheck

import edu.washington.cs.knowitall.logic.Expression.Arg;

@RunWith(classOf[JUnitRunner])
class LogicTest extends Specification with ScalaCheck {
  "order of operations" should {
    "infer the correct parenthesis" in {
      compile("false & false & false").toString() must_== "(false & (false & false))"
      compile("false & false | false").toString() must_== "((false & false) | false)"
      compile("false | false & false").toString() must_== "(false | (false & false))"
    }
  }

  def eval(expr: String,  f: (Boolean, Boolean) => Boolean) =
    "evaluate ("+expr+") correctly" in {
      check { (a: Boolean, b: Boolean) => compile(substitute(expr, a, b))(null) must_== f(a, b) }
    }
  def eval(expr: String,  f: (Boolean, Boolean, Boolean) => Boolean) =
    "evaluate ("+expr+") correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean) => compile(substitute(expr, a, b, c))(null) must_== f(a, b, c) }
    }
  def eval(expr: String,  f: (Boolean, Boolean, Boolean, Boolean) => Boolean) =
    "evaluate ("+expr+") correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => compile(substitute(expr, a, b, c, d))(null) must_== f(a, b, c, d) }
    }

  "two variable logic expressions" should {
    eval("a | b", (a: Boolean, b: Boolean) => a | b)
    eval("a & b", (a: Boolean, b: Boolean) => a & b)
  }

  "three variable logic expressions" should {
    eval("(a | (b & c))", (a: Boolean, b: Boolean, c: Boolean) => (a | (b & c)))
    eval("(a & (b & c))", (a: Boolean, b: Boolean, c: Boolean) => (a & (b & c)))
    eval("(a & (b | c))", (a: Boolean, b: Boolean, c: Boolean) => (a & (b | c)))
    eval("(a | (b | c))", (a: Boolean, b: Boolean, c: Boolean) => (a | (b | c)))
  }

  "four variable logic expressions" should {
    eval("(a | (b & c & d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a | (b & c & d)))
    eval("(a | (b & c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a | (b & c | d)))
    eval("(a | (b | c & d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a | (b | c & d)))
    eval("(a | (b | c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a | (b | c | d)))
    eval("(a & (b & c & d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a & (b & c & d)))
    eval("(a & (b & c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a & (b & c | d)))
    eval("(a & (b | c & d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a & (b | c & d)))
    eval("(a & (b | c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (a & (b | c | d)))
    eval("((a | b) & (c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => ((a | b) & (c | d)))
    eval("((a & b) | (c & d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => ((a & b) | (c & d)))
    eval("(!(a | b) & (c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (!(a | b) & (c | d)))
    eval("((a | b) & !(c | d))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => ((a | b) & !(c | d)))
    eval("(!((a | b) & !(c | d)))", (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => (!((a | b) & !(c | d))))
  }

  def substitute(expr: String, varargs: Boolean*) =
    (expr /: varargs.zipWithIndex) { case (expr, (arg, i)) =>
      val v = ('a' + i).toChar;
      expr.replace(v.toString(), arg.toString);
    }

  def compile(logic: String) = LogicExpressions.trivial(logic)
}
