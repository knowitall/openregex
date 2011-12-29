package edu.washington.cs.knowitall.logic;

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import edu.washington.cs.knowitall.logic.Tok.Arg;

@RunWith(classOf[JUnitRunner])
class LogicSpec extends Specification {
  "order of operations" should {
    "infer the correct parenthesis" in {
      logic("false & false & false").toString() must_== "(false & (false & false))"
      logic("false & false | false").toString() must_== "((false & false) | false)"
      logic("false | false & false").toString() must_== "(false | (false & false))"
    }
  }

  "two variable logic expressions" should {
    "evaluate correctly" in {
      forall(0 until 4) { i =>
        val a = (i & 1) != 0;
        val b = (i & 2) != 0;

        logic(expression("a | b", a, b))("true") must_== (a | b)
        logic(expression("a & b", a, b))("true") must_== (a & b)
      }
    }
  }

  "three variable logic expressions" should {
    "evaluate correctly" in {
      forall(0 until 8) { i =>
        val a = (i & 1) != 0
        val b = (i & 2) != 0
        val c = (i & 4) != 0

        logic(expression("a | (b & c)", a, b, c))("true") must_== (a | (b & c))
        logic(expression("a & (b & c)", a, b, c))("true") must_== (a & (b & c))
        logic(expression("a & (b | c)", a, b, c))("true") must_== (a & (b | c))
        logic(expression("a | (b | c)", a, b, c))("true") must_== (a | (b | c))
      }
    }
  }

  "four variable logic expressions" should {
    "evaluate correctly" in {
      forall (0 until 16) { i =>
        val a = (i & 1) != 0;
        val b = (i & 2) != 0;
        val c = (i & 4) != 0;
        val d = (i & 8) != 0;
            
        logic(expression("a | (b & c & d)", a, b, c, d))("true") must_== (a | (b & c & d))
        logic(expression("a | (b & c | d)", a, b, c, d))("true") must_== (a | (b & c | d))
        logic(expression("a | (b | c & d)", a, b, c, d))("true") must_== (a | (b | c & d))
        logic(expression("a | (b | c | d)", a, b, c, d))("true") must_== (a | (b | c | d))
        logic(expression("a & (b & c & d)", a, b, c, d))("true") must_== (a & (b & c & d))
        logic(expression("a & (b & c | d)", a, b, c, d))("true") must_== (a & (b & c | d))
        logic(expression("a & (b | c & d)", a, b, c, d))("true") must_== (a & (b | c & d))
        logic(expression("a & (b | c | d)", a, b, c, d))("true") must_== (a & (b | c | d))
        logic(expression("(a | b) & (c | d)", a, b, c, d))("true") must_== ((a | b) & (c | d))
        logic(expression("(a & b) | (c & d)", a, b, c, d))("true") must_== ((a & b) | (c & d))
        logic(expression("!(a | b) & (c | d)", a, b, c, d))("true") must_== (!(a | b) & (c | d))
        logic(expression("(a | b) & !(c | d)", a, b, c, d))("true") must_== (a | b) & !(c | d)
        logic(expression("!((a | b) & !(c | d))", a, b, c, d))("true") must_== !((a | b) & !(c | d))
      }
    }
  }

  def expression(expr: String, varargs: Boolean*) =
    (expr /: varargs.zipWithIndex) { case (arg, i) =>
      val v = 'a' + i.asInstanceOf[Char];
      expr.replace(v.toString(), arg.toString);
    }
    
  def logic(logic: String) = 
    LogicExpression.compile(logic, new ArgFactory[String]() {
        override def create(string: String) = 
          new Arg.Pred[String](string) {
            override def apply(entity: String) = "true".equals(string);
          }})
}
