package com.example.localguide_planner.data.local.db.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory

object PlaceMapper {

    fun toEntity(domain: Place): PlaceEntity = PlaceEntity(
        id = domain.id,
        name = domain.name,
        description = domain.description,
        category = domain.category.name,
        address = domain.address,
        latitude = domain.latitude,
        longitude = domain.longitude,
        isFavorite = domain.isFavorite,
        createdAt = domain.createdAt
    )

    fun toDomain(entity: PlaceEntity): Place = Place(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        category = PlaceCategory.valueOf(entity.category),
        address = entity.address,
        latitude = entity.latitude,
        longitude = entity.longitude,
        isFavorite = entity.isFavorite,
        createdAt = entity.createdAt
    )
}
