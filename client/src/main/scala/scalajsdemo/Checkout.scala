/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package scalajsdemo

import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^.*

object Checkout {

  case class Props()

  case class State(
    charity: String = "Nothing"
  )

  class Backend($ : BackendScope[Props, State]) {

    def loadState(): Callback =
      Ajax("get", "http://localhost:8888/favoriteCharity").setRequestContentTypeJsonUtf8.send.onComplete { xhr =>
        $.modState(_.copy(charity = xhr.responseText))
      }.asCallback

    def render(
      props: Props,
      state: State
    ): VdomElement = {
      <.div(
        ^.className := "wholePage",
        ^.width     := 100.pc,
        <.header(
          ^.className       := "pageTitle",
          ^.backgroundColor := "rgb(11 61 145)",
          ^.color           := "white",
          ^.width           := 100.pc,
          <.table(
            ^.width := 100.pc,
            <.tbody(
              <.tr(
                <.td(<.h1("HardwareHub")),
                <.td("Products |  Deals | Services | Support"),
                <.td(<.img(^.width := 30.px, ^.src := "images/search.svg")),
                <.td(<.img(^.width := 30.px, ^.src := "images/profile.svg")),
                <.td(<.img(^.width := 30.px, ^.src := "images/cart.svg"))
              )
            )
          )
        ),
        <.table(
          ^.width := 100.pc, // ^.height:=100.pc, (
          <.tbody(
            <.tr(
              <.td(
                <.div(
                  ^.textAlign := "center",
                  <.h2("Make a difference today"),
                  <.p("Round up your purchase or add a custom donation to support the charity of YOUR choice (with every.org)"),
                  <.button(s"Donate to ${state.charity}"),
                  <.p("Not today"),
                  <.button("Next")
                )
              ),
              <.td(
                // Results go here
                <.h2("Order Summary"),
                <.table(
                  <.tbody(
                    <.tr(
                      <.td(<.img(^.width := 60.px, ^.src := "images/drill.jpeg")),
                      <.td("Power Drill - 20V Cordless"),
                      <.td("$99.99")
                    ),
                    <.tr(
                      <.td(<.img(^.width := 60.px, ^.src := "images/screwdrivers.jpeg")),
                      <.td("10 Piece Screwdriver Set"),
                      <.td("$29.99")
                    ),
                    <.tr(
                      <.td(<.img(^.width := 60.px, ^.src := "images/measuring.jpeg")),
                      <.td("Measuring Tape - 25ft"),
                      <.td("$14.99")
                    ),
                    <.tr(<.td("Subtotal"), <.td(), <.td("$144.97")),
                    <.tr(<.td("Tax"), <.td(), <.td("$12.00")),
                    <.tr(<.td("Donation"), <.td(), <.td("$5.00")),
                    <.tr(<.td("Total"), <.td(), <.td("$161.97"))
                  )
                )
              )
            )
          )
        )
      )

    }

  }

  private val component = ScalaComponent
    .builder[Props]("SampleForm")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState())
    .build

  def apply(): Unmounted[Props, State, Backend] = component(Props())

}
