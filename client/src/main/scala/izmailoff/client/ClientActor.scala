package izmailoff.client

import akka.actor.Actor
import akka.actor.ActorLogging
import izmailoff.common.messages.Messages._
import akka.remote.DisassociatedEvent
import akka.remote.AssociatedEvent

/**
 * Client actor responsible for communicating with Akka server.
 */
class ClientActor extends Actor with ActorLogging {

  import ClientRunner.conf

  val remoteTransport = conf.getString("serverParams.remote.transport")
  val remoteHost = conf.getString("serverParams.remote.hostname")
  val remotePort = conf.getString("serverParams.remote.port")
  val remoteActorSystemName = conf.getString("akkaNames.remote.system")
  val remoteActorName = conf.getString("akkaNames.remote.actor")
  
  /**
   * Current server connection status. It's updated based on association events that we listen to.
   */
  var connectionStatus: ConnectionStatus = ConnectionStatusDisconnected

  /**
   * Creates connection string from remote Akka server settings.
   *
   * Resulting string looks something like this:
   * akka.ssl.tcp://Server@127.0.0.1:2552/user/Server
   */
  def buildRemoteActorConnectString =
    s"$remoteTransport://$remoteActorSystemName@$remoteHost:$remotePort/user/$remoteActorName"

  val remoteActor = context.system.actorSelection(buildRemoteActorConnectString)
  log.info(s"Created remote actor [$remoteActor].")

  // TODO: read this from cmd args later. Refactor this if needed
  // maybe it's not this actor responsibility, provide callback for
  // shutdown???
  val waitForJobCompletion = true

  def exitGracefully(): Unit = {
    log.info("Shutting down client.")
    context.system.shutdown
    sys.exit
  }

  def receive = {
    case message: RequestMessage =>
      remoteActor ! message
      if (waitForJobCompletion) { // TODO: fix/change this later
        log.info("Client waiting for response...")
      } else {
        log.info("Client not waiting for response.")
        //exitGracefully()
      }

    //    case intermediateResult: IntermediateResult =>
    //      log.info(s"Got\n[$intermediateResult].\nWaiting for more...")
    case ShutdownAccepted(request, operationMode) =>
      log.info(s"Server will be shut down with [$operationMode] option as you requested: [$request].")
    //exitGracefully()
    case result: ResponseMessage =>
      log.info(s"Got reply from the server: [$result].")
    case "EXIT" =>
      exitGracefully()
    case DisassociatedEvent(localAddress, remoteAddress, _) =>
      connectionStatus = ConnectionStatusDisconnected
      log.warning("DISCONNECTED.")
    case AssociatedEvent(localAddress, remoteAddress, _) =>
      connectionStatus = ConnectionStatusConnected
      log.warning("CONNECTED.")
    case GetConnectionStatus =>
      log.info(s"CURRENT CONNECTION STATUS: $connectionStatus.")
      sender ! connectionStatus
    /*case unknown =>
      log.error(s"Unknown response: [$unknown].")
      exitGracefully()*/
  }
}
