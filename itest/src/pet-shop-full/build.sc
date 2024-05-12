/* Copyright 2024 Jack Viers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import $file.plugins
import $file.shared

import com.jackcviers.mill.guardrail._
import mill._
import mill.scalalib._
import mill.api.PathRef
import mill.define.Command

def baseDir = build.millSourcePath

object `pet-shop-full` extends ScalaModule with Guardrail {

  override def scalaVersion = "2.13.12"

  override def ivyDeps = T.task {
    super.ivyDeps() ++ Agg(
      ivy"io.circe::circe-core:0.14.6",
      ivy"io.circe::circe-generic:0.14.6",
      ivy"io.circe::circe-parser:0.14.6",
      ivy"org.http4s::http4s-blaze-client:0.23.16",
      ivy"org.http4s::http4s-blaze-server:0.23.16",
      ivy"org.http4s::http4s-circe:0.23.16",
      ivy"org.http4s::http4s-dsl:0.23.16"
    )
  }

  def verify(): Command[Unit] = T.command {
    compile()
    val expectedSources = Set(
      "pet-shop-full/guardrailGenerate.dest/guardrail",
      "pet-shop-full/guardrailGenerate.dest/guardrail/server",
      "pet-shop-full/guardrailGenerate.dest/guardrail/server/support",
      "pet-shop-full/guardrailGenerate.dest/guardrail/models",
      "pet-shop-full/guardrailGenerate.dest/guardrail/models/support",
      "pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions",
      "pet-shop-full/guardrailGenerate.dest/guardrail/client",
      "pet-shop-full/guardrailGenerate.dest/guardrail/client/support",
      "pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Order.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/User.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/ApiResponse.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/package.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Tag.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/User.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/support/Presence.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/package.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/ApiResponse.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/support/Presence.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/ApiResponse.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Order.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Pet.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Customer.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Tag.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/package.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Category.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/Client.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/support/Presence.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/Http4sImplicits.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/Routes.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Address.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/User.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/Implicits.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Category.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Customer.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Tag.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Category.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Pet.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Address.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/Implicits.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/Implicits.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/Http4sImplicits.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/models/definitions/Address.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Pet.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/server/definitions/Order.scala",
      "pet-shop-full/out/pet-shop-full/guardrailGenerate.dest/guardrail/client/definitions/Customer.scala"
    )
    val generated = generatedSources().flatMap { entry =>
      val withoutRefRegex = """^(?:[^:]+:){3}(.*)$""".r
      val justPetShopFullRegex = """^.*/(pet-shop-full/.*)$""".r
      val withoutRef = withoutRefRegex.replaceAllIn(
        entry.toString.replaceAllLiterally("\\", "/"),
        "$1"
      )
      os.walk(os.Path(withoutRef))
        .map(f =>
          justPetShopFullRegex
            .replaceAllIn(f.toString().replaceAllLiterally("\\", "/"), "$1")
        )
    }
    assert(
      generated.exists(f =>
        expectedSources.exists(e =>
          f.toString.replaceAllLiterally("\\", "/").contains(e.toString)
        )
      )
    )
  }

}
