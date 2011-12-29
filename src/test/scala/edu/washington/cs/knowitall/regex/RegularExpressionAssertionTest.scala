package edu.washington.cs.knowitall.regex
import org.junit.runner.RunWith
import edu.washington.cs.knowitall.regex.Expression.BaseExpression
import scala.collection.JavaConversions._
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegularExpressionAssertionTest extends Specification {
  val regexTokens = List("^", "<is>", "<a>", "$")
  val matchTokens = List("this", "is", "a", "test")

  val regex = RegularExpressions.word(regexTokens.tail.init.mkString(" "))
  val regexEnd = RegularExpressions.word(regexTokens.tail.mkString(" "))
  val regexStart = RegularExpressions.word(regexTokens.init.mkString(" "))
  val regexBoth = RegularExpressions.word(regexTokens.mkString(" "))

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
}
