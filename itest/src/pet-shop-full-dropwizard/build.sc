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
import dev.guardrail.{Args => _, _}
import dev.{guardrail => dg}

def baseDir = build.millSourcePath

object `pet-shop-full-dropwizard` extends JavaModule with Guardrail {

  override def guardrailTasks = T.task {
    super
      .guardrailTasks()
      .map(languageAndArgs =>
        languageAndArgs.copy(
          language = Guardrail.Language.java,
          args = languageAndArgs.args.map {
            _.modifyContext(c =>
              c.withFramework(Option(Guardrail.Framework.dropwizard.toString))
                .withModules(
                  List(
                    "dropwizard",
                    "async-http-client",
                    "jackson",
                    "java-stdlib"
                  )
                )
            )
          }
        )
      )
  }

  private val jacksonVersion = "2.13.4"
  private val guavaVersion = "30.1-jre"
  private val jerseyVersion = "2.25.1"
  private val jettyVersion = "9.4.35.v20201120"
  private val servletVersion = "3.0.0.v201112011016"
  private val metrics4Version = "4.1.16"
  private val slf4jVersion = "1.7.30"
  private val logbackVersion = "1.2.3"
  private val h2Version = "1.4.197"
  private val jdbi3Version = "3.18.0"
  private val dropwizardVersion = "1.3.29"

