plugins {
   `java-library`
   kotlin("jvm")
}

dependencies {
   implementation(Libs.TwelveMonkeys.imageIoCore)
   implementation(Libs.TwelveMonkeys.jpeg)
   implementation(Libs.Drewnoaks.metadataExtractor)
   implementation(Libs.Zh.opengif)
   implementation(Libs.Commons.io)
   implementation(Libs.Hjg.pngj)
   implementation(project(":scrimage-core"))
   testImplementation(kotlin("stdlib"))
   testImplementation(kotlin("stdlib-jdk8"))
   testImplementation(Libs.Kotest.datatest)
   testImplementation(Libs.Kotest.junit5)
   testImplementation(Libs.Kotest.assertions)
}

java {
   sourceCompatibility = JavaVersion.VERSION_1_8
   targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<Test>("test") {
   useJUnitPlatform()
   filter {
      isFailOnNoMatchingTests = false
   }
   testLogging {
      showExceptions = true
      showStandardStreams = true
      events = setOf(
         org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
         org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
      )
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
   }
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
   jvmTarget = "1.8"
}
