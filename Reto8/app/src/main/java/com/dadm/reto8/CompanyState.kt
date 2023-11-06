package com.dadm.reto8

data class CompanyState(
    val companies: List<Company> = emptyList(),
    val id: Int = 0,
    val name: String = "",
    val url: String = "",
    val phone: String = "",
    val email: String = "",
    val products: String = "",
    val classification: String = "",
    val isAddingCompany: Boolean = false,
    val isEditingCompany: Boolean = false,
    val isFilteringCompany: Boolean = false,
    val nameFilter: String = "",
    val classificationFilter: String = ""
)
