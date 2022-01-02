package fhnw.emoba.thatsapp.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun LocalDateTime.toTimeString(): String =
    DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN).format(this)

fun LocalDateTime.toDateString(): String =
    DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN).format(this)

fun LocalDateTime.toJSONDateString(): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMAN).format(this)

fun String.dateFromJSON(): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMAN))