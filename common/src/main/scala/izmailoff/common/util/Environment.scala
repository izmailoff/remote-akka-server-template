package izmailoff.common.util

/**
 * A helper class that provides convenient way to work with OS environment.
 */
object Environment {

  def currentOsUser: String =
    System.getProperty("user.name")
}