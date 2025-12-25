package com.myplantdiary.apps.monolith.calendar

import com.myplantdiary.apps.monolith.common.PageResponse
import com.myplantdiary.apps.monolith.common.toPageResponse
import com.myplantdiary.apps.monolith.diary.Reminder
import com.myplantdiary.apps.monolith.diary.ReminderRepository
import com.myplantdiary.apps.monolith.diary.UserPlantRepository
import com.myplantdiary.apps.monolith.dictionary.PlantRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class CalendarEntry(
    val reminderId: String,
    val userPlantId: String,
    val kind: String,
    val dueAt: String,
    val nickname: String?,
    val plantLatinName: String?,
    val plantCommonName: String?
)

@Service
class CalendarService(
    private val reminders: ReminderRepository,
    private val userPlants: UserPlantRepository,
    private val plants: PlantRepository
) {
    fun day(userId: UUID, date: LocalDate, page: Int, size: Int, sort: String?): PageResponse<CalendarEntry> {
        val start = date.atStartOfDay().atOffset(ZoneOffset.UTC)
        val end = start.plusDays(1)
        return rangePage(userId, start, end, page, size, sort)
    }

    fun week(userId: UUID, startDate: LocalDate, page: Int, size: Int, sort: String?): PageResponse<CalendarEntry> {
        val start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC)
        val end = start.plusDays(7)
        return rangePage(userId, start, end, page, size, sort)
    }

    fun month(userId: UUID, year: Int, month: Int, page: Int, size: Int, sort: String?): PageResponse<CalendarEntry> {
        val start = LocalDate.of(year, month, 1).atStartOfDay().atOffset(ZoneOffset.UTC)
        val end = start.plusMonths(1)
        return rangePage(userId, start, end, page, size, sort)
    }

    private fun rangePage(userId: UUID, start: OffsetDateTime, end: OffsetDateTime, page: Int, size: Int, sort: String?): PageResponse<CalendarEntry> {
        val direction = if (sort?.startsWith("-") == true) Sort.Direction.DESC else Sort.Direction.ASC
        val sortProp = (sort?.trim()?.trimStart('-')?.takeIf { it.isNotBlank() }) ?: "dueAt"
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), direction, sortProp)
        val pageRem = reminders.findAllByUserIdAndDueAtBetween(userId, start, end, pageable)
        val pageCal = pageRem.map { it.toCalendarEntry() }
        return pageCal.toPageResponse()
    }

    private fun Reminder.toCalendarEntry(): CalendarEntry {
        val up = userPlants.findById(userPlantId).orElse(null)
        val plant = up?.plantId?.let { plants.findById(it).orElse(null) }
        return CalendarEntry(
            reminderId = id.toString(),
            userPlantId = userPlantId.toString(),
            kind = kind,
            dueAt = dueAt.toString(),
            nickname = up?.nickname,
            plantLatinName = plant?.latinName,
            plantCommonName = plant?.commonName
        )
    }
}
