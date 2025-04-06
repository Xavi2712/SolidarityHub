package com.example.solidarityhub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform