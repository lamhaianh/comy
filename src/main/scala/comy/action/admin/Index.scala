package comy.action.admin

import xitrum.action.annotation.GET
import comy.action.Application

@GET("/admin")
class Index extends Application {
  beforeFilters("authenticate") = () => {
    val ret = session.contains("username")
    if (!ret) redirectTo[Login]
    ret
  }

  def execute {
    renderView(<p>Admin page</p>)
  }
}
