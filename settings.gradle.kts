rootProject.name = "ksocks-root"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            application()
            net()
            common()
        }
    }
}

/* application-specific libraries */
fun VersionCatalogBuilder.application() {
    /* logging */
    library("kotlin-logging", "io.github.microutils", "kotlin-logging").version("2.1.23")
    library("slf4j-api",      "org.slf4j",            "slf4j-api").version("1.7.36")
    library("logback",        "ch.qos.logback",       "logback-classic").version("1.2.11")
}

/* common libraries */
fun VersionCatalogBuilder.common() {
    version("kotlinx-coroutines", "1.6.4")

    /* kotlin */
    library("kotlin-stdlib",      "org.jetbrains.kotlin",  "kotlin-stdlib").version("1.7.10")

    library("kotlinx-datetime",   "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")
    library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinx-coroutines")

    /* bundles */
    bundle("common", listOf(
        "kotlin-stdlib",
        "kotlinx-coroutines",
        "kotlin-logging"
    ))
}

/* networking */
fun VersionCatalogBuilder.net() {
    /* ktor */
    val ktor = version("ktor", "2.1.0")

    // server/client
    library("ktor-network", "io.ktor", "ktor-network").versionRef(ktor)
}
