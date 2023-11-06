package com.dadm.reto8

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Upsert
    suspend fun upsertCompany(company: Company)

    @Delete
    suspend fun deleteCompany(company: Company)

    @Query("SELECT * FROM company ORDER BY name ASC")
    suspend fun getCompanies(): List<Company>

    @Query("SELECT * FROM company WHERE name LIKE '%' || :nameFilter || '%' AND classification = :classificationFilter ORDER BY name ASC")
    suspend fun getCompanies(nameFilter: String, classificationFilter: String): List<Company>

    @Query("SELECT * FROM company WHERE name LIKE '%' || :nameFilter || '%' ORDER BY name ASC")
    suspend fun getCompanies(nameFilter: String): List<Company>

    @Query("SELECT * FROM company WHERE classification = :classificationFilter ORDER BY name ASC")
    suspend fun getCompaniesByClassification(classificationFilter: String): List<Company>
}