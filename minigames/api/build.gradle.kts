plugins {
    id("muxe.bukkit-library")
    `java-test-fixtures`
}

dependencies {
    compileOnly(projects.core.bukkit)
    api(projects.minigames.engine)

    testFixturesImplementation(projects.core.bukkit)
    testFixturesImplementation(libs.spigot.api)

    testImplementation(projects.core.bukkit)
    testImplementation(testFixtures(projects.core.common))
}
