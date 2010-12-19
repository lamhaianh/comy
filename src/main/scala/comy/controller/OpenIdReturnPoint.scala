package comy.controller

import s3m._

import org.expressme.openid.{Endpoint, OpenIdManager}
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
    if (!userBean.verifyForReturnPoint(request)) {
      response.sendRedirect(lp)
      complete
      return
    }

    val lsp = PageLevelAuthenticator.loginSuccessPage(request)
    response.sendRedirect(lsp)
    complete
  }
}
