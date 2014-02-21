package izmailoff.client

import com.typesafe.config.ConfigFactory
import izmailoff.common.util.Environment
import izmailoff.common.messages.Messages._

/**
 * Command line client application for sending requests to job server.
 * It parses command line options and constructs a request to be sent.
 * If request can be constructed according to requirements it will be send
 * to the server. Application might or might not wait for server reply
 * depending on the command line flag provided.
 */
object ClientRunner extends App {

  val conf = ConfigFactory.load.getConfig("client")
  val username = Environment.currentOsUser
  val cmdArgs = new ClientCommandLineConf(args)

  MessageBuilder.getMessageFromConf(cmdArgs) match {
    case Some(message) =>
      val app = new Client
      //app.sendRequest(message)
      for {
        i <- 1 to cmdArgs.job.times()
        _ = app.getConnectionStatus
        _ = app.log.info(s"MESSAGE# $i")
        _ = Thread.sleep(2000)
        m = message match {
          case job: RunJob => job.copy(description = s"$i: ${job.description}")
          case other => other
        }
        _ = app.sendRequest(m)
      } ()
      app.clientCommunicator ! "EXIT"
    case _ =>
      sys.error("Incorrect arguments provided.")
      cmdArgs.printHelp
      sys.exit(1)
  }
}