package comy

import java.util.Properties

object Config {
  private val properties = {
    val stream = getClass.getClassLoader.getResourceAsStream("comy.properties")
    val ret = new Properties
    ret.load(stream)
    ret
  }

  val apiIps   = properties.getProperty("allowed_ips.api").split(",").map(ip => ip.trim)
  val adminIps = properties.getProperty("allowed_ips.admin").split(",").map(ip => ip.trim)

  val dbAddrs              = properties.getProperty("db.addrs").split(",").map(addr => addr.trim)
  val dbConnectionsPerHost = properties.getProperty("db.connections_per_host").toInt
  val dbName               = properties.getProperty("db.name")
  val dbExpirationDays     = properties.getProperty("db.expiration_days").toInt

  val openIdFormat = properties.getProperty("open_id_format")

  val jks                      = properties.getProperty("jks")
  val jksPassword: Array[Char] = {
    val string = properties.getProperty("jks_password")
    if (string.isEmpty) null else string.toCharArray
  }

  def isApiAllowed(ip: String) = apiIps.exists { ip2 =>
    (ip2 == "*") || (ip2 == ip)
  }

  def isAdminAllowed(ip: String) = adminIps.exists { ip2 =>
    (ip2 == "*") || (ip2 == ip)
  }
}
