plugins {
  java
  idea
  id("org.jetbrains.intellij") version "1.15.0"
}

group = "org.jetbrains"

version = "1.1.0"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.7.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core:2.21.0")
  testImplementation("org.assertj:assertj-core:3.24.0")
}

intellij {
  // IntelliJ IDEA releases: https://www.jetbrains.com/intellij-repository/releases e.g. IC-2019.3
  // and see https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html#platformVersions
  version.set("2021.2.3")
  pluginName.set("branch-window-title")
  downloadSources.set(true)
  // name defined in e.g. plugins/vcs-git/lib/vcs-git/META-INF/plugin.xml
  plugins.set(listOf("Git4Idea", "Subversion"))
}

tasks.withType(JavaCompile::class.java) {
  options.isDeprecation = true
  options.encoding = "UTF-8"
}

tasks {
  test { useJUnitPlatform() }

  patchPluginXml {
    sinceBuild.set("${project.properties["pluginSinceBuild"]}")
    version.set("${project.properties["pluginVersion"]}")
  }
}

