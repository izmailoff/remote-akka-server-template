package ca.pgx.client

import java.util.UUID
import org.rogach.scallop._

class ClientCommandLineConf(args: Array[String], commandName: String = "")
  extends ScallopConf(args: Array[String], commandName: String) {

  val waitforreply = opt[Boolean]("wait", descr = "wait for the server reply.")

  val shutdown = new Subcommand("shutdown") {
    val abort = opt[Boolean]("abort", required = false, descr = "shutdown abort to force immediate shutdown.")
  }

  val restart = new Subcommand("restart") {
    val abort = opt[Boolean]("abort", required = false, descr = "restart abort to force immediate restart.")
  }

  val sleep = opt[Boolean]("sleep", descr = "sleep")

  val wakeup = opt[Boolean]("wakeup", descr = "wakeup")

  val status = opt[Boolean]("status", descr = "system status")

  mutuallyExclusive(sleep, wakeup)

  val verbose = opt[Boolean]("verbose", descr = "use more verbose output")

  //verify
}