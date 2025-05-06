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

import config.ConfigurationService
import zio.*
import zio.http.*
import zio.json.*

import java.nio.file.{Files, Paths as JPaths}

object AllRoutes {

  private def file(
    fileName: String
  ): IO[Throwable, java.io.File] = {
    JPaths.get(fileName) match {
      case path: java.nio.file.Path if !Files.exists(path) => ZIO.fail(Throwable(s"NotFound($fileName)"))
      case path: java.nio.file.Path                        => ZIO.succeed(path.toFile.nn)
      case null => ZIO.fail(Throwable(s"HttpError.InternalServerError(Could not find file $fileName))"))
    }
  }

  val fileRoutes = ZIO.succeed {
    Routes(
      Method.GET / Root -> handler {
        (
          _: Request
        ) =>
          Handler
            .fromFileZIO {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.http.staticContentDir
                file <- file(s"$staticContentDir/index.html")
              } yield file
            }
      }.flatten,
      Method.GET / trailing -> handler {
        (
          path: Path,
          _:    Request
        ) =>

          // You might want to restrict the files that could come back, but then again, you may not
          val somethingElse = path.toString
          Handler
            .fromFileZIO {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.http.staticContentDir
                file <- file(s"$staticContentDir/$somethingElse")
              } yield file
            }
      }.flatten
    )
  }

  /* case Method.GET -> !! => ZIO.succeed(Response(data = file("index.html")))
   * case Method.GET -> !! / "text" => ZIO.succeed(Response.text("Hello World!"))
   * case Method.GET -> !! / somethingElse => ZIO.succeed(Response(data = file(somethingElse)))
   */

  case class Charity(name: String)

  case class EveryOrgResponse(nonprofits: List[Charity])

  given JsonCodec[Charity] = DeriveJsonCodec.gen[Charity]
  given JsonCodec[EveryOrgResponse] = DeriveJsonCodec.gen[EveryOrgResponse]

  def favoriteCharity: ZIO[Any, Throwable, String] = {
    val file = java.io.File("key.txt")
    if (!file.exists()) {
      ZIO.fail(new Exception("Key file not found"))
    } else
      {
        val key = java.nio.file.Files.readString(file.toPath).strip() // Need to read it from the file system for security reasons

        for {
          request <- ZIO
            .fromEither(URL.decode(s"https://partners.every.org/v0.2/search/pets?apiKey=$key"))
            .mapBoth(new Exception(_), Request.get(_))
          response <- ZClient.batched(request)
          body     <- response.body.asString
          json     <- ZIO.fromEither(body.fromJson[EveryOrgResponse]).mapError(new Exception(_))
        } yield json.nonprofits.headOption.fold("")(_.name)
      }.provide(Client.default)
  }

  val modelRoutes = ZIO.succeed {
    Routes(
      Method.GET / "favoriteCharity" -> handler { (request: Request) =>
        favoriteCharity.map(charity => Response.text(charity))
      }
    )
  }
  /*
      case Method.GET -> !! / "modelObject" / id        => db.get(ModelObjectId(id.toInt)).map(o => Response.json(o.toJson))
      case Method.GET -> !! / "modelObjects"            => db.search().map(o => Response.json(o.toJson))
      case Method.GET -> !! / "modelObjects" / "search" => db.search().map(o => Response.json(o.toJson))
      case Method.DELETE -> !! / "modelObject" / id     => db.delete(ModelObjectId(id.toInt), softDelete = true).map(o => Response.json(o.toJson))
      case Method.POST -> !! / "modelObject" | Method.PUT -> !! / "modelObject" =>
        for {
          str      <- request.bodyAsString
          obj      <- ZIO.fromEither(str.fromJson[ModelObject]).mapError(new Exception(_))
          upserted <- db.upsert(obj)
        } yield Response.json(upserted.toJson)


   */

}
