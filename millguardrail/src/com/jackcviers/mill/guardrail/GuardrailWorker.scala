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
import dev.{guardrail => dg}
import mill.Agg
import mill.api._

import java.lang.ClassLoader

trait GuardrailWorker {
  def guardrailGenerate(
      tasks: Agg[Guardrail.LanguageAndArgs],
      runner: GuardrailMillRunner
  )(implicit
      ctx: Ctx
  ): Result[Seq[PathRef]]
}

final class GuardrailWorkerImpl extends GuardrailWorker {
  def guardrailGenerate(
      tasks: Agg[Guardrail.LanguageAndArgs],
      runner: GuardrailMillRunner
  )(implicit
      ctx: Ctx
  ): Result[Seq[PathRef]] = {
    val result = tasks
      .filter(_.args.nonEmpty)
      .iterator
      .to(List)
      .flatTraverse { case Guardrail.LanguageAndArgs(language, args) =>
        runner.guardrailRunner(language.toString, args.toArray)
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
                s"dev.guardrail::${name} is a missing dependency. Add it to your ivyDeps for this project."
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
            case UnspecifiedModules(choices) =>
              Result.Failure[Seq[PathRef]](
                s"You are missing some modules in the context. Please choose from the following choices:$choices"
              )
            case UnusedModules(found) =>
              Result.Failure[Seq[PathRef]](
                s"You have a unused in the modules in your context: $found. Please remove them."
              )
            case _ =>
              Result.Failure[Seq[PathRef]](
                s"Internal Guardrail failure. Check your spec files and guardrailTaskDefinition overrides carefully. See the Guardrail docs at https://guardrail.dev."
              )
          }
        },
        value => Result.Success(value.map { p => PathRef(os.Path(p)) })
      )
    result
  }
}
