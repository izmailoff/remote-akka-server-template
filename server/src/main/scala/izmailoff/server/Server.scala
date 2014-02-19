package izmailoff.server

import akka.kernel.Bootable
import scala.util._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import izmailoff.common.log.CustomLogSource._

/**
 * Actor System for managing all server actors.
 */
class Server extends Bootable {

  val localActorSystemName = Configuration.conf.getString("akkaNames.local.system")
  val localActorName = Configuration.conf.getString("akkaNames.local.actor")

  //val numWorkers = Configuration.conf.getInt("akkaWorkers.local.numWorkers")
  //  info("Configured to run [%d] workers.".format(numWorkers))

  val system = Try {
    ActorSystem(localActorSystemName, Configuration.conf)
  } match {
    case Success(actorSystem) =>
      actorSystem
    case Failure(e) =>
      sys.error(s"Failed to start server actor. Got exception: [\n$e\n].")
      sys.error("Shutting server down.")
      sys.exit(1)
  }

  val log = Logging(system, this)
  import log._
  info("Read Job server configuration\n")
  //info(PrintHelpers.prettyConf(Configuration.conf))
  info(s"Created actor system [$system].")

  val actor = Try {
    system.actorOf(Props[Listener], localActorName)
  } match {
    case Success(listener) =>
      listener
    case Failure(e) =>
      error(s"Failed to start listener actor. Got exception: [\n$e\n].")
      info("Shutting server down.")
      sys.exit(1)
  }
  info(s"Created listener actor [$actor].")

  override def startup() {
  }

  override def shutdown() {
    system.shutdown()
  }

}

/**
 * The Main / entry point of the server application.
 */
object ServerRunner extends Server with App {
  
}