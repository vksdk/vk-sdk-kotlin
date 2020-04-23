package com.example.vkbot

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val groupId = 151083290
        val groupAccessToken = "abcdef123456..."

        Bot().start(groupId, groupAccessToken)
    }
}