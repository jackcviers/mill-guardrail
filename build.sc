import $ivy.`io.github.davidgregory084::mill-tpolecat::0.0.0-68-5779b6`
import io.github.davidgregory084.TpolecatModule
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.7.1`
import de.tobiasroeser.mill.integrationtest._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version._
import $ivy.`io.github.kierendavies::mill-explicit-deps::0.2.0-57-207608-DIRTYb69d7b2d-SNAPSHOT`
import io.github.kierendavies.mill.explicitdeps.ExplicitDepsModule
import $ivy.`com.github.lolgab::mill-mima::0.1.0`
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
  def scalaVersion: String = "2.13.13"
  def testWithMill: Seq[String]

  def mimaPreviousVersions: Seq[String] = Seq()

  private val guardrailVersion = "0.75.4"
  val millMain = ivy"com.lihaoyi::mill-main:${millVersion}"
  val `mill-main-api` = ivy"com.lihaoyi::mill-main-api:${millVersion}"
  val `mill-scalalib` = ivy"com.lihaoyi::mill-scalalib:${millVersion}"
  val millTestkit = ivy"com.lihaoyi::mill-main-testkit:${millVersion}"
  val munit = ivy"org.scalameta::munit::0.7.29"
  val `guardrail-core` = ivy"dev.guardrail::guardrail-core:$guardrailVersion"
  def `os-lib` = ivy"com.lihaoyi::os-lib:0.8.0"
  def sourcecode = ivy"com.lihaoyi::sourcecode:0.2.7"
  val `cats-core` = ivy"org.typelevel::cats-core:2.7.0"
  def `upickle-core` = ivy"com.lihaoyi::upickle-core:1.4.3"
  def upickle = ivy"com.lihaoyi::upickle:1.4.3"

}

class Deps_latest(override val millVersion: String) extends Deps {
  override def millPlatform = millVersion
  override def testWithMill = Seq(millVersion)
  override def mimaPreviousVersions = Seq()
}
object Deps_0_11 extends Deps {
  override def millPlatform = "0.11"
  override def millVersion = "0.11.0" // scala-steward:off
  override def testWithMill = Seq(millVersion)
  override def mimaPreviousVersions = Seq()
  override def `os-lib` = ivy"com.lihaoyi::os-lib:0.9.1"
  override def sourcecode = ivy"com.lihaoyi::sourcecode:0.3.0"
  override def `upickle-core` = ivy"com.lihaoyi::upickle-core:3.1.0"
  def upickle = ivy"com.lihaoyi::upickle:3.1.0"

}
object Deps_0_10 extends Deps {
  override def millPlatform = "0.10"
  override def millVersion = "0.10.0" // scala-steward:off
  // 0.10.4 and 0.10.3 don't run in CI on Windows
  override def testWithMill = Seq("0.10.12", "0.10.5", millVersion)
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
  (Seq(Deps_0_11, Deps_0_10) ++ latestDeps).distinct
lazy val millApiVersions = crossDeps.map(x => x.millPlatform -> x)
lazy val millItestVersions = crossDeps.flatMap(x => x.testWithMill.map(_ -> x))

trait BaseModule
    extends ScalaModule
    with ExplicitDepsModule
    with Mima
    with PublishModule
    with HeaderModule
    with TpolecatModule {
  def ignoreUnimportedIvyDeps: Task[Dep => Boolean] = T.task((_: Dep) => false)

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
      deps.`mill-scalalib`
    ) ++ {
      if (deps.millPlatform != "0.10")
        Agg(
          deps.upickle,
          deps.`upickle-core`,
          ivy"com.lihaoyi::mill-moduledefs:0.10.9",
          ivy"com.lihaoyi::mill-main-define:${deps.millVersion}"
        )
      else
        Agg(
          ivy"com.lihaoyi::mill-main-core:${deps.millVersion}",
          ivy"com.lihaoyi::mill-main-moduledefs:${deps.millVersion}",
          deps.upickle,
          deps.`upickle-core`,
          ivy"com.lihaoyi::upickle-implicits:1.4.3"
        )
    }
  }

  def publishVersion = VcsVersion.vcsState().format()
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
      organization = "com.github.jackcviers",
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
  override def artifactName = "com.jackcviers.mill.guardrail"
  override def skipIdea: Boolean = deps != crossDeps.head
  override def compileIvyDeps = Agg(deps.millMain)
  object test extends Tests {
    override def ivyDeps = Agg(deps.munit, deps.millMain)
  }
}

object itest
    extends Cross[ItestCross](millItestVersions.map(_._1))
    with TaskModule {
  override def defaultCommandName(): String = "test"
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
        case "pet-shop-client-only" =>
          pathref -> Seq(
            TestInvocation.Targets(Seq("-d", "verify")),
            TestInvocation.Targets(
              Seq("com.jackcviers.mill.guardrail.Guardrail/generate")
            ),
            TestInvocation.Targets(
              Seq("com.jackcviers.mill.guardrail.Guardrail/clientGenerate")
            )
          )
        case _ =>
          pathref -> Seq(
            TestInvocation.Targets(Seq("-d", "verify")),
            TestInvocation.Targets(
              Seq("com.jackcviers.mill.guardrail.Guardrail/generate")
            ),
            TestInvocation.Targets(
              Seq("com.jackcviers.mill.guardrail.Guardrail/serverGenerate")
            ),
            TestInvocation.Targets(
              Seq("com.jackcviers.mill.guardrail.Guardrail/clientGenerate")
            )
          )
      }
    }
  }
}
