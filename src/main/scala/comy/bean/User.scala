package comy.bean

import java.io.Serializable

import javax.faces.application.FacesMessage
import javax.faces.bean.{ManagedBean, SessionScoped}
import javax.faces.context.FacesContext
import javax.servlet.http.{HttpSession, HttpServletRequest, HttpServletResponse}

import scala.reflect.BeanProperty

import org.openid4java.consumer.ConsumerManager
import org.openid4java.discovery.DiscoveryInformation
import org.openid4java.discovery.yadis.YadisException

import org.slf4j.LoggerFactory

import comy.Config

@ManagedBean
@SessionScoped
class User extends Serializable {
  val logger = LoggerFactory.getLogger(getClass)

  @BeanProperty var loggedInUsername: String = null
  @BeanProperty var inputUsername   : String = null

  val manager = new ConsumerManager
  var discoveryInformation: DiscoveryInformation = null

  def logout = {
    val session = FacesContext.getCurrentInstance.getExternalContext.getSession(false).asInstanceOf[HttpSession]
    session.invalidate
    "/user/index?faces-redirect=true"
  }

  def gotoCso {
    val facesContext = FacesContext.getCurrentInstance
    val errorMsgo =
      if (checkUsername(inputUsername)) {
        val code = redirectToOpenIdProvider(inputUsername)
        if (code < 0) {
          Some("Server error")
        } else if (code > 0) {
          if (code == 0x704) {  // Probably the remote server is HTTPS without valid key
            Some("Bad OpenID provider")
          } else {
            Some("Invalid username")
          }
        } else {  // == 0
          None
        }
      } else {
        Some("Unregistered username")
      }

    if (errorMsgo != None) {
      facesContext.addMessage(
        "form:username",
        new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsgo.get, null))
    }
  }

  //----------------------------------------------------------------------------

  private def checkUsername(username: String) = true

  /**
   * @return Error code from OpenID4Java, or -1 for other error, or 0 if
   * successfully redirected to the OpenID provider
   */
  private def redirectToOpenIdProvider(username: String): Int = {
    val facesContext = FacesContext.getCurrentInstance
    val extContext   = facesContext.getExternalContext
    val request      = extContext.getRequest.asInstanceOf[HttpServletRequest]
    val response     = extContext.getResponse.asInstanceOf[HttpServletResponse]

    // Configure the return_to URL where your application will receive
    // the authentication responses from the OpenID provider
    val returnToUrl = if ((request.getScheme == "https" && request.getServerPort == 443) ||
          (request.getScheme == "http"  && request.getServerPort == 80)) {
      request.getScheme + "://" +
      request.getServerName +
      request.getContextPath +
      "//open_id_return_point";
    } else {
      request.getScheme + "://" +
      request.getServerName + ":" +
      request.getServerPort + request.getContextPath +
      "/open_id_return_point";
    }

    // Perform discovery on the user-supplied identifier
    val openId = Config.openIdFormat.format(username)
    try {
      val discoveries = manager.discover(openId)

      // Attempt to associate with the OpenID provider
      // and retrieve one service endpoint for authentication
      discoveryInformation = manager.associate(discoveries)

      // Obtain a AuthRequest message to be sent to the OpenID provider
      val authRequest = manager.authenticate(discoveryInformation, returnToUrl)

      response.sendRedirect(authRequest.getDestinationUrl(true))
      0
    } catch {
      case ye: YadisException =>
        logger.debug("YadisException", ye)
        ye.getErrorCode

      case e =>
        logger.debug("Not YadisException", e)
        -1
    }
  }
}
