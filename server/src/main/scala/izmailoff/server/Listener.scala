package izmailoff.server

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import izmailoff.common.messages.Messages._
import izmailoff.common.messages.Messages.ServiceOperationType._
import akka.actor.Address
import akka.remote.DisassociatedEvent
import akka.actor.ActorSystem
import akka.remote.RemotingShutdownEvent
import akka.remote.AssociationErrorEvent
import akka.remote.AssociatedEvent
import akka.remote.AssociationEvent
import akka.actor.DeadLetter

/**
 * A listener actor that listens to all incoming requests and registers clients.
 */
class Listener extends Actor with ActorLogging {

  var knownClients = Set[ActorRef]()

  def printKnownClients(): Unit =
    log.info(s"List of ${knownClients.size} registered clients: [\n${knownClients.mkString("\n")}\n].")

  /**
   * Registers client actor for broadcasting purposes.
   */
  def registerClient(client: ActorRef): Unit =
    if (client != context.system.deadLetters && ! knownClients.contains(client)) {
      log.info(s"Registering client [$client].")
      knownClients += client
      printKnownClients()
    }

  /**
   * Whenever client disconnects it will be removed from the list and will not receive broadcasting events.
   */
  def unregisterClient(address: Address): Unit =
    if (address != context.system.deadLetters.path.address) {
      val deadClients = knownClients.filter(_.path.address == address)
      if (deadClients.size > 0) {
        log.info(s"Removing dead client: [$address].")
        deadClients foreach { knownClients -= _ }
        printKnownClients()
      }
    }

  /**
   * Broadcast events and statuses to all registered clients.
   */
  def broadcastToClients(response: ResponseMessage): Unit =
    knownClients.foreach(_ ! response)

  /**
   * Client connection events are handled to update list of active broadcast clients. For this to work this
   * actor subscribes to lifecycle events.
   */
  override def receive = {
    case Shutdown(username, request, operationMode) =>
      registerClient(sender)
      log.warning(s"User [$username] requested service SHUTDOWN with [$operationMode] mode! Request: [$request].")
      broadcastToClients(ShutdownAccepted(request, operationMode))
      //sender ! ShutdownAccepted(request, operationMode)
      log.info("Replied to sender to confirm SHUTDOWN operation.")
      operationMode match {
        case NORMAL => scheduleServiceShutdown()
        case ABORT => shutdownImmediate()
      }
    case status @ Status(username: String, request: Request) =>
      registerClient(sender)
      val currentStatus = getServiceStatus
      val status = StatusRunning(request, getServiceInfo) // FIXME: hardcoded
      log.info(s"Got [$status] request message, replying with [$status].")
      // No need to broadcast this message: broadcastToClients(status)
      sender ! status
    case job @ RunJob(request, username, description, millis) =>
      registerClient(sender)
      log.info(s"STARTED processing [$job].")
      Thread.sleep(millis)
      log.info(s"FINISHED processing [$job].")
      val error = Some(new Exception("EVERYTHING FAILED :)."))
      broadcastToClients(JobComplete(request, error))
//      sender ! JobComplete(request, error)
    case request: RequestMessage => // TODO: implement all cases
      registerClient(sender)
      log.info(s"GOT [$request].")
    //workerManager ! (request, sender)
    //broadcastToClients(new ResponseMessage)
    //    case (result: ResponseMessage, client: ActorRef) => // Why `client` ?? we don't use it I think
    //      broadcastToClients(result)
    case DisassociatedEvent(localAddress, remoteAddress, _) =>
      unregisterClient(remoteAddress)

  }

  /**
   * This service can be in one of these states.
   */
  object ServiceStatus extends Enumeration {
    type ServiceStatus = Value
    val RUNNING, SLEEPING, STARTING, STOPPING = Value
  }
  import ServiceStatus._

  // TODO: implement mechanism to find status
  def getServiceStatus: ServiceStatus = RUNNING

  // TODO: impelement to return service and server stats
  def getServiceInfo: StatusInfo = StatusInfo()

  // TODO: implement - perhaps stop accepting new requests and wait for existing jobs to finish.
  /**
   * Service will shut down whenever all jobs are finished. No new jobs will be accepted.
   */
  def scheduleServiceShutdown() = {
    log.warning("System is ignoring scheduled shutdown request because it's not yet implemented.")
  }

  /**
   * Service will shutdown immediately without waiting for jobs to complete.
   */
  def shutdownImmediate() = {
    log.warning("System is going down now due to requested immediate shutdown!!!")
    context.system.shutdown()
    //shutdown() // or use cake pattern
  }

}