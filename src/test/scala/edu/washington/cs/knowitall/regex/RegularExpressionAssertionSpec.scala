package edu.washington.cs.knowitall.regex
import org.junit.runner.RunWith
import edu.washington.cs.knowitall.regex.Expression.BaseExpression
import scala.collection.JavaConversions._
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegularExpressionAssertionSpec extends Specification {
  val regexTokens = List("^", "<is>", "<a>", "$")
  val matchTokens = List("this", "is", "a", "test")

  val regex = makeRegex(regexTokens.tail.init.mkString(" "))
  val regexEnd = makeRegex(regexTokens.tail.mkString(" "))
  val regexStart = makeRegex(regexTokens.init.mkString(" "))
  val regexBoth = makeRegex(regexTokens.mkString(" "))

  def evaluate(regex: RegularExpression[String], tokens: List[String], value: Boolean) =
    (if (value) "" else "not ") + "be found in '" + tokens.mkString(" ") + "'" in {
      regex.apply(tokens) must beTrue.iff(value)
    }
    
  
  regex.toString should {
    evaluate(regex, matchTokens, true)
    evaluate(regex, matchTokens.tail, true)
    evaluate(regex, matchTokens.init, true)
  }
  
  regexEnd.toString should {
    evaluate(regexEnd, matchTokens, false)
    evaluate(regexEnd, matchTokens.tail, false)
    evaluate(regexEnd, matchTokens.init, true)
  }
  
  regexStart.toString should {
    evaluate(regexStart, matchTokens, false)
    evaluate(regexStart, matchTokens.tail, true)
    evaluate(regexStart, matchTokens.init, false)
  }
  
  regexBoth.toString should {
    "match 'is a'" in {
      regexBoth.matches(List("is", "a")) must beTrue
    }
    evaluate(regexBoth, matchTokens, false)
    evaluate(regexBoth, matchTokens.tail, false)
    evaluate(regexBoth, matchTokens.init, false)
  }

  def makeRegex(input: String) = {
    new RegularExpression[String](input,
      new ExpressionFactory[String]() {
        override def create(string: String): BaseExpression[String] = {
          new BaseExpression[String](string) {
            override def apply(token: String): Boolean = token == string;
          };
        }
      })
  }
}