package ca.pgx.client

import java.util.UUID
import org.rogach.scallop._

/**
 * Parses command line arguments and provides a list of expected args and types.
 */
class ClientCommandLineConf(args: Array[String], commandName: String = "")
  extends ScallopConf(args: Array[String], commandName: String) {

  val shutdown = new Subcommand("shutdown") {
    val abort = opt[Boolean]("abort", required = false, descr = "shutdown abort to force immediate shutdown.")
  }

  val restart = new Subcommand("restart") {
    val abort = opt[Boolean]("abort", required = false, descr = "restart abort to force immediate restart.")
  }

  val sleep = new Subcommand("sleep")

  val wakeup = new Subcommand("wakeup")

  val status = new Subcommand("status")

  val job = new Subcommand("job") {
    val description = opt[String]("description", required = true, descr = "Job name/description - makes it easy to find in logs.")
    val executionTime = opt[Long]("millis", required = false, descr = "time in millis to run this job (simul).")
  }

  //  val verbose = opt[Boolean]("verbose", descr = "use more verbose output")
  val waitforreply = opt[Boolean]("wait", descr = "wait for the server reply.")

  //verify
}
