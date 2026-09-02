/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Car App Service with Hilt injection.
 */
package com.frish.lyricsauto.shared

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyCarAppService : CarAppService() {

    @Inject lateinit var musicStateRepository: MusicStateRepository

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return MyCarAppSession(musicStateRepository)
    }
}
