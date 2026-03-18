package com.o7solutions.student_project_bakingo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class User(
    var name: String ?= null,
    var email: String?= null,
    var uid: String ?= null,
    var time: String ?= null
)

data class Category(
    var name: String ?= null,
    var categoryUrl: String ?= null,
    var categoryId: String ?= null,
    var time: String ?= null,
)


@Parcelize
data class Product(
    var name : String ?= null,
    var categoryId: String ?= null,
    var price: String ?= null,
    var time: String ?= null,
    var description: String ?= null,
    var sellCount: Int ?= 0,
    var images: ArrayList<String> ?= ArrayList()
): Parcelable


data class CarouselItem(val imageResId: Int)

data class CartData(
    var name: String? = null,
    var categoryId: String? = null,
    var price: String? = null,
    var time: String? = null,
    var description: String? = null,
    var sellCount: Int? = 0,
    var images: ArrayList<String>? = ArrayList(),
    var weight: String? = null,
    var message: String? = null
){
    // Firebase needs this empty constructor
    constructor() : this(null, null, null, null, null, null, null)
}

data class CartItemWrapper(
    val key: String,    // This holds the push() key from Firebase (e.g., "-N1234abcd...")
    val data: CartData  // This holds the actual object (Name, Price, Weight, etc.)
)

data class ProductWrapper(
    val key: String,
    val data: Product // Replace with your Product model name
)