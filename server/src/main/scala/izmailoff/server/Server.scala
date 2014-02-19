package izmailoff.server

import akka.kernel.Bootable
import scala.util._
import akka.actor.ActorSystem
//import akka.actor.ActorLogging
//http://doc.akka.io/docs/akka/2.0/scala/logging.html

/**
 * Actor System for managing all server actors.
 */
class Server extends Bootable {
  //info("Read Job server configuration\n")
  //info(PrintHelpers.prettyConf(Configuration.conf))

  val localActorSystemName = Configuration.conf.getString("akkaNames.local.system")
  val localActorName = Configuration.conf.getString("akkaNames.local.actor")
//  info("Extracted all settings successfully.")

  val numWorkers = Configuration.conf.getInt("akkaWorkers.local.numWorkers")
//  info("Configured to run [%d] workers.".format(numWorkers))

  val system = Try {
    ActorSystem(localActorSystemName, Configuration.conf)
  } match {
    case Success(actorSystem) =>
      actorSystem
    case Failure(e) =>
//      error(s"Failed to start server actor. Got exception: [\n$e\n]")
//      info("Shutting server down.")
      sys.exit(1)
  }
//  info("Created actor system [" + system + "].")

  val actor = Try {
    123 //system.actorOf(Props(listener), localActorName)
  } match {
    case Success(listener) =>
      listener
    case Failure(e) =>
//      error(s"Failed to start listener actor. Got exception: [\n$e\n]")
//      info("Shutting server down.")
      sys.exit(1)
  }
//  info("Created listener actor [" + actor + "].")

  override def startup() {
  }

  override def shutdown() {
    // TODO: call markUnfinishedJobsAsFailed when shutdown completely implemented
    system.shutdown()
  }

  def markUnfinishedJobsAsFailed() {}
}