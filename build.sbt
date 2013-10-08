organization := "edu.washington.cs.knowitall"

name := "openregex"

description := "OpenRegex is an efficient and flexible library for running regular expressions over sequences of user-defined objects."

version := "1.1.2-SNAPSHOT"

libraryDependencies ++= Seq("com.google.code.findbugs" % "jsr305" % "2.0.1",
    "com.google.guava" % "guava" % "15.0",
    "org.scala-lang" % "scala-library" % "2.10.2" % "test",
    "junit" % "junit" % "4.10" % "test",
    "org.specs2" % "specs2_2.10" % "2.2.2" % "test",
    "org.scalacheck" % "scalacheck_2.10" % "1.10.1" % "test")

licenses := Seq("LGPL (GNU Lesser General Public License)" -> url("http://www.gnu.org/licenses/lgpl.html"))

homepage := Some(url("https://github.com/knowitall/openregex"))

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>https://github.com/knowitall/openregex</url>
    <connection>scm:git://github.com/knowitall/openregex.git</connection>
    <developerConnection>scm:git:git@github.com:knowitall/openregex.git</developerConnection>
    <tag>HEAD</tag>
  </scm>
  <developers>
   <developer>
      <name>Michael Schmitz</name>
    </developer>
  </developers>)
