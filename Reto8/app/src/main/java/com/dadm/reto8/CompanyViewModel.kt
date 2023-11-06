package com.dadm.reto8

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompanyViewModel (
    private val dao: CompanyDao
): ViewModel(){

    val _state = MutableStateFlow(CompanyState())

    fun onEvent(event: CompanyEvent){
        when(event) {
            is CompanyEvent.DeleteCompany -> {
                viewModelScope.launch {
                    dao.deleteCompany(event.company)
                }
            }

            CompanyEvent.SaveCompany -> {
                val id = _state.value.id
                val name = _state.value.name
                val url = _state.value.url
                val phone = _state.value.phone
                val email = _state.value.email
                val products = _state.value.products
                val classification = _state.value.classification

                if (name.isBlank() || url.isBlank() || phone.isBlank() || email.isBlank() || products.isBlank() || classification.isBlank()) {
                    return
                }

                val company = Company(
                    name = name,
                    url = url,
                    phone = phone,
                    email = email,
                    products = products,
                    classification = classification
                )

                if(id > 0) {
                    company.id = id
                }

                viewModelScope.launch {
                    dao.upsertCompany(company)
                }
                _state.update {
                    it.copy(
                        id = 0,
                        name = "",
                        url = "",
                        phone = "",
                        email = "",
                        products = "",
                        classification = "",
                        isEditingCompany = false
                    )
                }
            }

            is CompanyEvent.SetClassification -> {
                _state.update {
                    it.copy(
                        classification = event.classification
                    )
                }
            }

            is CompanyEvent.SetEmail -> {
                _state.update {
                    it.copy(
                        email = event.email
                    )
                }
            }

            is CompanyEvent.SetName -> {
                _state.update {
                    it.copy(
                        name = event.name
                    )
                }
            }

            is CompanyEvent.SetPhone -> {
                _state.update {
                    it.copy(
                        phone = event.phone
                    )
                }
            }

            is CompanyEvent.SetProducts -> {
                _state.update {
                    it.copy(
                        products = event.products
                    )
                }
            }

            is CompanyEvent.SetURL -> {
                _state.update {
                    it.copy(
                        url = event.url
                    )
                }
            }

            CompanyEvent.HideForm -> {
                _state.update {
                    it.copy(
                        isAddingCompany = false
                    )
                }
            }

            CompanyEvent.ShowForm -> {
                _state.update {
                    it.copy(
                        isAddingCompany = true
                    )
                }
            }

            is CompanyEvent.GetCompanies -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            companies = if (_state.value.nameFilter.isEmpty() && _state.value.classificationFilter.isEmpty()) dao.getCompanies() else if (_state.value.nameFilter.isNotEmpty() && _state.value.classificationFilter.isEmpty()) dao.getCompanies(_state.value.nameFilter) else if (_state.value.nameFilter.isEmpty() && _state.value.classificationFilter.isNotEmpty()) dao.getCompaniesByClassification(_state.value.classificationFilter) else dao.getCompanies(_state.value.nameFilter, _state.value.classificationFilter)
                        )
                    }
                }
            }

            CompanyEvent.HideFilter -> {
                _state.update {
                    it.copy(
                        isFilteringCompany = false
                    )
                }
            }

            CompanyEvent.ShowFilter -> {
                _state.update {
                    it.copy(
                        isFilteringCompany = true
                    )
                }
            }

            is CompanyEvent.SetId -> {
                _state.update {
                    it.copy(
                        id = event.id
                    )
                }
            }

            is CompanyEvent.SetNameFilter -> {
                _state.update {
                    it.copy(
                        nameFilter = event.nameFilter
                    )
                }
            }

            is CompanyEvent.SetClassificationFilter -> {
                _state.update {
                    it.copy(
                        classificationFilter = event.classificationFilter
                    )
                }
            }

            is CompanyEvent.SetForm -> {
                _state.update {
                    it.copy(
                        id = event.company.id,
                        name = event.company.name,
                        url = event.company.url,
                        phone = event.company.phone,
                        email = event.company.email,
                        products = event.company.products,
                        classification = event.company.classification,
                        isEditingCompany = true
                    )
                }
            }

            CompanyEvent.ClearForm -> {
                _state.update {
                    it.copy(
                        id = 0,
                        name = "",
                        url = "",
                        phone = "",
                        email = "",
                        products = "",
                        classification = "",
                        isEditingCompany = false
                    )
                }
            }
        }
    }

}