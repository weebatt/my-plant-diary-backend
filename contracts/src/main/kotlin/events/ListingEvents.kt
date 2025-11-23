package com.myplantdiary.contracts.events

import kotlinx.serialization.Serializable

@Serializable
data class ListingPublishRequested(
    val listingId: String,
    val userId: String,
    val title: String,
    val description: String,
    val priceCents: Long,
    val photos: List<String>
)

@Serializable
data class ListingPublished(
    val listingId: String,
    val externalId: String // e.g., Avito ad id
)

@Serializable
data class ListingPublishFailed(
    val listingId: String,
    val reason: String
)

