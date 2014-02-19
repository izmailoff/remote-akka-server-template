package izmailoff

import sbt._
import sbt.Keys._
import sbt.Tests
import java.io.{BufferedReader, InputStreamReader, FileInputStream, File}
import java.nio.charset.Charset
import java.util.Properties
import com.github.retronym.SbtOneJar.oneJarSettings
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object MyBuild extends Build {

  lazy val root = Project(id = "root",
                          base = file("."))
                          .aggregate(common, client, server)

  /**
   * All unit tests should run in parallel by default. This filter selects such tests
   * and afterwards parallel execution settings are applied.
   * Thus don't include word 'Integration' in unit test suite name.
   *
   * SBT command: test
   */
  def parFilter(name: String): Boolean = !(name contains "Integration")

  /**
   * Integration tests should run sequentially because they take lots of resources,
   * or shared use of resources can cause conflicts.
   * 
   * SBT command: serial:test
   **/
  def serialFilter(name: String): Boolean = (name contains "Integration")

  // config for serial execution of integration tests
  lazy val Serial = config("serial") extend(Test)

  lazy val client = Project(id = "client",
                            base = file("client")) settings(clientSettings: _*) dependsOn(common)

  lazy val server = Project(id = "server",
                            base = file("server")) settings(serverSettings: _*) dependsOn(common)

                          /*.configs(Serial)
                          .settings(inConfig(Serial)(Defaults.testTasks) : _*)
                          .settings(
                            testOptions in Test := Seq(Tests.Filter(parFilter)),
                            testOptions in Serial := Seq(Tests.Filter(serialFilter))
                          )
                          .settings( parallelExecution in Serial := false : _*)
                          .settings(...)*/
 
  lazy val common = Project(id = "common",
                            base = file("common")) settings(commonSettings: _*)

  // packages jar with all dependencies included
  //lazy val singleJarSettings = settings ++ oneJarSettings

  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/build.properties")

  lazy val commonSettings =
  settings ++
  Seq(libraryDependencies ++=
        Dependencies.cmd)

  lazy val clientSettings =
  settings ++
  Seq(libraryDependencies ++=
        Dependencies.akka) ++
//        Dependencies.cmd) ++
  oneJarSettings

  lazy val serverSettings =
  settings ++
  Seq(libraryDependencies ++=
        Dependencies.akka) ++
  oneJarSettings

  lazy val buildSettings = Seq(
    organization := "izmailoff",
    version      := "0.1",
    scalaVersion := System.getProperty("scalaVersion", "2.10.3"),
    exportJars := true,
    EclipseKeys.withSource := true
  )

  override lazy val settings = 
  super.settings ++
  buildSettings ++
  defaultSettings ++
  //Seq(libraryDependencies ++= Dependencies.testKit ++ Dependencies.lift ++ Dependencies.log) ++
  Seq(
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  )

  lazy val defaultSettings = Seq(
    // Compile options
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation",
      "-feature", "-unchecked", "-language:_"),
/* Uncomment to see reflection uses: "-Xlog-reflective-calls",
This generates lots of noise in build: "-Ywarn-adapted-args",
*/ 
    // scaladoc options - for now inheritance diagrams are not generated.
    // "-diagrams" option requires graphviz package/app to be installed. This option is for scala version >= 2.10
    scalacOptions in Compile in doc ++= Seq("-diagrams", "-implicits"),

    javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation"),

    ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,

    // Test settings

    // show full stack traces and test case durations
    // FIXME: fails to run test in client proj: testOptions in Test += Tests.Argument("-oDF"),

    parallelExecution in Test := System.getProperty("parallelExecution", "true").toBoolean,
    logBuffered in Test := System.getProperty("logBufferedTests", "true").toBoolean
  )

  def loadSystemProperties(fileName: String): Unit = {
    import scala.collection.JavaConverters._
    val file = new File(fileName)
    if (file.exists()) {
      println("Loading system properties from file `" + fileName + "`")
      val in = new InputStreamReader(new FileInputStream(file), "UTF-8")
      val props = new Properties
      props.load(in)
      in.close()
      sys.props ++ props.asScala
    }
  }

// Dependencies

object Dependencies {

  object Compile {
    // Compile

    // AKKA
    val akkaSystem = Seq(
      "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3",
      "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3",
      "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.3",
      "com.typesafe.akka" % "akka-slf4j_2.10" % "2.2.3",
      "com.typesafe.akka" % "akka-testkit_2.10" % "2.2.3" % "test"
    )

    // LOG
    val logback      = "ch.qos.logback" % "logback-classic"              % "1.0.13"
    val logbackJavaCompiler = "org.codehaus.janino" % "janino" % "2.6.1"

    // CMD
    val cmdLineArgParsing = "org.rogach" %% "scallop" % "0.9.5"

    // DATE
    //val dateScala = "org.scalaj" % "scalaj-time_2.10.0-M7" % "0.6"

    object Test {
      val junit        = "junit"                       % "junit"                        % "4.11"             % "test" // Common Public License 1.0
//      val logback      = "ch.qos.logback"              % "logback-classic"              % "1.0.7"            % "test" // EPL 1.0 / LGPL 2.1
      val mockito      = "org.mockito"                 % "mockito-all"                  % "1.8.1"            % "test" // MIT
      // changing the scalatest dependency must be reflected in akka-docs/rst/dev/multi-jvm-testing.rst
      val scalatest    = "org.scalatest"              %% "scalatest"                    % "2.0.M6-SNAP5"      % "test" // ApacheV2
//      val scalacheck   = "org.scalacheck"             %% "scalacheck"                   % "1.10.0"           % "test" // New BSD
      val log4j        = "log4j"                       % "log4j"                        % "1.2.14"           % "test" // ApacheV2
      val specs2 = "org.specs2"        %% "specs2"             % "2.1.1"           % "test"
    }
  }
  
  import Compile._

  val testKit = Seq(Test.junit, Test.scalatest, Test.specs2)

  val akka = Compile.akkaSystem

  val log = Seq(Compile.logback, Compile.logbackJavaCompiler)

  val cmd = Seq(Compile.cmdLineArgParsing)

  //val date = Seq(dateScala)

}

}

