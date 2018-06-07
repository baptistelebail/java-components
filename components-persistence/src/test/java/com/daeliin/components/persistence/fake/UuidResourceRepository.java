package com.daeliin.components.persistence.fake;

import com.daeliin.components.persistence.resource.repository.ResourceRepository;
import com.daeliin.components.persistence.sql.BUuidResource;
import com.daeliin.components.persistence.sql.QUuidResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class UuidResourceRepository extends ResourceRepository<BUuidResource, String> {

    public UuidResourceRepository() {
        super(QUuidResource.uuidResource, QUuidResource.uuidResource.uuid, BUuidResource::getUuid);
    }
}