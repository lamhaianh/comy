package comy.controller

import org.openid4java.message.ParameterList
import s3m._

import comy.bean.User

class OpenIdReturnPoint extends Controller {
  @Path("/open_id_return_point")
  def returnPoint {
    val lp = PageLevelAuthenticator.loginPage(request)

    // Extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
    val openIdResponse = new ParameterList(request.getParameterMap)

    // Retrieve the previously stored discovery information
    val user = request.getSession.getAttribute("user")
    if (user == null) {
      response.sendRedirect(lp)
      complete
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
      complete
      return
    }

    val ident = verifiedId.getIdentifier
    val tokens = ident.split("/")
    userBean.loggedInUsername = tokens(tokens.length - 1)

    val lsp = PageLevelAuthenticator.loginSuccessPage(request)
    response.sendRedirect(lsp)
    complete
  }
}
