package edu.washington.cs.knowitall.regex
import org.junit.runner.RunWith
import edu.washington.cs.knowitall.regex.Expression.BaseExpression
import scala.collection.JavaConversions._
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegularExpressionNamedGroupTest extends Specification {
  val regex = RegularExpressions.word("(<subject>: <I> | (?: <The> (<subjadj>: <crazy>)? <Mariners>)) <know> <all> <of> (<poss>: <her> | (?: <the> (<possadj>: <dirty>?) <King> <'s>)) <secrets>")
      
  regex.toString should {
    "match" in {
      regex.apply("I know all of her secrets".split(" ").toList) must beTrue
      regex.apply("The Mariners know all of her secrets".split(" ").toList) must beTrue
      regex.apply("The Mariners know all of the dirty King 's secrets".split(" ").toList) must beTrue
      regex.apply("The Mariners know all of the King 's secrets".split(" ").toList) must beTrue
      regex.apply("The crazy Mariners know all of the King 's secrets".split(" ").toList) must beTrue
    }
    
    "yield the correct groups" in {
      val m = regex.find("The crazy Mariners know all of the King 's secrets".split(" ").toList)
      m.groups().size() must_== 5
      m.group("subject").text must_== "The crazy Mariners"
      m.group("subjadj").text must_== "crazy"
      m.group("poss").text must_== "the King 's"
      m.group("possadj").text must_== ""
    }
    
    "yield the correct groups" in {
      val m = regex.find("The Mariners know all of her secrets".split(" ").toList)
      m.groups().size() must_== 3
      m.group("subject").text must_== "The Mariners"
      m.group("poss").text must_== "her"
    }
  }
}
