package com.myplantdiary.apps.monolith.common

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)

fun <T> Page<T>.toPageResponse(): PageResponse<T> = PageResponse(
    items = content,
    page = number,
    size = size,
    totalItems = totalElements,
    totalPages = totalPages
)

