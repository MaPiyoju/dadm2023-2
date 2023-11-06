package com.dadm.reto8

sealed interface CompanyEvent {
    object SaveCompany: CompanyEvent
    data class SetId(val id: Int):CompanyEvent
    data class SetName(val name: String):CompanyEvent
    data class SetURL(val url: String):CompanyEvent
    data class SetPhone(val phone: String):CompanyEvent
    data class SetEmail(val email: String):CompanyEvent
    data class SetProducts(val products: String):CompanyEvent
    data class SetClassification(val classification: String):CompanyEvent

    data class SetNameFilter(val nameFilter: String):CompanyEvent
    data class SetClassificationFilter(val classificationFilter: String):CompanyEvent

    object ShowForm: CompanyEvent
    object HideForm: CompanyEvent

    object ShowFilter: CompanyEvent
    object HideFilter: CompanyEvent

    data class GetCompanies(val nameFilter: String, val classificationFilter: String): CompanyEvent

    data class DeleteCompany(val company: Company): CompanyEvent

    data class SetForm(val company: Company):CompanyEvent
    object ClearForm: CompanyEvent
}