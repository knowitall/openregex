package edu.washington.cs.knowitall.logic;

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.ScalaCheck

import edu.washington.cs.knowitall.logic.Tok.Arg;

@RunWith(classOf[JUnitRunner])
class LogicTest extends Specification with ScalaCheck {
  "order of operations" should {
    "infer the correct parenthesis" in {
      logic("false & false & false").toString() must_== "(false & (false & false))"
      logic("false & false | false").toString() must_== "((false & false) | false)"
      logic("false | false & false").toString() must_== "(false | (false & false))"
    }
  }

  "two variable logic expressions" should {
    "evaluate (a | b) correctly" in {
      check { (a: Boolean, b: Boolean) => logic(expression("a | b", a, b))(null) must_== (a | b) }
    }

    "evaluate (a & b) correctly" in {
      check { (a: Boolean, b: Boolean) => logic(expression("a & b", a, b))(null) must_== (a & b) }
    }
  }

  "three variable logic expressions" should {
    "evaluate (a | (b & c)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean) => logic(expression("a | (b & c)", a, b, c))(null) must_== (a | (b & c)) }
    }

    "evaluate (a & (b & c)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean) => logic(expression("a & (b & c)", a, b, c))(null) must_== (a & (b & c)) }
    }

    "evaluate (a & (b | c)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean) => logic(expression("a & (b | c)", a, b, c))(null) must_== (a & (b | c)) }
    }

    "evaluate (a | (b | c)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean) => logic(expression("a | (b | c)", a, b, c))(null) must_== (a | (b | c)) }
    }
  }

  "four variable logic expressions" should {
    "evaluate (a | (b & c & d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a | (b & c & d)", a, b, c, d))(null) must_== (a | (b & c & d)) }
    }
    "evaluate (a | (b & c | d) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a | (b & c | d)", a, b, c, d))(null) must_== (a | (b & c | d)) }
    }
    "evaluate (a | (b | c & d) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a | (b | c & d)", a, b, c, d))(null) must_== (a | (b | c & d)) }
    }
    "evaluate (a | (b | c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a | (b | c | d)", a, b, c, d))(null) must_== (a | (b | c | d)) }
    }
    "evaluate (a & (b & c & d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a & (b & c & d)", a, b, c, d))(null) must_== (a & (b & c & d)) }
    }
    "evaluate (a & (b & c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a & (b & c | d)", a, b, c, d))(null) must_== (a & (b & c | d)) }
    }
    "evaluate (a & (b | c & d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a & (b | c & d)", a, b, c, d))(null) must_== (a & (b | c & d)) }
    }
    "evaluate (a & (b | c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("a & (b | c | d)", a, b, c, d))(null) must_== (a & (b | c | d)) }
    }
    "evaluate ((a | b) & (c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("(a | b) & (c | d)", a, b, c, d))(null) must_== ((a | b) & (c | d)) }
    }
    "evaluate ((a & b) | (c & d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("(a & b) | (c & d)", a, b, c, d))(null) must_== ((a & b) | (c & d)) }
    }
    "evaluate (!(a | b) & (c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("!(a | b) & (c | d)", a, b, c, d))(null) must_== (!(a | b) & (c | d)) }
    }
    "evaluate ((a | b) & !(c | d)) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("(a | b) & !(c | d)", a, b, c, d))(null) must_== (a | b) & !(c | d) }
    }
    "evaluate (!((a | b) & !(c | d))) correctly" in {
      check { (a: Boolean, b: Boolean, c: Boolean, d: Boolean) => logic(expression("!((a | b) & !(c | d))", a, b, c, d))(null) must_== !((a | b) & !(c | d)) }
    }
  }

  def expression(expr: String, varargs: Boolean*) =
    (expr /: varargs.zipWithIndex) { case (expr, (arg, i)) =>
      val v = ('a' + i).toChar;
      expr.replace(v.toString(), arg.toString);
    }
    
  def logic(logic: String) = 
    LogicExpression.compile(logic, new ArgFactory[String]() {
        override def create(string: String) = 
          new Arg.Pred[String](string) {
            override def apply(entity: String) = "true".equals(string);
          }})
}
