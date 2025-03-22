package com.devspace.fintrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories: List<CategoryUiData> = listOf()
    private var categoriesEntity: List<CategoryEntity> = listOf()
    private var tasks: List<TaskUiData> = listOf()
    
    private val categoryAdapter: CategorySelectionAdapter by lazy {
        CategorySelectionAdapter { selected ->
            if (selected.name == "+") {
                createCategoryBottomSheet()
            } else {
                val categoryTemp = categories.map { item ->
                    item.copy(isSelected = item.name == selected.name)
                }

                if (selected.name != "ALL") {
                    filterTaskByCategory(selected.name)
                } else {
                    getTasksfromDatabase(taskAdapter)
                }

                categoryAdapter.submitList(categoryTemp)
            }
        }
    }
    
    private val taskAdapter: TaskListAdapter = TaskListAdapter()
    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var price_total: TextView
    private lateinit var btnAddExpense: Button

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDataBase::class.java, "database-task-beat"
        ).build()
    }

    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao: TaskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvCategory = findViewById(R.id.rv_categories)
        ctnEmptyView = findViewById(R.id.ll_empty_view)
        btnAddExpense = findViewById(R.id.btn_add_expense)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        val btnCreateEmpty = findViewById<Button>(R.id.create_empty)

        btnCreateEmpty.setOnClickListener {
            createCategoryBottomSheet()
        }

        btnAddExpense.setOnClickListener {
            showCreateUpdateTaskBottomSheet()
        }

        // Configurar long click para deletar categoria
        categoryAdapter.setOnLongClickListener { selected ->
            if (selected.name != "+" && selected.name != "ALL") {
                val title = getString(R.string.info_title)
                val description = getString(R.string.category_delete_description)
                val btntext = getString(R.string.delete)

                showInfoDialog(title, description, btntext) {
                    val category = CategoryEntity(
                        name = selected.name,
                        isSelected = selected.isSelected,
                        iconRes = selected.iconRes
                    )
                    deleteCategory(category)
                }
            }
        }

        rvCategory.adapter = categoryAdapter
        getCategoriesfromDatabase()

        rvTask.adapter = taskAdapter
        taskAdapter.setOnClickListener { task ->
            showCreateUpdateTaskBottomSheet(task)
        }
        getTasksfromDatabase(taskAdapter)
        getPrices()

        updateViewVisibility()
    }

    /////////////////////////////////////////////////////////////////////////////////

    private fun showInfoDialog(
        title: String,
        description: String,
        btntext: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btntext,
            onClick
        )
        infoBottomSheet.show(supportFragmentManager, "infobottomsheet")
    }

    private fun filterTaskByCategory(category: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksdb = taskDao.getAllByCategory(category)
            val tasksUiData = tasksdb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category,
                    price = it.price
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                taskAdapter.submitList(tasksUiData)
            }
        }
    }

    fun getCategoriesfromDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            categoriesEntity = categoryDao.getAll()

            val categoriesUiData = categoriesEntity.map {
                CategoryUiData(
                    name = it.name,
                    isSelected = it.isSelected,
                    iconRes = it.iconRes
                )
            }.toMutableList()
            
            // Adiciona o ALL no início
            categoriesUiData.add(0, CategoryUiData(
                name = "ALL",
                isSelected = true,
                iconRes = R.drawable.ic_grid
            ))
            
            // Adiciona o botão + no final
            categoriesUiData.add(CategoryUiData(
                name = "+",
                isSelected = false,
                iconRes = R.drawable.ic_add
            ))
            
            GlobalScope.launch(Dispatchers.Main) {
                categories = categoriesUiData
                categoryAdapter.submitList(categories)
                updateViewVisibility()
            }
        }
    }

    private fun getTasksfromDatabase(adapter: TaskListAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksdb = taskDao.getAll()
            val tasksUiData = tasksdb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category,
                    price = it.price
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                getPrices()
                tasks = tasksUiData
                adapter.submitList(tasksUiData)
            }
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesfromDatabase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategory(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesfromDatabase()
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun insertTask(tasKEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(tasKEntity)
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun deleteTask(tasKEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(tasKEntity)
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createTaskBottomSheet = CreateTaskBottomSheet(
            task = taskUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { tasktobeCreatedorUpdated ->
                insertTask(
                    TaskEntity(
                        id = tasktobeCreatedorUpdated.id,
                        name = tasktobeCreatedorUpdated.name,
                        category = tasktobeCreatedorUpdated.category,
                        price = tasktobeCreatedorUpdated.price
                    )
                )
            },
            onDeleteClicked = { tasktobeDeleted ->
                deleteTask(
                    TaskEntity(
                        id = tasktobeDeleted.id,
                        name = tasktobeDeleted.name,
                        category = tasktobeDeleted.category,
                        price = tasktobeDeleted.price
                    )
                )
            }
        )
        createTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")
    }

    private fun createCategoryBottomSheet() {
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                isSelected = false,
                iconRes = getIconForCategory(categoryName)
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }

    private fun updateViewVisibility() {
        if (categories.isEmpty()) {
            rvCategory.isVisible = false
            ctnEmptyView.isVisible = true
            btnAddExpense.isVisible = false
        } else {
            rvCategory.isVisible = true
            ctnEmptyView.isVisible = false
            btnAddExpense.isVisible = true
        }
    }

    private fun getPrices() {
        GlobalScope.launch(Dispatchers.IO) {
            price_total = findViewById(R.id.price_total)
            val priceTotal = taskDao.getAllTaskPrice()
            GlobalScope.launch(Dispatchers.Main) {
                price_total.text = "R$ %.2f".format(priceTotal)
            }
        }
    }

    private fun getIconForCategory(categoryName: String): Int {
        return when (categoryName.lowercase()) {
            "alimentação", "comida", "food", "refeição" -> R.drawable.ic_food
            "transporte", "transport", "carro", "ônibus" -> R.drawable.ic_transport
            "casa", "moradia", "home", "aluguel" -> R.drawable.ic_home
            "lazer", "entretenimento", "diversão" -> R.drawable.ic_entertainment
            "saúde", "farmácia", "médico" -> R.drawable.ic_health
            "educação", "escola", "estudos" -> R.drawable.ic_education
            "compras", "shopping" -> R.drawable.ic_shopping
            "contas", "pagamentos" -> R.drawable.ic_bills
            else -> R.drawable.ic_category_default
        }
    }
}
