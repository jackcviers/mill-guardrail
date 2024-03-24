# mill-guardrail
Mill plugin for guardrail generation

## Usage

```scala
import $ivy.`io.github.jackcviers::mill-guardrail::<release-version>`
import io.github.jackcviers.mill.guardrail._


object MyModule extends Guardrail{

  override def ivyDeps = T.task{
    super.ivyDeps() ++ Agg(
	  // specific dedendencies for the framework and guardrail extensions you choose to use.
	)
  }
}
```

By default, this will generate scala code for the server, client, and
model dto files in the guardrail spec files using the defualt
guardrail args for scala and use the http4s framework without tracing,
custom extraction, or auth and with empty modules ignoring tags. You
can override this task and set your own framework and
`dev.Guardrail.Args` for each spec file, including the contextual
arguments for `dev.guardrail.Context`, which controls tracing, custom
extraction, module loading, tag behaviors, and auth implementations
during module compilation. See the Guardrail
[documentation](https://guardrail.dev) for all the possible
configuration options, and tests in the `itest` module for some
demonstrations.

Guardrail only outputs scala 2.13 sources at the moment. You may
consider separating your generated sources in their own modules and
depending upon them for Scala 3 modules, as Scala 3 can use 2.13
dependencies.

### Mill compatibility

mill-guardrail is compatible with mill 0.11.x+.

### Spec File Directory

By default, mill-guardrail will look in `<modulePath>/guardrail/` for
specification json/yml/yaml files. You may customize this by
overriding `Guardrail#guardrailDirectories` in your module definition.

### Filtering and overriding spec files in the directory

By default, mill-guardrail will identify all json, yaml, and yml files
in the Guardrail#guardrailDirectories and their children. You can
override this behavior by overriding Guardrail#guardrailSpecFiles in
your module definition. Commonly, you may want to filter out a
specific spec file. See the example below:

```scala
object MyModule extends Guardrail{

  override def guardrailSpecFiles = T.sources {
    super
      .guardrailSpecFiles()
	  .filter{ pathRef => 
	    !pathRef.path.lastOption.exists(_.contains("specFileToFilter"))
	  }
  }
}
```

### Controlling Code generation

You can choose the framework, language for code generation, and the
other guardrail-specific settings by overriding
`Guardrail#guardrailTasks` in your module. Typically, you may want to
filter out the client, server, or model generation for the
specifications. The tasks are returned in an
`Agg[Guardrail.LanguageAndArgs]`. An example removing server
generation from the default arguments is below:

```scala
import dev.{guardrail => dg}

// ...

object MyModule extends Guardrail{

  override def guardrailTasks: define.Task[Agg[Guardrail.LanguageAndArgs]] = T.task{
    super.guardrailTasks().map{ (languageAndArgs: Guardrail.LanguageAndArgs) =>
	  languageAndArgs.copy(args = languageAndArgs.args.filterNot{ a: (dg.Args) =>
	    a.kind == CodegenTarget.Server
	  })
	}
  }
}
```

See the `itest` test modules `build.sc` and the Guardrail
[documentation](https://guardrail.dev) for more information on how to
customize the language, framework, and Guardrail extension and module
definitions.

#### Examples

- [Scala Default (Http4s Client/Server/Models/Circe)](itest/src/pet-shop-full.build.sc)
- [Scala Default No Server (Http4s/Circe)](itest/src/pet-shop-no-server.build.sc)
- [Scala akka-http](itest/src/pet-shop-scala-akka-http.build.sc)
- [Scala akka-http-jackson](itest/src/pet-shop-scala-akka-http-jackson.build.sc)
- [Java dropwizard](itest/src/pet-shop-full-dropwizard.build.sc)

~~### Limitations~~

~~Java code generation does not work, pending an answer to~~
~~[guardrail/issues/1953](https://github.com/guardrail-dev/guardrail/issues/1953).~~

Special thanks to @blast-hardcheese for helping with the configuration for Java dropwizard.


## Contributing

### License Headers

All code contributed to this repository _MUST_ include the generated
license header, generated using
`https://index.scala-lang.org/lewisjkl/header`. The build for PRs will
run `./mill __.headerCheck` and fail if these headers are not created.

You can add the headers to any files you add with `./mill
__.headerCreate` before comitting.

### Structure

`millguardrail` contains the plug-in source files. The project is set
up for mill cross-building. Inside `millguardrail`, `src-0.11`
contains the mill `0.11.x`-specific files. Contributions extending the
plug-in to earlier versions of mill are greatly appreciated.

`itest` contains the plug-in tests themselves. Issue pull-requests are
welcome with only a test in `itest` to assist with replication.

## Acknowledgements

This plug-in was heavily influenced by
[sbt-guardrail](https://github.com/guardrail-dev/sbt-guardrail),
though it is not quite a 1:1 port.

The build structure was heavily influenced by
[mill-vcs-version](https://github.com/lefou/mill-vcs-version).


