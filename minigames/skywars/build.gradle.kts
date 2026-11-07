plugins {
    id("muxe.bukkit-plugin")
}

dependencies {
    compileOnly(projects.core.bukkit)
    implementation(projects.minigames.api)

    testImplementation(projects.core.bukkit)
}
