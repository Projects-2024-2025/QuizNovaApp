package com.technovix.quiznova.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<CategoryEntity>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<CategoryEntity>>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        repository.getCategories()
            .onEach { result ->
                _categories.value = result
            }
            .launchIn(viewModelScope)
    }

    fun refreshCategories() {
        _categories.value = Resource.Loading()
        loadCategories()
    }
}