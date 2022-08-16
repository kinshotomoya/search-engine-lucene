scalaVersion := "2.13.8"
organization := "kinsho-tomoya"
// このversionを使うには、java17必要か
val luceneVersion = "8.11.2"

lazy val root = (project in file("."))
  .settings(
    name := "root",
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-core" % "9.3.0", // https://mvnrepository.com/artifact/org.apache.lucene/lucene-core
      "org.apache.lucene" % "lucene-queryparser" % "9.3.0", // https://mvnrepository.com/artifact/org.apache.lucene/lucene-queryparser
      "org.apache.lucene" % "lucene-analysis-kuromoji" % "9.3.0", // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analysis-kuromoji/9.3.0
      "org.apache.lucene" % "lucene-backward-codecs" % "9.3.0", // https://mvnrepository.com/artifact/org.apache.lucene/lucene-backward-codecs/9.3.0
      "org.apache.lucene" % "lucene-analysis-icu" % "9.3.0", // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analysis-icu/9.3.0
      "org.apache.lucene" % "lucene-analyzers-kuromoji" % luceneVersion, // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analyzers-kuromoji/8.11.2
      "org.apache.lucene" % "lucene-analysis-icu" % luceneVersion // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analyzers-icu
    )
  )
