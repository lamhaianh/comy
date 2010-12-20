package comy.controller.login

import java.io.Serializable
import java.net.URLEncoder
import javax.faces.application.FacesMessage
import javax.faces.bean.{ManagedBean, SessionScoped}
import javax.faces.context.FacesContext
import javax.servlet.http.{HttpSession, HttpServletRequest, HttpServletResponse}

import scala.reflect.BeanProperty

import org.expressme.openid.{Association, OpenIdManager}
import org.slf4j.LoggerFactory

import comy.Config
import comy.controller.Msgs

// @SessionScoped class should contain minimum properties so that they can be
// serialized easily.
@ManagedBean
@SessionScoped
class User extends Serializable {
  import Msgs._

  @BeanProperty var loggedInUsername: String = null
  @BeanProperty var inputUsername   : String = null

  // See http://code.google.com/p/jopenid/wiki/DevGuide
  private var macKey: Array[Byte] = _
  private var alias:  String      = _


  def logout = {
    val session = FacesContext.getCurrentInstance.getExternalContext.getSession(false).asInstanceOf[HttpSession]
    session.invalidate
    "/user/index?faces-redirect=true"
  }

  def gotoCso {
    val facesContext = FacesContext.getCurrentInstance
    val errorMsgo =
      if (checkUsername(inputUsername)) {
        if (redirectToOpenIdProvider(inputUsername)) {
          None
        } else {
          Some(msgs("openid_invalid"))
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
    try {
      val manager        = new OpenIdManager
      manager.setReturnTo(returnToUrl(request))
      val authentication = manager.getAuthentication(request, macKey, alias)
      val openId         = authentication.getIdentity
      if (openId == null) {
        false
      } else {
        loggedInUsername = usernameFromOpenId(openId)
        true
      }
    } catch {
      //case _ => false
      case e => e.printStackTrace(); false
    }
  }

  //----------------------------------------------------------------------------

  private def checkUsername(username: String) = true

  private def usernameFromOpenId(openId: String) = {
    val (before, after) = if (Config.openIdFormat.endsWith("%s")) {
      (Config.openIdFormat, "")
    } else {
      val tokens = Config.openIdFormat.split("%s")  // 2 elements
      (tokens(0), tokens(1))
    }
    val beforeCut = openId.substring(before.length)
    if (after.length == 0) beforeCut else beforeCut.substring(0, beforeCut.indexOf(after))
  }

  /**
   * @return true if successfully redirected to the OpenID provider
   */
  private def redirectToOpenIdProvider(username: String): Boolean = {
    val facesContext = FacesContext.getCurrentInstance
    val extContext   = facesContext.getExternalContext
    val request      = extContext.getRequest.asInstanceOf[HttpServletRequest]

    val manager = new OpenIdManager
    manager.setReturnTo(returnToUrl(request))

    // Perform discovery on the user-supplied identifier
    val openId = Config.openIdFormat.format(username)
    try {
      val endpoint    = manager.lookupEndpoint(openId)
      val association = manager.lookupAssociation(endpoint)  // Save to the session
      val url         = manager.getAuthenticationUrl(endpoint, association)

      // The values of openid.claimed_id and openid.identity are http://specs.openid.net/auth/2.0/identifier_select
      // We change them to openId to specify our openId as the user name
      // See:
      //   http://openid.net/specs/openid-authentication-2_0.html
      //   http://code.google.com/apis/accounts/docs/OpenID.html
      val claimedUrl = url.replaceAll(URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", "UTF-8"), URLEncoder.encode(openId, "UTF-8"))

      macKey = association.getRawMacKey
      alias  = endpoint.getAlias

      // Using HTTP response directly in JSF to redirect will cause error:
      //   val response = extContext.getResponse.asInstanceOf[HttpServletResponse]
      //   response.sendRedirect(authRequest.getDestinationUrl(true))
      extContext.redirect(claimedUrl)

      true
    } catch {
      case e =>
        val logger = LoggerFactory.getLogger(getClass)
        logger.error("OpenID Exception", e)
        false
    }
  }

  /**
   * Configure the return_to URL where your application will receive
   * the authentication responses from the OpenID provider
   */
  private def returnToUrl(request: HttpServletRequest) = {
    if ((request.getScheme == "https" && request.getServerPort == 443) ||
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
  }
}
