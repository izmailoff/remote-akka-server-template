resolvers += Classpaths.typesafeResolver

// TODO: these seem to have no effect - take a look at it later. Added oracle driver dep resolver
// directly to Build.scala

resolvers ++= Seq(
  "oracle driver repo" at "http://dist.codehaus.org/mule/dependencies/maven2",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
  "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots",
  "Scala Tools Releases" at "http://scala-tools.org/repo-releases",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases"
)

// SBT idea plugin for generating IntelliJ Idea project. Use 'gen-idea' in SBT cmd.
// FROM: https://github.com/mpeltonen/sbt-idea
// TASKS: gen-idea
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")


// SBT eclipse plugin for generating Eclipse project. Simply run 'eclipse' in SBT cmd.
// FROM: https://github.com/typesafehub/sbteclipse
// TASKS: eclipse;
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.3.0")


// SBT plugin for running webapp in Jetty container for development.
// FROM: https://github.com/JamesEarlDouglas/xsbt-web-plugin
// TASKS: container:start; container:stop; container:reload /;
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")


// SBT jar plugin for packaging all dependencies into a single jar (1 jar per subproject).
// To package it run 'one-jar'. It creates 2 jars: one with all dependencies and one without.
addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

