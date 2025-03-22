package com.devspace.fintrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.devspace.fintrack.databinding.CuTaskBottomSheetBinding

class CreateTaskBottomSheet(
    private val task: TaskUiData? = null,
    private val categoryList: List<CategoryEntity>,
    private val onCreateClicked: (TaskUiData) -> Unit,
    private val onDeleteClicked: (TaskUiData) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: CuTaskBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategorySelectionAdapter
    private var selectedCategory: String = categoryList.firstOrNull()?.name ?: ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CuTaskBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupCategorySelection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        with(binding) {
            if (task != null) {
                tvTitle.text = "Editar despesa"
                btnCreateTask.text = "Atualizar"
                btnDelete.visibility = View.VISIBLE
                
                etTaskName.setText(task.name)
                etTaskPrice.setText(task.price.toString())
                selectedCategory = task.category
            }

            btnCreateTask.setOnClickListener {
                val name = etTaskName.text.toString()
                val price = etTaskPrice.text.toString().toFloatOrNull() ?: 0f

                val taskData = TaskUiData(
                    id = task?.id ?: 0,
                    name = name,
                    category = selectedCategory,
                    price = price
                )
                
                onCreateClicked(taskData)
                dismiss()
            }

            btnDelete.setOnClickListener {
                task?.let {
                    onDeleteClicked(it)
                    dismiss()
                }
            }
        }
    }

    private fun setupCategorySelection() {
        categoryAdapter = CategorySelectionAdapter { category ->
            selectedCategory = category.name
            updateCategorySelection(category.name)
        }

        binding.rvCategorySelection.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        val categories = categoryList.map { entity ->
            CategoryUiData(
                name = entity.name,
                isSelected = task?.category == entity.name,
                iconRes = entity.iconRes
            )
        }

        categoryAdapter.submitList(categories)
    }

    private fun updateCategorySelection(selectedName: String) {
        val updatedCategories = categoryAdapter.currentList.map { category ->
            category.copy(isSelected = category.name == selectedName)
        }
        categoryAdapter.submitList(updatedCategories)
    }
}