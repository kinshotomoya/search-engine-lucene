scalaVersion := "2.13.8"
organization := "kinsho-tomoya"
// このversionを使うには、java17必要か
val luceneVersion = "8.11.2"

lazy val root = (project in file("."))
  .settings(
    name := "root",
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-core" % luceneVersion, // https://mvnrepository.com/artifact/org.apache.lucene/lucene-core
      "org.apache.lucene" % "lucene-queryparser" % luceneVersion, // https://mvnrepository.com/artifact/org.apache.lucene/lucene-queryparser
      "org.apache.lucene" % "lucene-analyzers-kuromoji" % luceneVersion, // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analyzers-kuromoji/8.11.2
      "org.apache.lucene" % "lucene-analyzers-icu" % luceneVersion
    )
  )
