plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":payment-api"))
    api(project(":payment-domain"))

    // Coroutines (for suspend fakes)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
