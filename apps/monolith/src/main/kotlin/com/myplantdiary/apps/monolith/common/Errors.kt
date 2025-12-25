package com.myplantdiary.apps.monolith.common

open class NotFoundException(message: String) : RuntimeException(message)
open class ConflictException(message: String) : RuntimeException(message)
open class BadRequestException(message: String) : RuntimeException(message)

