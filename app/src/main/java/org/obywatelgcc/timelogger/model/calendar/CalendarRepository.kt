package org.obywatelgcc.timelogger.model.calendar

interface CalendarRepository {

    fun findAll(): List<Calendar>
}

class CalendarRepositoryImpl : CalendarRepository {

    override fun findAll(): List<Calendar> {
        return listOf(
            Calendar(1, "obywatel", "obywatel", "obywatel"),
            Calendar(2, "test", "obywatel", "obywatel")
        )
    }
}