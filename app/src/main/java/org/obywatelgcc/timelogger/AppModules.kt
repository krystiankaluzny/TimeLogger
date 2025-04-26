package org.obywatelgcc.timelogger

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.obywatelgcc.timelogger.timer.model.calendar.CalendarRepository
import org.obywatelgcc.timelogger.timer.model.calendar.CalendarRepositoryImpl
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.timer.model.calendar.TestCalendarRepositoryImpl
import org.obywatelgcc.timelogger.timer.presentation.TimeEventViewModel

val appModule = module {
    singleOf(::DataStoreManager)
    singleOf(::CalendarRepositoryImpl) { bind<CalendarRepository>() }
    viewModelOf(::TimeEventViewModel)
}

val testAppModule = module {
    singleOf(::DataStoreManager)
    singleOf(::TestCalendarRepositoryImpl) { bind<CalendarRepository>() }
    viewModelOf(::TimeEventViewModel)
}