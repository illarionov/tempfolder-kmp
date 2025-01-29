plugins {
    id("at.released.tempfolder.gradle.lint.detekt")
    id("at.released.tempfolder.gradle.lint.diktat")
    id("at.released.tempfolder.gradle.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