  override def ivyDeps = T.task {
    super.ivyDeps() ++ Agg(
      ivy"org.asynchttpclient:async-http-client:2.12.3",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
      ivy"org.glassfish.jersey.media:jersey-media-multipart:3.0.4",
      ivy"javax.xml.bind:jaxb-api:2.3.1",
      ivy"javax.annotation:javax.annotation-api:1.3.2",
      ivy"io.dropwizard:dropwizard-core:${dropwizardVersion}",
      ivy"org.objenesis:objenesis:3.1",
      ivy"org.apache.commons:commons-lang3:3.11",
      ivy"org.apache.commons:commons-text:1.9",
      ivy"com.google.guava:guava:${guavaVersion}",
      ivy"net.sourceforge.argparse4j:argparse4j:0.8.1",
      ivy"com.google.code.findbugs:jsr305:3.0.2",
      ivy"joda-time:joda-time:2.10.9",
      ivy"org.hibernate:hibernate-validator:5.4.3.Final",
      ivy"org.glassfish:javax.el:3.0.0",
      ivy"javax.servlet:javax.servlet-api:3.1.0",
      ivy"org.apache.httpcomponents:httpclient:4.5.13"
        .exclude("commons-logging" -> "commons-logging"),
      ivy"org.apache.tomcat:tomcat-jdbc:9.0.41",
      ivy"com.h2database:h2:${h2Version}",
      ivy"org.jadira.usertype:usertype.core:7.0.0.CR1"
        .exclude("org.slf4j" -> "slf4j-api")
        .exclude("org.joda" -> "joda-money")
        .exclude("org.apache.geronimo.specs" -> "geronimo-jta_1.1_spec"),
      ivy"org.hibernate:hibernate-core:5.2.18.Final"
        .exclude("org.jboss.logging" -> "jboss-logging"),
      ivy"org.javassist:javassist:3.27.0-GA",
      ivy"com.fasterxml:classmate:1.5.1",
      ivy"org.hsqldb:hsqldb:2.5.1",
      ivy"org.liquibase:liquibase-core:3.10.3".exclude(
        "org.yaml" -> "snakeyaml"
      ),
      ivy"com.mattbertolini:liquibase-slf4j:2.0.0"
        .exclude("org.slf4j" -> "slf4j-api")
        .exclude("org.liquibase" -> "liquibase-core"),
      ivy"net.jcip:jcip-annotations:1.0",
      ivy"com.github.spullara.mustache.java:compiler:0.9.7"
        .exclude("com.google.guava" -> "guava"),
      ivy"org.freemarker:freemarker:2.3.30",
      ivy"org.jdbi:jdbi:2.78",
      ivy"org.jdbi:jdbi3-core:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-commons-text:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-freemarker:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-generator:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-gson2:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-guava:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-jackson2:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-jodatime2:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-jpa:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-json:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-kotlin:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-kotlin-sqlobject:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-postgres:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-spring4:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-sqlite:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-sqlobject:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-stringtemplate4:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-testing:${jdbi3Version}",
      ivy"org.jdbi:jdbi3-vavr:${jdbi3Version}",
      ivy"org.checkerframework:checker-qual:3.9.0",
      ivy"org.eclipse.jetty:jetty-server:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-util:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-webapp:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-continuation:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-servlet:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-servlets:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-http:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-alpn-openjdk8-server:${jettyVersion}",
      ivy"org.eclipse.jetty.http2:http2-server:${jettyVersion}",
      ivy"org.eclipse.jetty.http2:http2-client:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-client:${jettyVersion}",
      ivy"org.eclipse.jetty.http2:http2-http-client-transport:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-alpn-openjdk8-client:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-alpn-conscrypt-server:${jettyVersion}",
      ivy"org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.4"
        .exclude("org.eclipse.jetty" -> "jetty-util")
        .exclude("org.eclipse.jetty" -> "jetty-server"),
      ivy"org.eclipse.jetty:jetty-alpn-java-server:${jettyVersion}",
      ivy"org.eclipse.jetty:jetty-alpn-java-client:${jettyVersion}",
      ivy"javax.xml.bind:jaxb-api:2.3.1",
      ivy"com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}",
      ivy"com.fasterxml.jackson.core:jackson-core:${jacksonVersion}",
      ivy"com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-avro:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-csv:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-ion:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-properties:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-smile:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-toml:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${jacksonVersion}",
      ivy"com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-eclipse-collections:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-guava:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-hibernate4:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-hibernate5:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-hppc:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jaxrs:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-joda:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-joda-money:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-json-org:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-jsr353:${jacksonVersion}",
      ivy"com.fasterxml.jackson.datatype:jackson-datatype-pcollections:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-cbor-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-smile-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-xml-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jaxrs:jackson-jaxrs-yaml-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-base:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-cbor-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-smile-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-xml-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-yaml-provider:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jr:jackson-jr-all:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jr:jackson-jr-annotation-support:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jr:jackson-jr-objects:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jr:jackson-jr-retrofit2:${jacksonVersion}",
      ivy"com.fasterxml.jackson.jr:jackson-jr-stree:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-afterburner:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-blackbird:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-guice:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-jaxb-annotations:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-jsonSchema:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-mrbean:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-no-ctor-deser:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-osgi:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-parameter-names:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-paranamer:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-scala_2.11:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-scala_2.12:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-scala_2.13:${jacksonVersion}",
      ivy"com.fasterxml.jackson.module:jackson-module-scala_3:${jacksonVersion}",
      ivy"org.yaml:snakeyaml:1.27",
      ivy"org.glassfish.jersey.core:jersey-common:${jerseyVersion}",
      ivy"org.glassfish.jersey.core:jersey-client:${jerseyVersion}",
      ivy"org.glassfish.jersey.core:jersey-server:${jerseyVersion}",
      ivy"org.glassfish.jersey.bundles:jaxrs-ri:${jerseyVersion}",
      ivy"org.glassfish.jersey.bundles.repackaged:jersey-guava:${jerseyVersion}",
      ivy"org.glassfish.jersey.bundles.repackaged:jersey-jsr166e:${jerseyVersion}",
      ivy"org.glassfish.jersey.connectors:jersey-apache-connector:${jerseyVersion}",
      ivy"org.glassfish.jersey.connectors:jersey-grizzly-connector:${jerseyVersion}",
      ivy"org.glassfish.jersey.connectors:jersey-jetty-connector:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-jetty-http:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-grizzly2-http:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-jetty-servlet:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-jdk-http:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-netty-http:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-servlet:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-servlet-core:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers:jersey-container-simple-http:${jerseyVersion}",
      ivy"org.glassfish.jersey.containers.glassfish:jersey-gf-ejb:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-bean-validation:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-entity-filtering:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-metainf-services:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-mvc:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-mvc-bean-validation:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-mvc-freemarker:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-mvc-jsp:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-mvc-mustache:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-proxy-client:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-servlet-portability:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-spring3:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-declarative-linking:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext:jersey-wadl-doclet:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-weld2-se:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-cdi1x:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-cdi1x-transaction:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-cdi1x-validation:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-cdi1x-servlet:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.cdi:jersey-cdi1x-ban-custom-hk2-binding:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.rx:jersey-rx-client:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.rx:jersey-rx-client-guava:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.rx:jersey-rx-client-java8:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.rx:jersey-rx-client-jsr166e:${jerseyVersion}",
      ivy"org.glassfish.jersey.ext.rx:jersey-rx-client-rxjava:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-jaxb:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-json-jackson:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-json-jackson1:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-json-jettison:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-json-processing:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-kryo:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-moxy:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-multipart:${jerseyVersion}",
      ivy"org.glassfish.jersey.media:jersey-media-sse:${jerseyVersion}",
      ivy"org.glassfish.jersey.security:oauth1-client:${jerseyVersion}",
      ivy"org.glassfish.jersey.security:oauth1-server:${jerseyVersion}",
      ivy"org.glassfish.jersey.security:oauth1-signature:${jerseyVersion}",
      ivy"org.glassfish.jersey.security:oauth2-client:${jerseyVersion}",
      ivy"io.dropwizard.metrics:metrics-annotation:${metrics4Version}",
      ivy"io.dropwizard.metrics:metrics-core:${metrics4Version}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-jvm:${metrics4Version}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-jmx:${metrics4Version}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-servlets:${metrics4Version}"
        .exclude("com.fasterxml.jackson.core" -> "jackson-databind")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-healthchecks:${metrics4Version}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-logback:${metrics4Version}"
        .exclude("ch.qos.logback" -> "logback-classic")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-jersey2:${metrics4Version}"
        .exclude("org.glassfish.jersey.core" -> "jersey-server")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-jetty9:${metrics4Version}"
        .exclude("org.eclipse.jetty" -> "jetty-server")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-httpclient:${metrics4Version}"
        .exclude("commons-logging" -> "commons-logging")
        .exclude("org.slf4j" -> "slf4j-api")
        .exclude("org.apache.httpcomponents" -> "httpclient"),
      ivy"io.dropwizard.metrics:metrics-jdbi:${metrics4Version}"
        .exclude("org.jdbi" -> "jdbi")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-jdbi3:${metrics4Version}"
        .exclude("org.jdbi" -> "jdbi3-core")
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard.metrics:metrics-graphite:${metrics4Version}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"org.slf4j:slf4j-api:${slf4jVersion}",
      ivy"org.slf4j:jul-to-slf4j:${slf4jVersion}",
      ivy"org.slf4j:log4j-over-slf4j:${slf4jVersion}",
      ivy"org.slf4j:jcl-over-slf4j:${slf4jVersion}",
      ivy"ch.qos.logback:logback-access:${logbackVersion}",
      ivy"ch.qos.logback:logback-core:${logbackVersion}",
      ivy"ch.qos.logback:logback-classic:${logbackVersion}"
        .exclude("org.slf4j" -> "slf4j-api"),
      ivy"io.dropwizard:dropwizard-assets:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-auth:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-client:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-configuration:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-core:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-db:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-forms:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-hibernate:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-jackson:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-jdbi:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-jdbi3:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-jersey:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-jetty:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-lifecycle:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-logging:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-metrics:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-metrics-graphite:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-migrations:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-request-logging:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-json-logging:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-servlets:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-testing:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-util:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-validation:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-views:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-views-freemarker:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-views-mustache:${dropwizardVersion}",
      ivy"io.dropwizard:dropwizard-http2:${dropwizardVersion}"
    )
  }

  def verify(): Command[Unit] = T.command {
    compile()

    val expectedSources = Set(
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Address.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/ApiResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Category.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Customer.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Order.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Pet.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/Tag.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/User.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/definitions/package-info.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/Client.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/GetInventoryResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/PlaceOrderResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/GetOrderByIdResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/DeleteOrderResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/Shower.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/ClientException.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/MarshallingException.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/HttpError.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/AsyncHttpClientSupport.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/JacksonSupport.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/client/AsyncHttpClientUtils.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Address.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/ApiResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Category.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Customer.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Order.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Pet.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/Tag.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/User.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/definitions/package-info.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/Handler.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/Resource.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/Shower.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/GuardrailJerseySupport.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/PATCH.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/server/TRACE.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Address.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/ApiResponse.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Category.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Customer.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Order.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Pet.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/Tag.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/User.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/definitions/package-info.java",
      "pet-shop-full-dropwizard/guardrailGenerate.dest/guardrail/models/Shower.java"
    )
    assert(
      generatedSources().exists(f =>
        expectedSources.exists(e =>
          f.toString.replaceAllLiterally("\\", "/").contains(e.toString)
        )
      )
    )
  }

}
