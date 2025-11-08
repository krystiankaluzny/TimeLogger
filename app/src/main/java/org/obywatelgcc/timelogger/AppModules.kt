package org.obywatelgcc.timelogger

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessageBus
import org.obywatelgcc.timelogger.timer.model.CalendarRepository
import org.obywatelgcc.timelogger.timer.model.CalendarRepositoryImpl
import org.obywatelgcc.timelogger.timer.model.TestCalendarRepositoryImpl
import org.obywatelgcc.timelogger.timer.presentation.TimerViewModel

val appModule = module {
    singleOf(::DataStoreManager)
    singleOf(::SnackbarMessageBus)
    singleOf(::CalendarRepositoryImpl) { bind<CalendarRepository>() }
    viewModelOf(::TimerViewModel)
}

val testAppModule = module {
    singleOf(::DataStoreManager)
    singleOf(::SnackbarMessageBus)
    singleOf(::TestCalendarRepositoryImpl) { bind<CalendarRepository>() }
    viewModelOf(::TimerViewModel)
}