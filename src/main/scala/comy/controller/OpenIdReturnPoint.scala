package comy.controller

import s3m._

import org.expressme.openid.Endpoint
import comy.bean.User

class OpenIdReturnPoint extends Controller {
  @Path("/open_id_return_point")
  def returnPoint {
    val lp = PageLevelAuthenticator.loginPage(request)

    // Retrieve the previously stored discovery information
    val user = request.getSession.getAttribute("user")
    if (user == null) {
      response.sendRedirect(lp)
      complete
      return
    }

    val userBean = user.asInstanceOf[User]
    val authentication = userBean.manager.getAuthentication(request, userBean.association.getRawMacKey, "ext1")  // ext1: Endpoint.DEFAULT_ALIAS

    val verifiedId = authentication.getIdentity
    if (verifiedId == null) {
      response.sendRedirect(lp)
      complete
      return
    }

    val tokens = verifiedId.split("/")
    userBean.loggedInUsername = tokens(tokens.length - 1)

    val lsp = PageLevelAuthenticator.loginSuccessPage(request)
    response.sendRedirect(lsp)
    complete
  }
}
