package com.jackcviers.mill.guardrail

import cats._
import cats.data._
import cats.implicits._
import dev.guardrail._
import dev.{guardrail => dg}
import dev.guardrail.core.StructuredLogger
import dev.guardrail.runner._
import mill.Module
import mill._
import mill.api.Result
import mill.define
import mill.define._
import mill.scalalib._

import java.nio.file.Path

trait Guardrail extends JavaModule {

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
    val args: Agg[dg.Args] = guardrailSpecFiles().flatMap { specFileRef =>
      for {
        codegenTarget <- List(
          CodegenTarget.Client,
          CodegenTarget.Server,
          CodegenTarget.Models
        )
      } yield dg.Args.empty.copy(
        kind = codegenTarget,
        specPath = Option(specFileRef.path.toString),
        outputPath = Option(T.dest.toString),
        context =
          Context.empty.copy(Option(Guardrail.Framework.http4s.toString))
      )
    }
    Agg(
      Guardrail.LanguageAndArgs(
        Guardrail.Language.scala,
        args.iterator.to(List)
      )
    )
  }

  /** Runs guardrail generation
    */
  object guardrailMillRunner extends GuardrailRunner

  /** Generates source code from the guardrailSpecFiles. If
    * shouldGuardrailGenerateClient is true, client files will be generated. If
    * shouldGuardrailGenerateModels is true, DTO models will be generated. If
    * shouldGuardrailGenerateServer is true, server stubs will be generated.
    *
    * @see
    *   https://guardrail.dev/#/
    */
  def guardrailGenerate: define.Target[Seq[PathRef]] = T.sources {
    // run the runner, fold over the error, logging the structured log on error and returning the paths in Agg.from on success.
    guardrailTasks()
      .filter(_.args.nonEmpty)
      .iterator
      .to(List)
      .flatTraverse { case Guardrail.LanguageAndArgs(language, args) =>
        guardrailMillRunner.guardrailRunner(
          Map(
            language.toString -> NonEmptyList
              .of[dg.Args](args.head, args.tail: _*)
          )
        )
      }
      .fold(
        (error) => {
          error match {
            case MissingArg(arg, name) =>
              Result
                .Failure[Seq[PathRef]](s"In $arg, ${name.value} is invalid.")
            case UnknownArguments(args) =>
              Result
                .Failure[Seq[PathRef]](s"${args.mkString(", ")} are invalid.")
            case UnknownFramework(name) =>
              Result.Failure[Seq[PathRef]](s"${name} is not a valid framework")
            case MissingDependency(name) =>
              Result.Failure[Seq[PathRef]](
                s"${name} is a missing dependency. Add it to your ivyDeps for this project."
              )
            case UnparseableArgument(name, message) =>
              Result.Failure[Seq[PathRef]](
                s"$name is unparseable: message was: ${message}"
              )
            case _: NoArgsSpecified.type =>
              Result.Failure[Seq[PathRef]](
                s"Args was empty. Please define them in a guardrailTasks override or remove the override."
              )
            case _: NoFramework.type =>
              Result.Failure[Seq[PathRef]](
                s"You must define a framework in your arg context."
              )
            case _: PrintHelp.type =>
              Result.Failure[Seq[PathRef]](s"This shouldn't happen.")
            case RuntimeFailure(message) =>
              Result.Failure[Seq[PathRef]](message)
            case UserError(message) =>
              Result.Failure[Seq[PathRef]](s"A user error occurred: $message")
            case MissingModule(section, choices) =>
              Result.Failure[Seq[PathRef]](
                s"You are missing a module in $section. Please define it in your arg context from among the following choices: ${choices
                    .mkString(", ")}"
              )
            case ModuleConflict(section) =>
              Result.Failure[Seq[PathRef]](
                s"You have a conflict in the modules in $section. Please only define modules that work together according to the Guardrail docs at https://guardrail.dev"
              )
            case _ =>
              Result.Failure[Seq[PathRef]](
                s"Internal Guardrail failure. Check your spec files and guardrailTaskDefinition overrides carefully. See the Guardrail docs at https://guardrail.dev."
              )
          }
        },
        value => Result.Success(value.map { p => PathRef(os.Path(p)) })
      )
  }

  /** Add the necessary support modules for any generated classes. NOTE - you
    * can override these if you wish.
    */
  override def ivyDeps = T.task {
    super.ivyDeps() ++ Agg(
      ivy"dev.guardrail::guardrail-java-dropwizard:0.71.0",
      ivy"dev.guardrail::guardrail-java-spring-mvc:0.71.0",
      ivy"dev.guardrail::guardrail-java-support:0.71.0",
      ivy"dev.guardrail::guardrail-scala-akka-http:0.71.0",
      ivy"dev.guardrail::guardrail-scala-dropwizard:0.71.0",
      ivy"dev.guardrail::guardrail-scala-http4s:0.71.0",
      ivy"dev.guardrail::guardrail-scala-support:0.71.0"
    )
  }

  // add to generated sources the guardrailGenerate
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

    def http4s: Framework = Http4s
    def `akka-http`: Framework = `Akka-Http`
    def dropwizard: Framework = `Dropwizard`
    def `pekko-http`: Framework = `Pekko-Http`
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

}
