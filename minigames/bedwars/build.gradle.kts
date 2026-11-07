plugins {
    id("muxe.bukkit-plugin")
}

dependencies {
    compileOnly(projects.core.bukkit)
    implementation(projects.minigames.api)

    testImplementation(projects.core.bukkit)
    testImplementation(testFixtures(projects.core.common))
    testImplementation(testFixtures(projects.minigames.api))
    testImplementation(libs.bundles.testcontainers)
}
