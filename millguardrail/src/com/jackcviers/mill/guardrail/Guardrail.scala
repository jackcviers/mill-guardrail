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

package com.jackcviers.mill.guardrail

import cats._
import cats.data._
import cats.implicits._
import dev.guardrail._
import dev.guardrail.core.StructuredLogger
import dev.guardrail.runner._
import dev.{guardrail => dg}
import mill.Module
import mill._
import mill.api.Result
import mill.define
import mill.define._
import mill.scalalib._

import java.io.File
import java.nio.file.Path

trait Guardrail extends JavaModule with GuardrailPlatform {

  /** Where to find your api spec files. Defaults to the module/guardrail
    * directory.
    */
  def guardrailDirectories: define.Target[Seq[PathRef]] = T.sources {
    Seq(PathRef(millSourcePath / "guardrail"))
  }

  /** The api spec files from which code is generated. Defaults to walking the
    * guardrailDirectories and finding all .json, .yml, and/or .yaml files.
    *
    * Keep in mind that you can customize open api specs with vendor extensions
    * for more control over your scala file generation.
    * @see
    *   https://guardrail.dev/#/extensions
    */
  def guardrailSpecFiles: define.Target[Seq[PathRef]] = T.sources {
    Result.create(
      guardrailDirectories()
        .flatMap { dirRef =>
          os.walk(dirRef.path)
            .filter(f =>
              os.isFile(
                f,
                followLinks = true
              ) && f.ext == "json" || f.ext == "yaml" || f.ext == "yml"
            )
        }
        .map(PathRef(_))
    )
  }

  /** The guardrail tasks to pass to the generator. By default, this will
    * generate arguments for server, client, and model dto files in the
    * guardrail spec files using the defualt guardrail args for scala and use
    * the http4s framework without tracing, custom extraction, or auth and with
    * empty modules ignoring tags. You can override this task and set your own
    * framework and dev.Guardrail.Args for each spec file, including the
    * contextual arguments for dev.guardrail.Context, which controls tracing,
    * custom extraction, module loading, tag behaviors, and auth
    * implementations.
    *
    * @see
    *   the integration tests in itests for details
    * @see
    *   dev.guardrail.Args
    * @see
    *   dev.guardrail.Context
    * @see
    *   https://guardrail.dev/#/scala/README
    * @see
    *   https://guardrail.dev/#/java/README
    */
  def guardrailTasks: define.Task[Agg[Guardrail.LanguageAndArgs]] = T.task {
    import Guardrail.ops._
    val args: Agg[dg.Args] = guardrailSpecFiles().flatMap { specFileRef =>
      for {
        codegenTarget <- List(
          CodegenTarget.Client,
          CodegenTarget.Server,
          CodegenTarget.Models
        )
        empty = dg.Args.empty
      } yield empty
        .withKind(codegenTarget)
        .withSpecPath(Option(specFileRef.path.toString))
        .withOutputPath(Option(T.dest.toString))
        .withPackageName(
          Option(codegenTarget.toPackageName(specFileRef, millSourcePath))
        )
        .modifyContext(c =>
          c.withFramework(Option(Guardrail.Framework.http4s.toString))
        )
    }
    Agg(
      Guardrail.LanguageAndArgs(
        Guardrail.Language.scala,
        args.iterator.to(List)
      )
    )
  }

  /** Generates source code from the guardrailSpecFiles. If
    * shouldGuardrailGenerateClient is true, client files will be generated. If
    * shouldGuardrailGenerateModels is true, DTO models will be generated. If
    * shouldGuardrailGenerateServer is true, server stubs will be generated.
    *
    * @see
    *   https://guardrail.dev/#/
    */
  def guardrailGenerate: define.Target[Seq[PathRef]] = T.sources {
    val (worker, runner, classLoader) = guardrailWorker()
    val currentClassLoader = Thread.currentThread().getContextClassLoader()
    Thread.currentThread().setContextClassLoader(classLoader)
    val result = worker.guardrailGenerate(guardrailTasks(), runner)
    Thread.currentThread().setContextClassLoader(currentClassLoader)
    result
  }

  /** Add the necessary support modules for any generated classes. NOTE - you
    * can override these if you wish.
    */
  def guardrailIvyDeps = T.task {
    Agg(
      ivy"dev.guardrail:guardrail-java-dropwizard_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-java-spring-mvc_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-java-support_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-scala-akka-http_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-scala-dropwizard_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-scala-http4s_2.13:1.0.0-M1",
      ivy"dev.guardrail:guardrail-scala-support_2.13:1.0.0-M1"
    )
  }

  override def generatedSources: T[Seq[PathRef]] = T {
    super.generatedSources() ++ guardrailGenerate()
  }

}

object Guardrail {
  sealed trait Framework
  object Framework {
    private case object Http4s extends Framework {
      override def toString = "http4s"
    }
    private case object `Akka-Http` extends Framework {
      override def toString = "akka-http"
    }
    private case object Dropwizard extends Framework {
      override def toString = "dropwizard"
    }
    private case object `Pekko-Http` extends Framework {
      override def toString = "pekko-http"
    }
    private case object `Spring-Mvc` extends Framework {
      override def toString = "spring-mvc"
    }

    def http4s: Framework = Http4s
    def `akka-http`: Framework = `Akka-Http`
    def dropwizard: Framework = Dropwizard
    def `pekko-http`: Framework = `Pekko-Http`
    def `spring-mvc`: Framework = `Spring-Mvc`
  }
  sealed trait Language
  object Language {
    private case object Java extends Language {
      override def toString = "java"
    }
    private case object Scala extends Language {
      override def toString = "scala"
    }
    def scala: Language = Scala
    def java: Language = Java
  }
  final case class LanguageAndArgs(
      language: Guardrail.Language,
      args: Seq[dg.Args]
  )
  object ops {
    implicit class CodegenTargetOps(val underlying: CodegenTarget)
        extends AnyVal {
      private def pathRefToPackageName(
          pathRef: PathRef,
          millSourcePath: os.Path
      ): IndexedSeq[String] = pathRef.path
        .relativeTo(millSourcePath)
        .segments
        .dropRight(1)

      /** Converts the codegen type to a list of package names.
        */
      def toPackageName(
          pathRef: PathRef,
          millSourcePath: os.Path
      ): List[String] = (underlying match {
        case CodegenTarget.Client =>
          pathRefToPackageName(pathRef, millSourcePath).appended("client")
        case CodegenTarget.Server =>
          pathRefToPackageName(pathRef, millSourcePath).appended("server")
        case CodegenTarget.Models =>
          pathRefToPackageName(pathRef, millSourcePath).appended("models")
      }).iterator.to(List)
    }
  }

}
