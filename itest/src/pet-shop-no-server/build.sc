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
import dev.guardrail.{Args => _, _}
import dev.{guardrail => dg}

def baseDir = build.millSourcePath

object `pet-shop-no-server` extends ScalaModule with Guardrail {

  override def scalaVersion = "2.13.12"

  override def guardrailTasks: define.Task[Agg[Guardrail.LanguageAndArgs]] =
    T.task {
      super.guardrailTasks().map {
        (languageAndArgs: Guardrail.LanguageAndArgs) =>
          languageAndArgs.copy(
            args = languageAndArgs.args.filterNot { a: (dg.Args) =>
              a.kind == CodegenTarget.Server
            }
          )
      }
    }

  override def ivyDeps = T.task {
    super.ivyDeps() ++ Agg(
      ivy"io.circe::circe-core:0.14.6",
      ivy"io.circe::circe-generic:0.14.6",
      ivy"io.circe::circe-parser:0.14.6",
      ivy"org.http4s::http4s-blaze-client:0.23.16",
      ivy"org.http4s::http4s-circe:0.23.16",
      ivy"org.http4s::http4s-dsl:0.23.16"
    )
  }

  def verify(): Command[Unit] = T.command {
    compile()
    println(allSources())
    val expectedSources = Set(
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Order.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/User.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/ApiResponse.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/User.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/package.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/ApiResponse.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/support/Presence.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Order.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Pet.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Tag.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/package.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/Client.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/support/Presence.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Category.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Customer.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Tag.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Category.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Pet.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Address.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/Implicits.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/Implicits.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/Http4sImplicits.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/models/definitions/Address.scala",
      s"pet-shop-no-server/out/pet-shop-no-server/guardrailGenerate.dest/guardrail/client/definitions/Customer.scala"
    )
    assert(
      generatedSources().exists(f =>
        expectedSources.exists(e =>
          f.toString.replaceAllLiterally("\\", "/").contains(e.toString)
        )
      )
    )
  }

}
