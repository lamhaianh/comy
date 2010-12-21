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

    if (request.getServletPath == LOGIN_PAGE) {
      chain.doFilter(request, response)
    } else if (request.getServletPath == OPEN_ID_RETURN_POINT) {
      openIdReturnPoint(request, response, lp)
    } else {
      checkLogin(request, response, chain, lp)
    }
  }

  //----------------------------------------------------------------------------

  private def loginPage(request: HttpServletRequest)        = request.getContextPath + LOGIN_PAGE
  private def loginSuccessPage(request: HttpServletRequest) = request.getContextPath + LOGIN_SUCESS_PAGE

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
    val user = request.getSession.getAttribute("user")
    if (user == null) {
      response.sendRedirect(loginPage)
      return
    }

    val userBean = user.asInstanceOf[User]
    if (!userBean.verifyForReturnPoint(request)) {
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
    val user = request.getSession.getAttribute("user")
    if (user == null)
      response.sendRedirect(loginPage)
    else {
      val userBean = user.asInstanceOf[User]
      if (userBean.loggedInUsername == null)
        response.sendRedirect(loginPage)
      else
        chain.doFilter(request, response)
    }
  }
}
