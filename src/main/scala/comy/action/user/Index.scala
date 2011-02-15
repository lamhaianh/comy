package comy.action.user

import xitrum._
import xitrum.vc.validator._

import comy.action.Application

@GET("/")
class Index extends Application {
  def execute {
    renderView(
      <form post2="submit">
        <table>
          <tr>
            <td><label for="url">URL:</label></td>
            <td>{<input id="url" type="text" name="url" value="http://mobion.jp/" tabindex="1" />.validate(new Required, new URL)}</td>
          </tr>
          <tr>
            <td><label for="key">Key:</label></td>
            <td>
              {<input type="text" name="key" tabindex="2" />.validate(new KeyValidator, new MaxLength(32))}
              <span>(optional, a-z A-Z 0-9 _ -)</span>
            </td>
          </tr>
        </table>

        <input type="submit" value="Shorten" tabindex="3" />
      </form>

      <div id="result"></div>
    )
  }
}
