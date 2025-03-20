package com.wp.coroutinesxrequest

import android.widget.Toast

fun Any.toast(msg: String) {
    Toast.makeText(AppModule.provideApp(), msg, Toast.LENGTH_SHORT).show()
}