package com.o7solutions.student_project_bakingo

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


data class Product(
    var name : String ?= null,
    var categoryId: String ?= null,
    var price: String ?= null,
    var time: String ?= null,
    var description: String ?= null,
    var sellCount: Int ?= 0,
    var images: ArrayList<String> ?= ArrayList()
)

data class CarouselItem(val imageResId: Int)