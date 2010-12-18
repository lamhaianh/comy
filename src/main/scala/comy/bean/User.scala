package comy.bean

import java.io.Serializable

import javax.faces.application.FacesMessage
import javax.faces.bean.{ManagedBean, SessionScoped}
import javax.faces.context.FacesContext
import javax.servlet.http.{HttpSession, HttpServletRequest, HttpServletResponse}

import scala.reflect.BeanProperty

import org.expressme.openid.{Association, OpenIdManager}
import org.slf4j.LoggerFactory

import comy.Config

@ManagedBean
@SessionScoped
class User extends Serializable {
  import Msgs._

  val logger = LoggerFactory.getLogger(getClass)

  @BeanProperty var loggedInUsername: String = null
  @BeanProperty var inputUsername   : String = null

  val manager = new OpenIdManager
  var association: Association = _   // Each user/session has an association

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
          Some(msgs("openid_server_error"))
        } else if (code > 0) {
          if (code == 0x704) {  // Probably the remote server is HTTPS without valid key
            Some(msgs("openid_bad_provider"))
          } else {
            Some(msgs("openid_invalid"))
          }
        } else {  // == 0
          None
        }
      } else {
        Some(msgs("openid_unregistered"))
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
    manager.setReturnTo(returnToUrl)

    // Perform discovery on the user-supplied identifier
    val openId = Config.openIdFormat.format(username)
    try {
      val endpoint = manager.lookupEndpoint(openId)
      val association = manager.lookupAssociation(endpoint)
      val url = manager.getAuthenticationUrl(endpoint, association)

      // Using HTTP response directly in JSF to redirect will cause error:
      //   val response = extContext.getResponse.asInstanceOf[HttpServletResponse]
      //   response.sendRedirect(authRequest.getDestinationUrl(true))
      extContext.redirect(url)

      0
    } catch {
//      case ye: YadisException =>

      case e =>
        logger.debug("OpenID Exception", e)
        -1
    }
  }
}
