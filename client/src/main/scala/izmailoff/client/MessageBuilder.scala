package izmailoff.client

import java.util.UUID
import java.net.URI
import izmailoff.common.messages.Messages._
import izmailoff.common.messages.Messages.ServiceOperationType._
import ClientRunner.username

/**
 * Builds messages from command line arguments that later will be sent to the Akka server.
 */
object MessageBuilder {

  /**
   * Generates a Request with a random id.
   */
  def makeRequest = Request(UUID.randomUUID)

  /**
   * transforms a List of requestIds to a list of Request messages
   */
  def getRequestList(requestIds: Option[List[String]]) = requestIds match {
    case Some(requestIds) => requestIds.map(r => Request(UUID.fromString(r)))
    case _ => List()
  }

  /**
   * Returns the appropriate RequestMessage to be sent to Akka server based on command line args provided.
   */
  def getMessageFromConf(conf: ClientCommandLineConf): Option[RequestMessage] =
    if (conf.subcommands.contains(conf.status)) checkSystemStatusMessage
    else if (conf.subcommands.contains(conf.shutdown)) shutdownMessage(conf.shutdown.abort())
    else if (conf.subcommands.contains(conf.restart)) restartMessage(conf.restart.abort())
    else if (conf.subcommands.contains(conf.sleep)) sleepMessage
    else if (conf.subcommands.contains(conf.wakeup)) wakeupMessage
    else if (conf.subcommands.contains(conf.job)) runJobMessage(conf.job.description(),
      if (conf.job.executionTime.isSupplied) conf.job.executionTime() else 0)
    else None

  /**
   * Constructs a message of type Status
   */
  def checkSystemStatusMessage =
    Some(Status(username, makeRequest))

  /**
   * Constructs a Shutdown message
   */
  def shutdownMessage(abort: Boolean) =
    Some(Shutdown(username, makeRequest, if (abort) ABORT else NORMAL))

  /**
   * Constructs a Restart message
   */
  def restartMessage(abort: Boolean) =
    Some(Restart(username, makeRequest, if (abort) ABORT else NORMAL))

  /**
   * Constructs a Sleep message
   */
  def sleepMessage =
    Some(Sleep(username, makeRequest))

  /**
   * Constructs a Wakeup message
   */
  def wakeupMessage =
    Some(Wakeup(username, makeRequest))

  /**
   * Request server to run a simulated job.
   */
  def runJobMessage(description: String, millis: Long) =
    Some(RunJob(makeRequest, username, description, millis))
}
