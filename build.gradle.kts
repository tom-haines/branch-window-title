plugins {
  java
  idea
  id("org.jetbrains.intellij") version "1.3.0"
}

// TODO (TH) should be publisher company
group = "org.jetbrains"

version = "1.0.0"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  testImplementation("junit:junit:4.12")
  testImplementation("org.mockito:mockito-core:2.21.0")
  testImplementation("org.assertj:assertj-core:3.21.0")
}

intellij {
  // IntelliJ IDEA releases: https://www.jetbrains.com/intellij-repository/releases e.g. IC-2019.3
  version.set("2021.2.3")

  pluginName.set("branch-window-title")
  // // Disables updating since-build attribute in plugin.xml
  updateSinceUntilBuild.set(false)
  downloadSources.set(true)
  plugins.addAll("git4idea", "svn4idea")
}

tasks.withType(JavaCompile::class.java) {
  options.isDeprecation = true
  options.encoding = "UTF-8"
}