plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":KitUtil"))
    api(libs.app.ashcon.sportpaper)
    api(libs.tc.oc.pgm.core)
}

description = "KitAggregator"
