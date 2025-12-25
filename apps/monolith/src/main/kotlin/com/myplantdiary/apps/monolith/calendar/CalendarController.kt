package com.myplantdiary.apps.monolith.calendar

import com.myplantdiary.apps.monolith.common.PageResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/calendar")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class CalendarController(
    private val calendar: CalendarService
) {
    @GetMapping("/day")
    fun day(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("date") date: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<CalendarEntry> = calendar.day(UUID.fromString(jwt.subject), LocalDate.parse(date), page, size, sort)

    @GetMapping("/week")
    fun week(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("start") start: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<CalendarEntry> = calendar.week(UUID.fromString(jwt.subject), LocalDate.parse(start), page, size, sort)

    @GetMapping("/month")
    fun month(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("year") year: Int,
        @RequestParam("month") month: Int,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<CalendarEntry> = calendar.month(UUID.fromString(jwt.subject), year, month, page, size, sort)
}
