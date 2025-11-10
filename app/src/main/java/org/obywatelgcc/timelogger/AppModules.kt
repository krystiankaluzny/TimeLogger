package org.obywatelgcc.timelogger

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessageBus
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.core.model.CalendarRepositoryImpl
import org.obywatelgcc.timelogger.core.model.TestCalendarRepositoryImpl
import org.obywatelgcc.timelogger.statistics.StatisticsViewModel
import org.obywatelgcc.timelogger.timer.presentation.TimerViewModel

val appModule = module {
    singleOf(::DataStoreManager)
    singleOf(::SnackbarMessageBus)
    singleOf(::CalendarRepositoryImpl) { bind<CalendarRepository>() }
    singleOf(::TimerViewModel)
    singleOf(::StatisticsViewModel)
}

val testAppModule = module {
    singleOf(::DataStoreManager)
    singleOf(::SnackbarMessageBus)
    singleOf(::TestCalendarRepositoryImpl) { bind<CalendarRepository>() }
    singleOf(::TimerViewModel)
    singleOf(::StatisticsViewModel)
}