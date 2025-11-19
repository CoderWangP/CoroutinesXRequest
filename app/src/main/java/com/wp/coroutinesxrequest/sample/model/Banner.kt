package com.wp.coroutinesxrequest.sample.model

import androidx.annotation.Keep

@Keep
data class Banner(
    val desc: String = "",
    val id: String = "",
    val imagePath: String = "",
    val isVisible: String = "",
    val order: String = "",
    val title: String = "",
    val type: String = "",
    val url: String = ""
)