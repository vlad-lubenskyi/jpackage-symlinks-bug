import com.teamdev.jxbrowser.engine.Engine
import com.teamdev.jxbrowser.engine.EngineOptions
import com.teamdev.jxbrowser.engine.NoLicenseException
import com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://europe-maven.pkg.dev/jxbrowser/releases")
    }
    dependencies {
        add("classpath", "com.teamdev.jxbrowser:jxbrowser:8.5.0")
        add("classpath", "com.teamdev.jxbrowser:jxbrowser-mac-arm:8.5.0")
    }
}

plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    register("unpackChromiumBundle") {
        doLast {
            val options = EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .chromiumDir(projectDir.resolve("build/additional-content/Helpers").toPath())
            try {
                // This will extract Chromium bundle to the directory configured above.
                Engine.newInstance(options.build())
            } catch (expected: NoLicenseException) {
                // We don't need to configure license to extract the binaries.
            }
        }
    }

    jar {
        archiveFileName.set("main.jar")
    }
}
