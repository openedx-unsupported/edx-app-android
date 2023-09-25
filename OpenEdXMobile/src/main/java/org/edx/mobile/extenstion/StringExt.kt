package org.edx.mobile.extenstion

fun String?.isNotNullOrEmpty():Boolean = this.isNullOrEmpty().not()
