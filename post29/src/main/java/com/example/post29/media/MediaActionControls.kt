package com.example.post29.media

enum class MediaActionControls(name: String) {
    PLAY("com.example.post29.ACTION_PLAY"),
    PAUSE("com.example.post29.ACTION_PAUSE");

    companion object {
        fun from(action: String?) = MediaActionControls
            .values()
            .find { it.name == action }
    }
}