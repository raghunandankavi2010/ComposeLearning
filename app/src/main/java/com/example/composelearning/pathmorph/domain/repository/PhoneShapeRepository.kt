package com.example.composelearning.pathmorph.domain.repository

import com.example.composelearning.pathmorph.domain.model.PhoneShape

interface PhoneShapeRepository {
    suspend fun getShapes(): List<PhoneShape>
}
