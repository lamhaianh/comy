package comy.controller

import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.openid4java.message.ParameterList

import comy.bean.User

class OpenIdReturnPoint extends Filter {
  def init(config: FilterConfig) {}

  def destroy {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest  = request.asInstanceOf[HttpServletRequest]
    val httpResponse = response.asInstanceOf[HttpServletResponse]
    verifyOpenIdResponse(httpRequest, httpResponse)
  }

  private def verifyOpenIdResponse(request: HttpServletRequest, response: HttpServletResponse) {
    val lp = PageLevelAuthenticator.loginPage(request)

    // Extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
    val openIdResponse = new ParameterList(request.getParameterMap)

    // Retrieve the previously stored discovery information
    val user = request.getSession.getAttribute("user")
    if (user == null) {
      response.sendRedirect(lp)
      return
    }

    val userBean = user.asInstanceOf[User]
    val discoveryInformation = userBean.discoveryInformation

    // Extract the receiving URL from the HTTP request
    val receivingURL = request.getRequestURL

    val queryString = request.getQueryString
    if (queryString != null && queryString.length() > 0)
      receivingURL.append("?").append(request.getQueryString)

    // Verify the response; ConsumerManager needs to be the same
    // (static) instance used to place the authentication request
    val verification = userBean.manager.verify(
      receivingURL.toString(), openIdResponse, discoveryInformation)

    // Examine the verification result and extract the verified identifier
    val verifiedId = verification.getVerifiedId
    if (verifiedId == null) {
      response.sendRedirect(lp)
      return
    }

    val ident = verifiedId.getIdentifier
    val tokens = ident.split("/")
    userBean.loggedInUsername = tokens(tokens.length - 1)

    val lsp = PageLevelAuthenticator.loginSuccessPage(request)
    response.sendRedirect(lsp)
  }
}
