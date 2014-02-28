package izmailoff.client

import org.rogach.scallop._

/**
 * Parses command line arguments and provides a list of expected args and types.
 */
class ClientCommandLineConf(args: Array[String], commandName: String = "")
  extends ScallopConf(args: Array[String], commandName: String) {

  val shutdown = new Subcommand("shutdown") {
    val abort = opt[Boolean]("abort", 'a', "shutdown abort to force immediate shutdown.", required = false) // FIXME: throws for some reason
  }

  val restart = new Subcommand("restart") {
    val abort = opt[Boolean]("abort", 'a', descr = "restart abort to force immediate restart.", required = false)
  }

  val sleep = new Subcommand("sleep")

  val wakeup = new Subcommand("wakeup")

  val status = new Subcommand("status")

  //(name, short, descr, default, validate, required, argName, hidden, noshort)
  val job = new Subcommand("job") {
    val description = opt[String]("description", 'd', "Job name/description - makes it easy to find in logs.", required = true)
    val times = opt[Int]("times", 't', "N times to send request.", Some(1), required = false)
    val executionTime = opt[Long]("millis", 'm', "time in millis to run this job (simul).", Some(0), required = false)
  }

  //  val verbose = opt[Boolean]("verbose", descr = "use more verbose output")
  val waitforreply = opt[Boolean]("wait", descr = "wait for the server reply.")

  //verify
}
