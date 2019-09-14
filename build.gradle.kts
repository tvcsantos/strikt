@file:Suppress("UNUSED_VARIABLE", "UnstableApiUsage")

import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.KotlinterExtension

plugins {
  id("nebula.release") version "12.0.0"
  id("nebula.kotlin") version "1.3.50" apply false
  id("org.jmailen.kotlinter") version "2.1.1" apply false
  id("info.solidsoft.pitest") version "1.4.0" apply false
  id("com.github.ben-manes.versions") version "0.25.0"
}

buildscript {
  configurations.maybeCreate("pitest")
  dependencies {
    "pitest"("org.pitest:pitest-junit5-plugin:0.9")
  }
}

allprojects {
  group = "io.strikt"
}

subprojects {
  apply(plugin = "nebula.release")

  repositories {
    jcenter()
  }

  afterEvaluate {
    plugins.withId("kotlin") {
      configure<JavaPluginConvention> {
        sourceCompatibility = VERSION_1_8
      }

      tasks.withType<KotlinCompile> {
        kotlinOptions {
          jvmTarget = VERSION_1_8.toString()
          languageVersion = "1.3"
          freeCompilerArgs = listOf("-progressive")
        }
      }

      // Test with JUnit 5
      dependencies {
        "implementation"(platform("org.jetbrains.kotlin:kotlin-bom:1.3.50"))
        "implementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.3.1"))
        "testImplementation"(platform("org.junit:junit-bom:5.5.2"))
        "testImplementation"("org.junit.jupiter:junit-jupiter-api")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
      }
      tasks.withType<Test> {
        systemProperty("junit.jupiter.execution.parallel.enabled", "false")
        useJUnitPlatform {
          includeEngines("junit-jupiter")
        }
      }

      // Lint Kotlin code
      apply(plugin = "org.jmailen.kotlinter")
      configure<KotlinterExtension> {
        ignoreFailures = true
        indentSize = 2
        continuationIndentSize = 4
        reporters = arrayOf("html", "plain")
      }
    }

    plugins.withId("info.solidsoft.pitest") {
      configure<PitestPluginExtension> {
        jvmArgs = listOf("-Xmx512m")
        testPlugin = "junit5"
        avoidCallsTo = setOf("kotlin.jvm.internal")
        mutators = setOf("NEW_DEFAULTS")
        targetClasses = setOf("strikt.*")  //by default "${project.group}.*"
        targetTests = setOf("strikt.**.*")
        pitestVersion = "1.4.10"
        threads = System.getenv("PITEST_THREADS")?.toInt()
          ?: Runtime.getRuntime().availableProcessors()
        outputFormats = setOf("XML", "HTML")
      }
    }
  }
}
