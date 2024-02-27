import $file.plugins
import $file.shared

import com.jackcviers.mill.guardrail._
import mill._
import mill.api.PathRef
import mill.define.Command


def baseDir = build.millSourcePath

def verify = T.command{
  val result = GaurdRail.generate()
  val expectedSources = Set.empty[PathRef]
  assert(expectedSources.subsetOf(generatedSources().toSet))
}

