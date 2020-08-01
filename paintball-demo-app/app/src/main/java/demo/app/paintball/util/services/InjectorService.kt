package demo.app.paintball.util.services

import dagger.Component
import demo.app.paintball.data.mqtt.MqttServiceImpl
import demo.app.paintball.data.rest.RestServiceImpl
import javax.inject.Singleton

@Singleton
@Component
interface InjectorService {

    fun rest(): RestServiceImpl

    fun mqtt(): MqttServiceImpl

    fun player(): PlayerService
}