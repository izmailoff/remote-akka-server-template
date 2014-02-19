package ca.pgx.client

import java.util.UUID
import java.net.URI
import izmailoff.common.messages.Messages._

/**
 * Builds messages from command line arguments that later will be sent to the Akka server.
 */
object MessageBuilder {

  /**
   * Generates a Request with a random id.
   */
  def makeRequest = Request(UUID.randomUUID())

  /**
   * transforms a List of requestIds to a list of Request messages
   */
  def getRequestList(requestIds: Option[List[String]]) = requestIds match {
    case Some(requestIds) => requestIds.map(r => Request(UUID.fromString(r)))
    case _ => List()
  }

  /**
   * returns the appropriate RequestMessage based on a [[ClientCommandLineConf]] object.
   */
  def getMessageFromConf(configuration: Option[ClientCommandLineConf]): Option[RequestMessage] =
    configuration match {
      case Some(conf) =>
        if (conf.status.isSupplied) {
          checkSystemStatusMessage()
        } else if (conf.subcommands.contains(conf.shutdown)) {
          shutdownMessage(conf.shutdown.abort.isSupplied)
        } else if (conf.subcommands.contains(conf.restart)) {
          restartMessage(conf.restart.abort.isSupplied)
        } else if (conf.sleep.isSupplied) {
          sleepMessage()
        } else if (conf.wakeup.isSupplied) {
          wakeupMessage()
        } else {
          None
        }
      case _ => None
    }

  /**
   * Constructs a message of type Status
   */
  def checkSystemStatusMessage() = {
    Some(Status("shell", makeRequest))
  }

  /**
   * Constructs a Shutdown message
   */
  def shutdownMessage(abort: Boolean) = {
    Some(Shutdown("shell", makeRequest, if (abort) ServiceOperationType.ABORT else ServiceOperationType.NORMAL))
  }

  /**
   * Constructs a Restart message
   */
  def restartMessage(abort: Boolean) = {
    Some(Restart("shell", makeRequest, if (abort) ServiceOperationType.ABORT else ServiceOperationType.NORMAL))
  }

  /**
   * Constructs a Sleep message
   */
  def sleepMessage() = {
    Some(Sleep("shell", makeRequest))
  }

  /**
   * Constructs a Wakeup message
   */
  def wakeupMessage() = {
    Some(Wakeup("shell", makeRequest))
  }
}


