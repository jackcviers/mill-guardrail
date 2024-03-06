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

import mill._
import mill.define.ModuleRef
import mill.scalalib._
import java.net.URLClassLoader

trait GuardrailPlatform extends JavaModule { self: Guardrail =>

  protected def guardrailWorkerRef = ModuleRef(GuardrailWorkerModule)

  private def guardrailClasspath: T[Seq[PathRef]] = T {
    resolveDeps(T.task {
      guardrailIvyDeps().map(bindDependency())
    })().toSeq
  }

  protected def guardrailWorker
      : Task[(GuardrailWorker, GuardrailMillRunner, URLClassLoader)] = T.task {
    guardrailWorkerRef()
      .guardrailWorkerManager()
      .get(
        guardrailClasspath()
      )
  }
}
