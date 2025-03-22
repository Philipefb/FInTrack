package com.devspace.fintrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.devspace.fintrack.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class CreateCategoryBottomSheet(
    private val onCreateClicked:
        (String) -> Unit
) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_category_button_sheet, container, false)
        val btnCreate = view.findViewById<Button>(R.id.btn_create_category)

        val tieCategoryName = view.findViewById<TextInputEditText>(R.id.et_category_name)

        btnCreate.setOnClickListener{
            val name = tieCategoryName.text.toString()
            onCreateClicked.invoke(name)
            dismiss()
        }

        return view
    }
}