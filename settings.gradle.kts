pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CheckoutPayment"
include(":app")
include(":payment-api")
include(":payment-domain")
include(":payment-data")
include(":payment-3ds")
include(":payment-ui")
include(":payment-sdk")
include(":payment-testing")
