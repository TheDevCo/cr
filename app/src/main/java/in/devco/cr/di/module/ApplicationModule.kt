package `in`.devco.cr.di.module

import dagger.Module

@Module(includes = [ViewModelModule::class, NetworkModule::class])
class ApplicationModule