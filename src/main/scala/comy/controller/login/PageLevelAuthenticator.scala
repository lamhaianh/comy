package comy.controller.login

import java.io.FileInputStream
import java.security.KeyStore

import javax.net.ssl.{HttpsURLConnection, SSLContext, TrustManagerFactory}
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import comy.Config

object PageLevelAuthenticator {
  val LOGIN_PAGE           = "/admin/login.gnt"
  val OPEN_ID_RETURN_POINT = "/admin/open_id"
  val LOGIN_SUCESS_PAGE    = "/admin/stats.gnt"

  def isLoginRequest       (request: HttpServletRequest) = request.getServletPath == LOGIN_PAGE
  def isOpenIdReturnRequest(request: HttpServletRequest) = request.getServletPath == OPEN_ID_RETURN_POINT

  def loginPage(request: HttpServletRequest)        = request.getContextPath + LOGIN_PAGE
  def loginSuccessPage(request: HttpServletRequest) = request.getContextPath + LOGIN_SUCESS_PAGE

  /**
   * Configure the return_to URL where your application will receive
   * the authentication responses from the OpenID provider
   */
  def returnToUrl(request: HttpServletRequest) = {
    if ((request.getScheme == "https" && request.getServerPort == 443) ||
        (request.getScheme == "http"  && request.getServerPort == 80)) {
      request.getScheme + "://" +
      request.getServerName +
      request.getContextPath +
      "/" + OPEN_ID_RETURN_POINT;
    } else {
      request.getScheme + "://" +
      request.getServerName + ":" +
      request.getServerPort + request.getContextPath +
      OPEN_ID_RETURN_POINT;
    }
  }
}

class PageLevelAuthenticator extends Filter {
  import PageLevelAuthenticator._

  def init(config: FilterConfig) {
    setJavaKeyStore
  }

  def destroy {}

  def doFilter(srequest: ServletRequest, sresponse: ServletResponse, chain: FilterChain) {
    val request  = srequest.asInstanceOf[HttpServletRequest]
    val response = sresponse.asInstanceOf[HttpServletResponse]
    val lp       = loginPage(request)

    if (isLoginRequest(request)) {
      chain.doFilter(request, response)
    } else if (isOpenIdReturnRequest(request)) {
      openIdReturnPoint(request, response, lp)
    } else {
      checkLogin(request, response, chain, lp)
    }
  }

  //----------------------------------------------------------------------------

  /** Sets keystore for self-signed HTTPS OpenID provider. */
  private def setJavaKeyStore {
    if (Config.jks.isEmpty) return

    val keyStore   = KeyStore.getInstance(KeyStore.getDefaultType)
    val trustStore = new FileInputStream(Config.jks)
    keyStore.load(trustStore, Config.jksPassword)
    trustStore.close

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(keyStore)
    val ctx = SSLContext.getInstance("TLS")
    ctx.init(null, tmf.getTrustManagers, null)
    val sslFactory = ctx.getSocketFactory

    HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory)
  }


  private def openIdReturnPoint(request: HttpServletRequest, response: HttpServletResponse, loginPage: String) {
    // Retrieve the previously stored discovery information
    val suser = request.getSession.getAttribute("user")
    if (suser == null) {
      response.sendRedirect(loginPage)
      return
    }

    val user = suser.asInstanceOf[User]
    if (!user.verifyForReturnPoint(request)) {
      response.sendRedirect(loginPage)
      return
    }

    val lsp = loginSuccessPage(request)
    response.sendRedirect(lsp)
  }

  /**
   * The user has logged in if there is "user" bean in the session and
   * loggedInUsername != null
   */
  private def checkLogin(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, loginPage: String) {
    val suser = request.getSession.getAttribute("user")
    if (suser == null) {
      response.sendRedirect(loginPage)
      return
    }

    val user = suser.asInstanceOf[User]
    if (user.loggedInUsername == null) {
      response.sendRedirect(loginPage)
      return
    }

    chain.doFilter(request, response)
  }
}
