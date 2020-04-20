package com.example.vkbot

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val groupId = 142409596
        val groupAccessToken = "7c9d3416ef28440d381573cf5165c911827e75a426ca351463759eba4cf95fba6652102ad84138f65865d"

        Bot().start(groupId, groupAccessToken)
    }
}