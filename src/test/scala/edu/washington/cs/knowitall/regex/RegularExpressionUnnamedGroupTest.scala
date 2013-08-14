package edu.washington.cs.knowitall.regex
import org.junit.runner.RunWith
import edu.washington.cs.knowitall.regex.Expression.BaseExpression
import scala.collection.JavaConversions._
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegularExpressionTest extends Specification {
  val regex = RegularExpressions.word("<this> <is> (((?:(?: <a> <very>+) | <an>) <amazing>? <new>{1,3}) | (?: <a> <many>* <centuries> <old>)) <test>")

  regex.toString should {
    "match" in {
      regex.apply("this is a very very very amazing new test".split(" ").toList) must beTrue
      regex.apply("this is a very new test".split(" ").toList) must beTrue
      regex.apply("this is an amazing new test".split(" ").toList) must beTrue
      regex.apply("this is a centuries old test".split(" ").toList) must beTrue
      regex.apply("this is a many many centuries old test".split(" ").toList) must beTrue
      regex.apply("this is a very new test".split(" ").toList) must beTrue
      regex.apply("this is a very new new test".split(" ").toList) must beTrue
      regex.apply("this is a very new new new test".split(" ").toList) must beTrue
      regex.apply("this is a very new new new new test".split(" ").toList) must beFalse
    }

    "not match" in {
      regex.apply("this is a amazing new test".split(" ").toList) must beFalse
    }

    "yield the correct groups" in {
      val m = regex.find("this is a very very very amazing new test".split(" ").toList)
      m.groups().size() must_== 3
      m.groups().get(1).text must_== "a very very very amazing new"
      m.groups().get(2).text must_== "a very very very amazing new"
    }

    "yield the correct groups" in {
      val m = regex.find("this is a centuries old test".split(" ").toList)
      m.groups().size() must_== 2
      m.groups().get(1).text must_== "a centuries old"
    }
  }
}
