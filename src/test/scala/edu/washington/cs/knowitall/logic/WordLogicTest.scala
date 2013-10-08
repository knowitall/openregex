package edu.washington.cs.knowitall.regex

import scala.collection.JavaConverters._

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import edu.washington.cs.knowitall.logic._
import edu.washington.cs.knowitall.logic.Expression.Arg

@RunWith(classOf[JUnitRunner])
class WordLogicTest extends Specification {
  case class WordToken(string: String, postag: String, chunk: String)

  "README logic example" should {
    "work" in {
      def create(string: String) = {
        new LogicExpressionParser[WordToken] {
          override def factory(expr: String) = {
            new Arg.Pred[WordToken](expr) {
              val Array(part, quotedValue) = expr.split("=")
              val value = quotedValue.drop(1).take(quotedValue.size - 2)
              override def apply(entity: WordToken) = part match {
                case "string" => entity.string == value
                case "postag" => entity.postag == value
                case "chunk" => entity.chunk == value
              }
            }
          }
        }.parse(string)
      }

      val logic = create("string='the' | postag='JJ'")
      logic.apply(WordToken("the", "foo", "bar")) must beTrue
      logic.apply(WordToken("foo", "JJ", "bar")) must beTrue
      logic.apply(WordToken("foo", "bar", "baz")) must beFalse
    }
  }
}
