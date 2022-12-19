buildscript {

   repositories {
      mavenCentral()
      mavenLocal()
   }

   repositories {
      mavenCentral()
   }
}

plugins {
   java
   `java-library`
   id("java-library")
   id("maven-publish")
   signing
   id("com.adarshr.test-logger") version "2.0.0"
   kotlin("jvm").apply(false).version("1.6.21")
}

allprojects {

   repositories {
      mavenCentral()
      google()
      mavenLocal()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      }
   }

   group = "com.sksamuel.scrimage"
   version = Ci.version
}

val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

signing {
   useGpgCmd()
   if (Ci.isRelease)
      sign(publications)
}
