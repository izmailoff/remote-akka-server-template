package izmailoff.server

import com.typesafe.config.ConfigFactory

/**
 * Provides HOCON based configuration for the application.
 *
 * Do not try to {{{config.resolve()}}} config because it breaks AKKA connectivity!
 */
object Configuration {
  val conf = ConfigFactory.load.getConfig("server")
}
