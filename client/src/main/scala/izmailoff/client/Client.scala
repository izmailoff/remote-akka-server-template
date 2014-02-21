package izmailoff.client

import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }
import java.util.UUID
import java.net.URI
import akka.actor.ActorSelection
import izmailoff.common.log.CustomLogSource._
import akka.event.Logging
import izmailoff.common.messages.Messages._
import akka.actor.ActorLogging
import izmailoff.common.util.Environment
import akka.remote.AssociationEvent
import akka.remote.DisassociatedEvent

/**
 * Client configuration and actor management system.
 */
class Client {
  import ClientRunner._
  
  /**
   * Returns unique local actor or system name with base name from conf.
   *
   * The purpose of this method is for the server to be able to differentiate
   * between clients that have the same logical path to avoid confusion
   * between multiple clients.
   */
  def makeUniqueName(baseName: String) =
    s"$baseName-${UUID.randomUUID}"

  val localActorSystemName = makeUniqueName(conf.getString("akkaNames.local.system"))
  val localActorName = makeUniqueName(conf.getString("akkaNames.local.actor"))

  val system = ActorSystem(localActorSystemName, conf)
  val log = Logging(system, this)
  import log._
  info(s"Created actor system [$system].")
  debug(s"All local actor system settings [${system.settings}].")

  val clientCommunicator = system.actorOf(Props[ClientActor], localActorName)
  info(s"Created local actor [$clientCommunicator].")

  /**
   * Sends a message to Akka server.
   */
  def sendRequest(message: RequestMessage): Unit = {
    info(s"Sending request to server: [$message].")
    clientCommunicator ! message
    info("Request has been sent.")
  }

  def getConnectionStatus: Unit = { // FIXME:   : ConnectionStatus = {
    clientCommunicator ! GetConnectionStatus
  }

  system.eventStream.subscribe(clientCommunicator, classOf[AssociationEvent])
  info(s"Registered remote lifecycle event listeners.")

}

sealed trait ConnectionStatus
case object GetConnectionStatus extends ConnectionStatus
case object ConnectionStatusConnected extends ConnectionStatus
case object ConnectionStatusDisconnected extends ConnectionStatus

