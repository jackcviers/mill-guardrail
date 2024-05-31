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

// import $ivy.`io.github.davidgregory084::mill-tpolecat::0.0.0-68-5779b6`
// import io.github.davidgregory084.TpolecatModule
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.7.1`
import de.tobiasroeser.mill.integrationtest._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version._
// import $ivy.`io.github.kierendavies::mill-explicit-deps::0.2.0-57-207608-DIRTYb69d7b2d-SNAPSHOT`
// import io.github.kierendavies.mill.explicitdeps.ExplicitDepsModule
import $ivy.`com.github.lolgab::mill-mima::0.1.1`
import com.github.lolgab.mill.mima._
import $ivy.`com.lewisjkl::header-mill-plugin::0.0.3`
import header._
import mill._
import mill.scalalib._
import mill.scalalib.api._
import mill.scalalib.publish._

import scala.util.Properties
import scala.util.Try

lazy val baseDir = build.millSourcePath

trait Deps {
  def millPlatform: String
  def millVersion: String
  def scalaVersion: String = "2.13.14"
  def testWithMill: Seq[String]

  def mimaPreviousVersions: Seq[String] = Seq("0.0.1-RC-5", "0.0.1-RC-6")

  private val guardrailVersion = "1.0.0-M1"

  def `mill-moduledefs` = ivy"com.lihaoyi::mill-moduledefs:0.10.9"
  def `os-lib` = ivy"com.lihaoyi::os-lib:0.10.0"
  def `upickle-core` = ivy"com.lihaoyi::upickle-core:3.2.0"
  def `upickle-implicits` = ivy"com.lihaoyi::upickle-implicits:3.2.0"
  def mainargs = ivy"com.lihaoyi::mainargs:0.6.3"
  def sourcecode = ivy"com.lihaoyi::sourcecode:0.3.1"
  def upickle = ivy"com.lihaoyi::upickle:3.2.0"
  val `cats-core` = ivy"org.typelevel::cats-core:2.10.0"
  val `guardrail-core` = ivy"dev.guardrail::guardrail-core:$guardrailVersion"
  val `mill-main-api` = ivy"com.lihaoyi::mill-main-api:${millVersion}"
  val `mill-scalalib` = ivy"com.lihaoyi::mill-scalalib:${millVersion}"
  val millTestkit = ivy"com.lihaoyi::mill-main-testkit:${millVersion}"
  val munit = ivy"org.scalameta::munit::0.7.29"
  val millMain = ivy"com.lihaoyi::mill-main:${millVersion}"
}

class Deps_latest(override val millVersion: String) extends Deps {
  override def millPlatform = millVersion
  override def testWithMill = Seq(millVersion)
  override def mimaPreviousVersions = Seq()
}
object Deps_0_11 extends Deps {
  override def millPlatform = "0.11"
  override def millVersion = "0.11.0" // scala-steward:off
  override def testWithMill =
    Seq("0.11.1", "0.11.2", "0.11.4", "0.11.5", "0.11.6", "0.11.7", millVersion)
  override def mimaPreviousVersions = Seq()
}

lazy val latestDeps: Seq[Deps] = {
  val path = baseDir / "MILL_DEV_VERSION"
  interp.watch(path)
  println(s"Checking for file ${path}")
  if (os.exists(path)) {
    Try {
      Seq(new Deps_latest(os.read(path).trim()))
    }
      .recover { _ => Seq() }
  }.get
  else Seq()
}

lazy val crossDeps: Seq[Deps] =
  (Seq(Deps_0_11) ++ latestDeps).distinct
lazy val millApiVersions = crossDeps.map(x => x.millPlatform -> x)
lazy val millItestVersions = crossDeps.flatMap(x => x.testWithMill.map(_ -> x))

trait BaseModule
    extends ScalaModule
    // with ExplicitDepsModule
    with Mima
    with PublishModule
    with HeaderModule
    // with TpolecatModule
    {
  // def ignoreUnimportedIvyDeps: Task[Dep => Boolean] = T.task((_: Dep) => false)

  override def license: HeaderLicense =
    HeaderLicense.Apache2("2024", "Jack Viers")

  def millApiVersion: String
  def deps: Deps = millApiVersions.toMap.apply(millApiVersion)
  def scalaVersion = deps.scalaVersion
  override def artifactSuffix: T[String] =
    s"_mill${deps.millPlatform}_${artifactScalaVersion()}"

  override def ivyDeps = T {
    Agg(
      ivy"${scalaOrganization()}:scala-library:${scalaVersion()}",
      deps.`mill-main-api`,
      deps.`os-lib`,
      deps.sourcecode,
      deps.`cats-core`,
      deps.`guardrail-core`,
      deps.`mill-scalalib`,
      deps.upickle,
      deps.`upickle-core`,
      deps.`upickle-implicits`,
      deps.mainargs,
      deps.`mill-moduledefs`,
      ivy"com.lihaoyi::mill-main-define:${deps.millVersion}"
    )
  }

  def publishVersion =
    VcsVersion.vcsState().format(untaggedSuffix = "-SNAPSHOT")
  override def versionScheme: T[Option[VersionScheme]] = T(
    Option(VersionScheme.EarlySemVer)
  )

  override def mimaPreviousVersions = deps.mimaPreviousVersions
  override def mimaPreviousArtifacts: Target[Agg[Dep]] = T {
    val md = artifactMetadata()
    Agg.from(
      mimaPreviousVersions().map(v => ivy"${md.group}:${md.id}:${v}")
    )
  }

  override def sources = T.sources {
    println(s"millSourcePath: $millSourcePath")
    Seq(PathRef(millSourcePath / "src")) ++
      (ZincWorkerUtil.matchingVersions(millApiVersion) ++
        ZincWorkerUtil.versionRanges(
          millApiVersion,
          crossDeps.map(_.millPlatform)
        ))
        .map(p => PathRef(millSourcePath / s"src-${p}"))
  }

  override def javacOptions = {
    (if (Properties.isJavaAtLeast(9)) Seq("--release", "8")
     else Seq("-source", "1.8", "-target", "1.8")) ++
      Seq("-encoding", "UTF-8", "-deprecation")
  }

  override def scalacOptions =
    Seq("-release:8", "-encoding", "UTF-8", "-deprecation")

  def pomSettings = T {
    PomSettings(
      description = "Guardrail generation for mill",
      organization = "com.jackcviers",
      url = "https://github.com/jackcviers/mill-guardrail",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("jackcviers", "mill-guardrail"),
      developers = Seq(
        Developer("jackcviers", "Jack Viers", "https.//github.com/jackcviers")
      )
    )
  }
  trait Tests extends ScalaTests with TestModule.Munit {
    override def ivyDeps = Agg(deps.munit, deps.millMain, deps.millTestkit)
  }
}

object millguardrail
    extends Cross[MillGuardrailCross](millApiVersions.map(_._1))

trait MillGuardrailCross extends BaseModule with Cross.Module[String] {

  override def millApiVersion: String = crossValue
  override def artifactName = "mill-guardrail"
  override def skipIdea: Boolean = deps != crossDeps.head
  override def compileIvyDeps = Agg(deps.millMain)
  object test extends Tests {
    override def ivyDeps = Agg(deps.munit, deps.millMain)
  }
}

object itest
    extends Cross[ItestCross](millItestVersions.map(_._1))
    with TaskModule {
  override def defaultCommandName(): String = "generatedSources"
  def testCached: T[Seq[TestCase]] = itest(
    millItestVersions.map(_._1).head
  ).testCached
  def test(args: String*): Command[Seq[TestCase]] =
    itest(millItestVersions.map(_._1).head).test(args: _*)

}

trait ItestCross extends MillIntegrationTestModule with Cross.Module[String] {
  def millItestVersion = crossValue

  val millApiVersion =
    millItestVersions.toMap.apply(millItestVersion).millPlatform
  def deps: Deps = millApiVersions.toMap.apply(millApiVersion)

  override def millTestVersion = millItestVersion
  override def pluginsUnderTest = Seq(millguardrail(millApiVersion))

  override def testInvocations
      : Target[Seq[(PathRef, Seq[TestInvocation.Targets])]] = T {
    testCases().map { pathref =>
      pathref.path.last match {
        case "pet-shop-scala-akka-http-jackson" =>
          pathref -> Seq(
            TestInvocation.Targets(
              Seq("-d", "-j", "0", "pet-shop-scala-akka-http-jackson.verify"),
              noServer = false
            )
          )
        case "pet-shop-no-server" =>
          pathref -> Seq(
            TestInvocation.Targets(
              Seq("-d", "-j", "0", "pet-shop-no-server.verify"),
              noServer = false
            )
          )
        case "pet-shop-scala-akka-http" =>
          pathref -> Seq(
            TestInvocation.Targets(
              Seq("-d", "-j", "0", "pet-shop-scala-akka-http.verify"),
              noServer = false
            )
          )
        case "pet-shop-java-spring-mvc" =>
          pathref -> Seq(
            TestInvocation.Targets(
              Seq("-d", "-j", "0", "pet-shop-java-spring-mvc.verify")
            )
          )
        case _ =>
          pathref -> Seq(
            TestInvocation.Targets(
              Seq("-d", "-j", "0", "pet-shop-full.verify"),
              noServer = false
            )
          )
      }
    }
  }
  override def perTestResources = T.sources {
    Seq(generatedSharedSrc())
  }

  def generatedSharedSrc = T {
    os.write(
      T.dest / "shared.sc",
      ""
    )
    PathRef(T.dest)
  }
}

def findLatestMill(toFile: String = "") = T.command {
  import coursier._
  val versions =
    Versions(
      cache
        .FileCache()
        .withTtl(
          concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.MINUTES)
        )
    )
      .withModule(mod"com.lihaoyi:mill-main_2.13")
      .run()
  println(s"Latest Mill versions: ${versions.latest}")
  if (toFile.nonEmpty) {
    val path = os.Path.expandUser(toFile, os.pwd)
    println(s"Writing file: ${path}")
    os.write.over(path, versions.latest, createFolders = true)
  }
  versions.latest
}
