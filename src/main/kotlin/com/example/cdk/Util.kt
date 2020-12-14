package com.example.cdk

import software.amazon.jsii.Builder

fun <B: Builder<T>, T> B.build(init: B.() -> Unit): T {
    init(this)
    return this.build()
}
