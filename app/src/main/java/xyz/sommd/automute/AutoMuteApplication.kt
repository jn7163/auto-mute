/*
 * Copyright (C) 2018 David Sommerich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.sommd.automute

import android.app.Application
import xyz.sommd.automute.di.AutoMuteComponent
import xyz.sommd.automute.di.ContextModule
import xyz.sommd.automute.di.DaggerAutoMuteComponent

class AutoMuteApplication: Application() {
    companion object {
        lateinit var component: AutoMuteComponent
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        component = DaggerAutoMuteComponent.builder()
                .contextModule(ContextModule(this))
                .build()
    }
}