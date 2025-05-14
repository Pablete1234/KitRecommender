rootProject.name = "KitParent"
include(":KitRecommender")
include(":KitUtil")
/* include(":KitAggregator") */
/* include(":KitRewritter") */
project(":KitRecommender").projectDir = file("plugin")
project(":KitUtil").projectDir = file("util")
/* project(":KitAggregator").projectDir = file("aggregator") */
/* project(":KitRewritter").projectDir = file("rewritter") */
