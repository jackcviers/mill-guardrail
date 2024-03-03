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

package io.github.jackcviers.mill.guardrail

import mill.api._

import java.net.URL
import java.net.URLClassLoader
import scala.collection.concurrent.TrieMap

class GuardrailWorkerManagerImplementation()
    extends GuardrailWorkerManager
    with AutoCloseable {

  private val cache: TrieMap[Seq[
    PathRef
  ], (GuardrailWorker, GuardrailMillRunner, WorkerCount, URLClassLoader)] =
    TrieMap.empty

  def get(toolsClasspath: Seq[PathRef])(implicit
      ctx: Ctx
  ): (GuardrailWorker, GuardrailMillRunner, URLClassLoader) = {
    val distinctClasspath = toolsClasspath.distinct
    val (worker, runner, count, classLoader) = cache.getOrElseUpdate(
      distinctClasspath, {
        val classLoader = new URLClassLoader(
          distinctClasspath.map(_.path.toNIO.toUri().toURL()).toArray[URL],
          getClass().getClassLoader()
        )
        val workerClass =
          classLoader.loadClass(classOf[GuardrailWorkerImpl].getName())
        val worker = workerClass
          .getConstructor()
          .newInstance()
          .asInstanceOf[GuardrailWorker]
        val runner = classLoader
          .loadClass(classOf[GuardrailMillRunner].getName())
          .getConstructor()
          .newInstance()
          .asInstanceOf[GuardrailMillRunner]
        (worker, runner, WorkerCount(0), classLoader)
      }
    )
    cache.replace(
      distinctClasspath,
      (worker, runner, WorkerCount(count.value + 1), classLoader)
    )
    (worker, runner, classLoader)
  }

  override def close(): Unit = cache.clear()
}
