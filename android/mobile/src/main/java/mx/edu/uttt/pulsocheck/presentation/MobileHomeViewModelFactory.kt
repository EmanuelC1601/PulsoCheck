package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/** Construye MobileHomeViewModel con el repositorio de Room. */
class MobileHomeViewModelFactory(
    private val repository: HeartRateReadingRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MobileHomeViewModel::class.java)) {
            throw IllegalArgumentException("Tipo de ViewModel no compatible.")
        }

        @Suppress("UNCHECKED_CAST")
        return MobileHomeViewModel(repository) as T
    }
}
