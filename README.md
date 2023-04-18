# KitRecommender

KitRecommender is a minecraft plugin that integrates with [PGM](https://github.com/PGMDev/PGM) to offer players 
automatically sorted kits based on their preferences.

This repository contains all the java code to:
 - [Plugin](/plugin) Sort kits based on what the user does in the match
 - [Plugin](/plugin) Predict kits based on what the user has historically done (configurable)
 - [Plugin](/plugin) Store data on what users have historically been doing for model training purposes
 - [Aggregator](/aggregator) Aggregate the data per player to have data to test ML models on
 - [Rewritter](/rewritter) Rewrite data from parquet v2 to v1 format for compatibility (python library failed to support some v2 features).

The files created by collecting & aggregating data can be used python code hosted in 
[KitModels](https://github.com/Pablete1234/KitModels) to test and validate models.