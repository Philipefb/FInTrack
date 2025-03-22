package com.devspace.fintrack

data class CategoryUiData(
    val name: String,
    val isSelected: Boolean,
    val iconRes: Int = R.drawable.ic_category_default
)
