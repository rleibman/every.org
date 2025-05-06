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

package apiClient

import zio.*
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}
import zio.http.{Client, url}

trait HelloClient[F[_]] {

  def greeting: F[HelloEndpoints.ResponseBody]

}

case class ZioHelloClient(executor: EndpointExecutor[Any, Unit, Scope]) extends HelloClient[UIO] {

  def greeting: UIO[HelloEndpoints.ResponseBody] = ZIO.scoped(executor(HelloEndpoints.getGreeting(())))

}

object ZioHelloClient {

  def live: URLayer[EndpointLocator & Client, ZioHelloClient] =
    ZLayer.fromZIO {
      for {
        client  <- ZIO.service[Client]
        locator <- ZIO.service[EndpointLocator]
      } yield ZioHelloClient(EndpointExecutor(client, locator))
    }

}

object TestHello extends ZIOApp {

  override type Environment = Client & EndpointLocator & ZioHelloClient

  override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: TaskLayer[Environment] =
    ZLayer.make[Environment](
      Client.default,
      ZLayer.succeed(EndpointLocator.fromURL(url"http://localhost:8080")),
      ZioHelloClient.live
    )

  override def run = ZIO.serviceWith[ZioHelloClient](_.greeting)

}
