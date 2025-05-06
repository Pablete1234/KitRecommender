rootProject.name = "KitParent"
include(":KitRecommender")
include(":KitUtil")
project(":KitRecommender").projectDir = file("plugin")
project(":KitUtil").projectDir = file("util")
