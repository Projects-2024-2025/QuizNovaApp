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
import kotlinx.coroutines.launch
import javax.inject.Inject

// Senior Dev: HiltViewModel, Hilt'in ViewModel'e bağımlılıkları (örn: Repository)
// otomatik olarak enjekte etmesini sağlar.
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    // Senior Dev: UI state'ini tutmak için StateFlow kullanıyoruz. Compose UI,
    // bu StateFlow'u observe ederek state değiştikçe otomatik olarak güncellenir.
    // _categories private ve Mutable, categories public ve immutable (StateFlow).
    // Bu, state'in sadece ViewModel içinden değiştirilebilmesini sağlar (Unidirectional Data Flow).
    private val _categories = MutableStateFlow<Resource<List<CategoryEntity>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<CategoryEntity>>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    // Senior Dev: Kategorileri yüklemek için Repository'yi çağırıyoruz.
    // viewModelScope, ViewModel yaşam döngüsüne bağlı bir CoroutineScope sağlar.
    // ViewModel destroy olduğunda bu scope'taki coroutine'ler otomatik iptal olur.
    private fun loadCategories() {
        // Repository'den gelen Flow'u dinliyoruz. onEach ile her yeni veri geldiğinde
        // _categories StateFlow'unu güncelliyoruz. launchIn ile Flow'u viewModelScope'ta başlatıyoruz.
        repository.getCategories()
            .onEach { result ->
                _categories.value = result
            }
            .launchIn(viewModelScope) // Flow'u viewModelScope'ta başlat ve dinlemeye devam et
    }

    // Yenileme butonu vb. için public fonksiyon
    fun refreshCategories() {
        // Eski flow'u iptal etmeye gerek yok, repository.getCategories() çağrısı
        // zaten yeni bir flow başlatacak ve onEach onu dinleyecek.
        // Ancak state'i tekrar Loading yapmak iyi olabilir:
        _categories.value = Resource.Loading()
        loadCategories() // Tekrar yüklemeyi tetikle
    }
}