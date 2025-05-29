package com.mindostech.quiznova.data.remote.dto


data class CategoriesResponse(
    val trivia_categories: List<CategoryDto>
)

data class CategoryDto(
    val id: Int,
    val name: String
)