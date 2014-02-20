package ca.pgx.client

import akka.kernel.Bootable
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

/**
 * Client configuration and actor management system.
 */
class Client extends Bootable {
  /**
   * Returns unique local actor or system name with base name from conf.
   *
   * The purpose of this method is for the server to be able to differentiate
   * between clients that have the same logical path to avoid confusion
   * between multiple clients.
   */
  def makeUniqueName(baseName: String) =
    s"$baseName-${UUID.randomUUID}"

  val conf = ConfigFactory.load.getConfig("client")

  val remoteHost = conf.getString("serverParams.remote.hostname")
  val remotePort = conf.getString("serverParams.remote.port")
  val remoteActorSystemName = conf.getString("akkaNames.remote.system")
  val remoteActorName = conf.getString("akkaNames.remote.actor")
  val localActorSystemName = makeUniqueName(conf.getString("akkaNames.local.system"))
  val localActorName = makeUniqueName(conf.getString("akkaNames.local.actor"))

  val system = ActorSystem(localActorSystemName, conf)
  val log = Logging(system, this)
  import log._
  info(s"Created actor system [$system].")
  debug(s"All local actor system settings [${system.settings}].")
  //  info(PrintHelpers.prettyConf(conf))

  val clientCommunicator = system.actorOf(Props[ClientActor], localActorName)
  info("Created local actor [" + clientCommunicator + "].")

  val remoteActor = system.actorSelection(buildRemoteActorConnectString)
  info("Created remote actor [" + remoteActor + "].")

  /**
   * Creates connection string from remote Akka server settings.
   * Resulting string looks something like this:
   * akka://Server@127.0.0.1:2552/user/Server
   */
  def buildRemoteActorConnectString =
    s"akka.ssl.tcp://$remoteActorSystemName@$remoteHost:$remotePort/user/$remoteActorName"

  /**
   * Sends a message to Akka server.
   */
  def sendRequest(message: RequestMessage): Unit = {
    info(s"Sending request to server: [$message].")
    clientCommunicator ! (remoteActor, message)
    info("Request has been sent.")
  }

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

/**
 * Client actor responsible for communicating with Akka server.
 */
class ClientActor extends Actor with ActorLogging {
  import log._

  // TODO: read this from cmd args later. Refactor this if needed
  // maybe it's not this actor responsibility, provide callback for
  // shutdown???
  val waitForJobCompletion = true

  def exitGracefully(): Unit = {
    info("Shutting down client.")
    context.system.shutdown
    sys.exit
  }

  def receive = {
    case (remoteActor: ActorSelection, message) =>
      remoteActor ! message
      if (waitForJobCompletion) {
        info("Client waiting for response...")
      } else {
        info("Client not waiting for response.")
        exitGracefully()
      }
    //    case intermediateResult: IntermediateResult =>
    //      info(s"Got\n[$intermediateResult].\nWaiting for more...")
    case ShutdownAccepted(request, operationMode) =>
      info(s"Server will be shut down with [$operationMode] option as you requested: [$request].")
      exitGracefully()
    case result: ResponseMessage =>
      info(s"Got reply from server: [$result].")
      exitGracefully()
    case unknown =>
      error(s"Unknown response: [$unknown].")
      exitGracefully()
  }
}

/**
 * Command line client application for sending requests to job server.
 * It parses command line options and constructs a request to be sent.
 * If request can be constructed according to requirements it will be send
 * to the server. Application might or might not wait for server reply
 * depending on the command line flag provided.
 */
object LimsReportsClientApp extends App {
  
  val username = Environment.currentOsUser
  val conf = new ClientCommandLineConf(args)

  MessageBuilder.getMessageFromConf(conf) match {
    case Some(message) =>
      val app = new Client
      app.sendRequest(message)
    case _ =>
      sys.error("Incorrect arguments provided.")
      conf.printHelp
      sys.exit(1)
  }
}


