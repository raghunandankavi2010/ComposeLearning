package com.example.composelearning.pathmorph.domain.usecase

import com.example.composelearning.pathmorph.domain.model.PhoneShape
import com.example.composelearning.pathmorph.domain.repository.PhoneShapeRepository

class GetPhoneShapesUseCase(private val repository: PhoneShapeRepository) {
    suspend operator fun invoke(): List<PhoneShape> = repository.getShapes()
}
