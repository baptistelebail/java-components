package com.blebail.components.webservices.fake;

import com.blebail.components.webservices.dto.ResourceDtoConversion;

import java.time.Instant;
import java.util.UUID;

public final class UuidResourceDtoConversion implements ResourceDtoConversion<UuidResourceDto, UuidResource, String> {

    @Override
    public UuidResourceDto from(UuidResource uuidEntity) {
        if (uuidEntity == null) {
            return null;
        }

        return new UuidResourceDto(
                uuidEntity.id(),
                uuidEntity.creationDate(),
                uuidEntity.label);
    }

    @Override
    public UuidResource to(UuidResourceDto uuidEntityDto, String id, Instant localDateTime) {
        if (uuidEntityDto == null) {
            return null;
        }

        return new UuidResource(
                id != null ? id: UUID.randomUUID().toString(),
                localDateTime,
                uuidEntityDto.label);
    }
}
