package izmailoff.common.log

import akka.event.LogSource

/**
 * Provides logger name for Akka logger.
 */
object CustomLogSource {
  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }
}
