package org.obywatelgcc.timelogger.core.data

import java.time.ZonedDateTime

fun maxOf(a: ZonedDateTime, b: ZonedDateTime) = if (a.isAfter(b)) a else b
fun minOf(a: ZonedDateTime, b: ZonedDateTime) = if (a.isBefore(b)) a else b