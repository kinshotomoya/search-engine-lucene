scalaVersion := "2.13.8"
organization := "kinsho-tomoya"
// このversionを使うには、java17必要か
val luceneVersion = "9.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "root",
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-core" % luceneVersion,
      "org.apache.lucene" % "lucene-queryparser" % luceneVersion
    )
  )
