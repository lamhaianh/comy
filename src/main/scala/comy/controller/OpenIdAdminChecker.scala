package comy.controller

import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import comy.bean.User

object OpenIdAdminChecker {
  val LOGIN_PATH = "/admin/login.gnt"
}

/** Authentication using CSO */
class OpenIdAdminChecker extends Filter {
  import OpenIdAdminChecker._

  def init(config: FilterConfig) {}

  def destroy {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest  = request.asInstanceOf[HttpServletRequest]
    val httpResponse = response.asInstanceOf[HttpServletResponse]
    val loginPage    = httpRequest.getContextPath + LOGIN_PATH

    if (httpRequest.getServletPath == LOGIN_PATH) {
      chain.doFilter(request, response)
    } else {
      val user = httpRequest.getSession.getAttribute("user")
      if (user == null)
        httpResponse.sendRedirect(loginPage)
      else {
        val userBean = user.asInstanceOf[User]
        if (userBean.loggedInUsername == null)
          httpResponse.sendRedirect(loginPage)
        else
          chain.doFilter(request, response)
      }
    }
  }
}
