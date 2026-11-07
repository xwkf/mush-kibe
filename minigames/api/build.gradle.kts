plugins {
    id("muxe.bukkit-library")
}

dependencies {
    compileOnly(projects.core.bukkit)
    api(projects.minigames.engine)

    testImplementation(projects.core.bukkit)
    testImplementation(testFixtures(projects.core.common))
}
