package demo.server.paintball.service

import demo.server.paintball.data.Game

interface GameService {

    fun getGame(): Game?
}