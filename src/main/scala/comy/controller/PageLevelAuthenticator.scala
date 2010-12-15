package comy.controller

import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import comy.Config
import comy.bean.User

object PageLevelAuthenticator {
  val LOGIN_PAGE        = "/admin/login.gnt"
  val LOGIN_SUCESS_PAGE = "/admin/stats.gnt"

  def loginPage(request: HttpServletRequest)        = request.getContextPath + LOGIN_PAGE
  def loginSuccessPage(request: HttpServletRequest) = request.getContextPath + LOGIN_SUCESS_PAGE
}

class PageLevelAuthenticator extends Filter {
  import PageLevelAuthenticator._

  def init(config: FilterConfig) {
    if (!Config.keyStore.isEmpty) {
      System.setProperty("javax.net.ssl.keyStore",         Config.keyStore)
      System.setProperty("javax.net.ssl.keyStorePassword", Config.keyStorePassword)
      System.setProperty("javax.net.ssl.trustStore",       Config.keyStore)
    }
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
