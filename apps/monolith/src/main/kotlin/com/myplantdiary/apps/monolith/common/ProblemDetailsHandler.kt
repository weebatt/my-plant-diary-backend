package com.myplantdiary.apps.monolith.common

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.springframework.dao.DataIntegrityViolationException
import org.hibernate.exception.ConstraintViolationException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.servlet.resource.NoResourceFoundException
import jakarta.validation.ConstraintViolationException as JakartaConstraintViolationException

data class ProblemDetails(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String?,
    val instance: String? = null,
    val timestamp: String = OffsetDateTime.now(ZoneOffset.UTC).toString(),
    val errors: Map<String, String>? = null
)

@ControllerAdvice
class ProblemDetailsHandler {
    private val log = LoggerFactory.getLogger(ProblemDetailsHandler::class.java)
    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException) = problem(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(ConflictException::class)
    fun conflict(ex: ConflictException) = problem(HttpStatus.CONFLICT, ex.message)

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(ex: BadRequestException) = problem(HttpStatus.BAD_REQUEST, ex.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArg(ex: IllegalArgumentException) = problem(HttpStatus.BAD_REQUEST, ex.message)

    @ExceptionHandler(NoResourceFoundException::class)
    fun notFoundResource(ex: NoResourceFoundException) = problem(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetails> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        log.debug("Validation error: {} -> {}", ex.message, errors)
        return problem(HttpStatus.BAD_REQUEST, "Ошибка валидации", errors = errors)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun dataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ProblemDetails> {
        log.warn("Data integrity violation", ex)
        val root = ex.cause
        // Hibernate wraps driver exceptions into ConstraintViolationException
        if (root is ConstraintViolationException) {
            val sqlState = root.sqlException?.sqlState
            // 23505 = unique_violation in PostgreSQL
            if (sqlState == "23505") {
                return problem(HttpStatus.CONFLICT, "Конфликт уникальности (возможно, email уже зарегистрирован)")
            }
        }
        return problem(HttpStatus.BAD_REQUEST, "Ошибка целостности данных")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun badJson(ex: HttpMessageNotReadableException): ResponseEntity<ProblemDetails> {
        log.warn("JSON parse error", ex)
        return problem(HttpStatus.BAD_REQUEST, "Некорректное тело запроса")
    }

    @ExceptionHandler(JakartaConstraintViolationException::class)
    fun beanValidation(ex: JakartaConstraintViolationException): ResponseEntity<ProblemDetails> {
        log.debug("Bean validation failed", ex)
        return problem(HttpStatus.BAD_REQUEST, ex.message)
    }

    private fun problem(status: HttpStatus, detail: String?, errors: Map<String,String>? = null): ResponseEntity<ProblemDetails> =
        ResponseEntity.status(status).body(
            ProblemDetails(
                title = status.reasonPhrase,
                status = status.value(),
                detail = detail,
                errors = errors
            )
        )

    @ExceptionHandler(Exception::class)
    fun fallback(ex: Exception): ResponseEntity<ProblemDetails> {
        log.error("Unhandled exception", ex)
        val detail = "${ex.javaClass.simpleName}: ${ex.message}"
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, detail)
    }
}
