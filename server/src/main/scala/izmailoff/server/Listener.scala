package izmailoff.server

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import izmailoff.common.messages.Messages._
import izmailoff.common.messages.Messages.ServiceOperationType._
import akka.actor.Address

/**
 * A listener actor that listens to all incoming requests and registers clients.
 */
class Listener extends Actor with ActorLogging {
  import log._

  override def preStart = {
    //???
  }

  var knownClients = Set[ActorRef]()

  def registerClient(client: ActorRef): Unit =
    if (!knownClients.contains(client)) {
      info(s"Registering client $client")
      knownClients += client
    }

  def removeDeadClient(address: Address): Unit = {
    val deadClients = knownClients.filter(_.path.address == address)
    warning(s"Removing dead clients: [\n${deadClients.mkString("\n")}\n]")
    deadClients foreach { knownClients -= _ }
    info(s"Current number of registered clients: [${knownClients.size}]")
  }

  /**
   * Broadcast events and statuses to all registered clients.
   */
  def broadCastToClients(response: ResponseMessage): Unit = knownClients.foreach(_ ! response)

  /**
   * Client connection events are handled to update list of active broadcast clients. For this to work this
   * actor subscribes to these connection events.
   */
  override def receive = {
    case Shutdown(username, request, operationMode) =>
      warning(s"User [$username] requested service SHUTDOWN with [$operationMode] mode! Request: [$request].")
      sender ! ShutdownAccepted(request, operationMode)
      info("Replied to sender to confirm SHUTDOWN operation.")
      operationMode match {
        case NORMAL => scheduleServiceShutdown()
        case ABORT => shutdownImmediate()
      }
    case status @ Status(username: String, request: Request) =>
      val currentStatus = getServiceStatus
      val status = StatusRunning(request, getServiceInfo) // FIXME: hardcoded
      info(s"Got [$status] request message, replying with [$status].")
      sender ! status
    case request: RequestMessage =>
      registerClient(sender)
    //workerManager ! (request, sender)
    case (result: ResponseMessage, client: ActorRef) =>
      broadCastToClients(result)

  }

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
  def scheduleServiceShutdown() = {
    warning("System is ignoring scheduled shutdown request because it's not yet implemented.")
  }

  def shutdownImmediate() = {
    warning("System is going down now due to requested immediate shutdown!!!")
    context.system.shutdown()
    //shutdown() // or use cake pattern
  }

}