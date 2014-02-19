package izmailoff.server

import akka.kernel.Bootable
import scala.util._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.event.LogSource

object CustomLogSource {
  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }
}

/**
 * Actor System for managing all server actors.
 */
class Server extends Bootable {

  val localActorSystemName = Configuration.conf.getString("akkaNames.local.system")
  val localActorName = Configuration.conf.getString("akkaNames.local.actor")
  //  info("Extracted all settings successfully.")

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

  import CustomLogSource._
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
      error(s"Failed to start listener actor. Got exception: [\n$e\n]")
      info("Shutting server down.")
      sys.exit(1)
  }
  info("Created listener actor [" + actor + "].")

  override def startup() {
  }

  override def shutdown() {
    // TODO: call markUnfinishedJobsAsFailed when shutdown completely implemented
    system.shutdown()
  }

  def markUnfinishedJobsAsFailed() {}
}