package izmailoff.common.messages

import java.util.UUID

/**
 * All available messages for client-server communication.
 */
object Messages {

  case class Request(val requestId: UUID) {
    val createdAt = System.currentTimeMillis
    require(requestId != null, "UUID can't be null in Request.")
  }

  // TODO: add any kind of service and server stats we are interested in
  case class StatusInfo()

  // TODO: add fields. I think it can have exception details, and general message, maybe error code too
  case class ErrorInfo()

  /**
   * Represents all requests in the system.
   *
   * See also [[ResponseMessage]].
   */
  trait RequestMessage {
    val username: String
  }

  /**
   * Represents all responses to requests in the system.
   *
   * See also [[RequestMessage]].
   */
  trait ResponseMessage

  /**
   * A type of service operation request.
   *
   * Some requests could take time to process, while others
   * have to be processed immediately.
   */
  object ServiceOperationType extends Enumeration {
    type ServiceOperationType = Value
    val /**
   * A normal priority of a request. Running jobs will finish execution as scheduled.
   * New requests will not be accepted.
   */ NORMAL, /**
   * A high priority request. Running jobs will be terminated as soon as possible.
   * Operation will be performed while new requests will not be accepted.
   */ ABORT = Value
  }
  import ServiceOperationType.ServiceOperationType

  /**
   * A generic service message that represents all messages.
   *
   * See also [[ServiceMessageResult]].
   */
  trait ServiceMessage extends RequestMessage

  /**
   * This is the message clients will send to server to simulate work load.
   *
   * @timeMillis execute the job for this long. This simulates workload.
   */
  case class RunJob(request: Request, username: String, description: String, timeMillis: Long) extends RequestMessage

  /**
   * A generic service message response that represents all responses.
   * Server will reply to all requests in most of the service states but it
   * might refuse to run long running requests.
   * //TODO: define which requests are processed and which are refused in various states when everything is implemented
   *
   * See also [[ServiceMessage]].
   */
  trait ServiceMessageResult extends ResponseMessage

  /**
   * A counterpart of [[RunJob]] message. This message is sent once the job is finished.
   */
  case class JobComplete(request: Request, error: Option[Exception]) extends ResponseMessage

  /**
   * Get service status
   *
   * This request will be processed immediately, and one of these replies will be sent:
   * [[StatusRunning]], [[StatusSleeping]], [[StatusStarting]], [[StatusStopping]].
   *
   * @param request
   *   This request (parent)
   */
  case class Status(username: String, request: Request) extends ServiceMessage

  /**
   * Server replies with this status when it's fully functional and ready to process requests.
   * This is the most common case. All other states have to be investigated or should be short-lived.
   *
   * See also [[Status]].
   *
   * @param request
   *   This request (parent)
   * @param serviceDetails
   *   Additional information about server i.e. various server stats.
   */
  case class StatusRunning(request: Request, serviceDetails: StatusInfo) extends ServiceMessageResult

  /**
   * Server replies with this status when it's been put to maintenance mode/sleep.
   * In this state server will not accept any new requests.
   *
   * See also [[Status]].
   *
   * @param request
   *   This request (parent)
   * @param serviceDetails
   *   Additional information about server i.e. various server stats.
   */
  case class StatusSleeping(request: Request, serviceDetails: StatusInfo) extends ServiceMessageResult

  /**
   * Server replies with this status when it was just started and going through initialization procedure.
   * In this state server will not accept any new requests. Usually this state takes only a few seconds
   * at most.
   *
   * See also [[Status]].
   *
   * @param request
   *   This request (parent)
   * @param serviceDetails
   *   Additional information about server i.e. various server stats.
   */
  case class StatusStarting(request: Request, serviceDetails: StatusInfo) extends ServiceMessageResult

  /**
   * Server replies with this status when it's shutting down.
   *
   * It might be still processing scheduled jobs if shutdown was not requested with ABORT.
   * In this state server will not accept any new requests.
   *
   * See also [[Status]].
   *
   * @param request
   *   This request (parent)
   * @param serviceDetails
   *   Additional information about server i.e. various server stats.
   */
  case class StatusStopping(request: Request, serviceDetails: StatusInfo) extends ServiceMessageResult

  /**
   * Request server shutdown.
   *
   * @param request
   *   This request (parent)
   * @param operationMode
   *   If shutdown is requested as NORMAL, then server will finish all scheduled jobs without accepting
   *   new requests. Otherwise, if operationMode is ABORT then server will terminate all running jobs
   *   and shut down.
   */
  case class Shutdown(username: String, request: Request, operationMode: ServiceOperationType) extends ServiceMessage

  /**
   * Server response message for shutdown request.
   *
   * This message is sent immediately and then shutdown is performed. Client can use [[Status]] message
   * to query server status if shutdown was requested as NORMAL and there were long running jobs in the queue.
   * Server always accepts shutdown request.
   *
   * @param request
   *   This request (parent)
   * @param operationMode
   *   A mode of operation or a priority of this request
   */
  case class ShutdownAccepted(request: Request, operationMode: ServiceOperationType) extends ServiceMessageResult

  /**
   * Request server restart.
   *
   * //TODO: describe what restart is when implemented - just an actor system and db reinit or the whole app restart? conf??
   *
   * @param request
   *   This request (parent)
   * @param operationMode
   *   Behaviour is the same as [[Shutdown]]
   */
  case class Restart(username: String, request: Request, operationMode: ServiceOperationType) extends ServiceMessage

  /**
   * Server response message for restart request.
   *
   * Behaviour is the same as [[ShutdownAccepted]].
   *
   * @param request
   *   This request (parent)
   * @param operationMode
   *   A mode of operation or a priority of this request
   */
  case class RestartAccepted(request: Request, operationMode: ServiceOperationType) extends ServiceMessageResult

  /**
   * Request server sleep/maintenance mode.
   *
   * //TODO: describe how server replies to various requests in this mode
   *
   * @param request
   *   This request (parent)
   */
  case class Sleep(username: String, request: Request) extends ServiceMessage
  case class SleepComplete(request: Request) extends ServiceMessageResult
  case class SleepFailed(request: Request, error: ErrorInfo) extends ServiceMessageResult

  /**
   * TODO: the same as sleep: implement and then write the doc
   */
  case class Wakeup(username: String, request: Request) extends ServiceMessage
  case class WakeupComplete(request: Request) extends ServiceMessageResult
  case class WakeupFailed(request: Request, error: ErrorInfo) extends ServiceMessageResult

}