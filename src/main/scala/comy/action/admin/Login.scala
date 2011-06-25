package comy.action.admin

import xitrum.annotation._
import xitrum.validation._

import comy.action.{AppAction, Var}

@GET("/admin/login")
class Login extends AppAction {
  override def execute {
    renderView(
      <form postback="submit" action={urlForPostbackThis}>
        <div id="error"></div>

        <label>Username:</label> <input type="text" name={validate("username", Required)} />
        <br />
        <input type="submit" value="Password »" />
      </form>
    )
  }

  override def postback {
    val username = param("username")
    if (username == "xxx") {  // TODO
      session.reset
      Var.sUsername.set(username)
      flash("You have successfully logged in.")
      jsRedirectTo[Index]
    } else {
      jsRenderHtml(jsById("error"), <p class="error">Could not login.</p>)
    }
  }
}
