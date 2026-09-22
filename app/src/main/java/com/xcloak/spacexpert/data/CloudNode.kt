package com.xcloak.spacexpert.data

data class CloudNode(
    val providerName: String,
    val connectedAccount: String,
    val totalSpaceBytes: Long,
    val usedSpaceBytes: Long,
    val orphanPayloadsCount: Int
)
