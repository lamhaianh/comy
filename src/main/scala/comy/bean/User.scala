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
// @SessionScoped class should contain minimum properties so that they can be
// serialized easily.
class User extends Serializable {
  import Msgs._

  @BeanProperty var loggedInUsername: String = null
  @BeanProperty var inputUsername   : String = null

  private var association: Association = _   // Each user/session has an association

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

  def verifyForReturnPoint(request: HttpServletRequest): Boolean = {
    val manager = new OpenIdManager
    val authentication = manager.getAuthentication(request, association.getRawMacKey, "ext1")  // ext1: Endpoint.DEFAULT_ALIAS
    val openId = authentication.getIdentity
    if (openId == null) {
      false
    } else {
      loggedInUsername = usernameFromOpenId(openId)
      true
    }
  }

  //----------------------------------------------------------------------------

  private def checkUsername(username: String) = true

  private def usernameFromOpenId(openId: String) = {
    val tokens = Config.openIdFormat.split("%s")  // 2 elements
    val before = tokens(0)
    val after  = tokens(1)
    val beforeCut = openId.substring(before.length)
    if (after.length == 0) beforeCut else beforeCut.substring(0, beforeCut.indexOf(after))
  }

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
    val manager = new OpenIdManager
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
        val logger = LoggerFactory.getLogger(getClass)
        logger.debug("OpenID Exception", e)
        -1
    }
  }
}
