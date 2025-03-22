package com.devspace.fintrack

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo("key")
    val name: String,

    @ColumnInfo("is_selected")
    val isSelected: Boolean,

    @ColumnInfo("icon_res")
    val iconRes: Int = R.drawable.ic_category_default
)