package comy.action.admin

import xitrum.annotation.GET
import comy.action.{AppAction, Session}

@GET("/admin")
class Index extends AppAction {
  beforeFilters("authenticate") = () => {
    val ret = Session.username.isDefined
    if (!ret) {
      flash(t("Please login."))
      redirectTo[Login]
    }
    ret
  }

  override def execute {
    renderView(<p>Admin page</p>)
  }
}
