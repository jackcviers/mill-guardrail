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

object `pet-shop-scala-akka-http` extends ScalaModule with Guardrail {

  override def scalaVersion = "2.13.12"

  override def guardrailTasks = T.task {
    super.guardrailTasks().map { (langAndArgs: Guardrail.LanguageAndArgs) =>
      langAndArgs.copy(args =
        langAndArgs.args.map(
          _.modifyContext(c =>
            c.withFramework(Option(Guardrail.Framework.`akka-http`.toString))
          )
        )
      )
    }
  }

  override def scalacOptions = T.task {
    super.scalacOptions() ++ Agg("-deprecation")
  }

  private val akkaVersion = "2.6.20"
  private val akkaHttpVersion = "10.2.10"
  private val circeVersion = "0.14.6"
  private val refinedVersion = "0.11.0"

  override def ivyDeps = T.task {
    super.ivyDeps() ++ Agg(
      ivy"io.circe::circe-core:$circeVersion",
      ivy"io.circe::circe-generic:$circeVersion",
      ivy"io.circe::circe-parser:$circeVersion",
      ivy"io.circe::circe-jawn:$circeVersion",
      ivy"io.circe::circe-refined:$circeVersion",
      ivy"com.typesafe.akka::akka-actor:$akkaVersion",
      ivy"com.typesafe.akka::akka-stream:$akkaVersion",
      ivy"com.typesafe.akka::akka-http:$akkaHttpVersion",
      ivy"com.typesafe.akka::akka-http-testkit:$akkaHttpVersion",
      ivy"eu.timepit::refined:$refinedVersion",
      ivy"eu.timepit::refined-cats:$refinedVersion",
      ivy"javax.annotation:javax.annotation-api:1.3.2",
      ivy"javax.xml.bind:jaxb-api:2.3.1",
      ivy"org.typelevel::cats-core:2.12.0"
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
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Address.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/ApiResponse.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Category.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Customer.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Order.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Pet.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/Tag.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/User.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/definitions/package.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/Client.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/Implicits.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/AkkaHttpImplicits.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/client/support/Presence.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Address.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/ApiResponse.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Category.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Customer.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Order.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Pet.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/Tag.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/User.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/definitions/package.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/Routes.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/Implicits.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/AkkaHttpImplicits.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/server/support/Presence.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Address.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/ApiResponse.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Category.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Customer.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Order.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Pet.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/Tag.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/User.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/definitions/package.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/Implicits.scala",
      "pet-shop-scala-akka-http/out/pet-shop-scala-akka-http/guardrailGenerate.dest/guardrail/models/support/Presence.scala"
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
