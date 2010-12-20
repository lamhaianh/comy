package comy.controller.login

import java.io.FileInputStream
import java.security.KeyStore

import javax.net.ssl.{HttpsURLConnection, SSLContext, TrustManagerFactory}
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import comy.Config

object PageLevelAuthenticator {
  val LOGIN_PAGE        = "/admin/login.gnt"
  val LOGIN_SUCESS_PAGE = "/admin/stats.gnt"

  def loginPage(request: HttpServletRequest)        = request.getContextPath + LOGIN_PAGE
  def loginSuccessPage(request: HttpServletRequest) = request.getContextPath + LOGIN_SUCESS_PAGE
}

class PageLevelAuthenticator extends Filter {
  import PageLevelAuthenticator._

  /** Sets keystore for self-signed HTTPS OpenID provider. */
  def init(config: FilterConfig) {
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

  def destroy {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest  = request.asInstanceOf[HttpServletRequest]
    val httpResponse = response.asInstanceOf[HttpServletResponse]
    val lp           = loginPage(httpRequest)

    if (httpRequest.getServletPath == LOGIN_PAGE) {
      chain.doFilter(request, response)
    } else {
      // Check login
      // The user has logged in if there is "user" bean in the session and
      // loggedInUsername != null

      val user = httpRequest.getSession.getAttribute("user")
      if (user == null)
        httpResponse.sendRedirect(lp)
      else {
        val userBean = user.asInstanceOf[User]
        if (userBean.loggedInUsername == null)
          httpResponse.sendRedirect(lp)
        else
          chain.doFilter(request, response)
      }
    }
  }
}
