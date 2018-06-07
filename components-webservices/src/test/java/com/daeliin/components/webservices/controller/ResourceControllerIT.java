package com.daeliin.components.webservices.controller;

import com.daeliin.components.test.rule.DbFixture;
import com.daeliin.components.test.rule.DbMemory;
import com.daeliin.components.webservices.fake.UuidResourceDto;
import com.daeliin.components.webservices.fake.UuidResourceDtoConversion;
import com.daeliin.components.webservices.fake.UuidResourceService;
import com.daeliin.components.webservices.fixtures.JavaFixtures;
import com.daeliin.components.webservices.library.UuidResourceDtoLibrary;
import com.daeliin.components.webservices.sql.QUuidResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ResourceControllerIT {

    @Inject
    private ObjectMapper jsonMapper;

    @Inject
    private UuidResourceService service;

    @Inject
    private MockMvc mockMvc;

    private UuidResourceDtoConversion conversion = new UuidResourceDtoConversion();

    @ClassRule
    public static DbMemory dbMemory = new DbMemory();

    @Rule
    public DbFixture dbFixture = new DbFixture(dbMemory, JavaFixtures.uuidResources());

    @Test
    public void shouldReturnHttpCreatedAndCreatedResource() throws Exception {
        UuidResourceDto uuidPersistentResourceDto = new UuidResourceDto("id", Instant.now(), "label");

        MvcResult result =
            mockMvc
                .perform(post("/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(uuidPersistentResourceDto)))
                .andExpect(status().isCreated())
                .andReturn();

        UuidResourceDto createdUuidPersistentResourceDto = jsonMapper.readValue(result.getResponse().getContentAsString(), UuidResourceDto.class);

        assertThat(createdUuidPersistentResourceDto.id).isNotBlank();
        assertThat(createdUuidPersistentResourceDto.creationDate).isNotNull();
        assertThat(createdUuidPersistentResourceDto.label).isEqualTo(uuidPersistentResourceDto.label);
    }

    @Test
    public void shouldPersistResource() throws Exception {
        UuidResourceDto uuidPersistentResourceDto = new UuidResourceDto("id", Instant.now(), "label");
        int uuidPersistentResourceCountBeforeCreate = countRows();

         UuidResourceDto returnedUuidPersistentResourceDto = jsonMapper.readValue(mockMvc
                .perform(post("/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(uuidPersistentResourceDto)))
                .andReturn()
                .getResponse()
                .getContentAsString(), UuidResourceDto.class);

        int uuidPersistentResourceCountAfterCreate = countRows();

        UuidResourceDto persistedUuidPersistentResourceDto = conversion.instantiate(service.findOne(returnedUuidPersistentResourceDto.id));

        assertThat(uuidPersistentResourceDto.id).isNotBlank();
        assertThat(uuidPersistentResourceDto.creationDate).isNotNull();
        assertThat(persistedUuidPersistentResourceDto.label).isEqualTo(uuidPersistentResourceDto.label);
        assertThat(uuidPersistentResourceCountAfterCreate).isEqualTo(uuidPersistentResourceCountBeforeCreate + 1);
    }

    @Test
    public void shouldReturnHttpBadRequest_whenCreatingInvalidResource() throws Exception {
        dbFixture.noRollback();

        UuidResourceDto invalidUuidPersistentResourceDto = new UuidResourceDto("id", Instant.now(), " ");

        mockMvc
            .perform(post("/uuid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(invalidUuidPersistentResourceDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotPersistResource_whenCreatingInvalidResource() throws Exception {
        dbFixture.noRollback();

        int uuidPersistentResourceCountBeforeCreate = countRows();

        UuidResourceDto invalidUuidPersistentResourceDto = new UuidResourceDto("id", Instant.now(), " ");

        mockMvc
            .perform(post("/uuid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(invalidUuidPersistentResourceDto)));

        int uuidPersistentResourceCountAfterCreate = countRows();

        assertThat(service.exists(invalidUuidPersistentResourceDto.id)).isFalse();
        assertThat(uuidPersistentResourceCountAfterCreate).isEqualTo(uuidPersistentResourceCountBeforeCreate);
    }

    @Test
    public void shouldReturnHttpOkAndResource_whenResourceExists() throws Exception {
        dbFixture.noRollback();

        UuidResourceDto existingUuidPersistentResourceDto = UuidResourceDtoLibrary.uuidResourceDto1();

        MvcResult result =
            mockMvc
                .perform(get("/uuid/" + existingUuidPersistentResourceDto.id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UuidResourceDto retrievedUuidPersistentResourceDto = jsonMapper.readValue(result.getResponse().getContentAsString(), UuidResourceDto.class);
        assertThat(retrievedUuidPersistentResourceDto).isEqualToComparingFieldByField(existingUuidPersistentResourceDto);
    }

    @Test
    public void shouldReturnHttpNotFound_whenResourceDoesntExist() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid/nonExistingId")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnHttpOkAndPage0WithSize20SortedByIdAsc_byDefault() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(4)))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.totalItems").value(4))
            .andExpect(jsonPath("$.nbItems").value(4));
    }

    @Test
    public void shouldReturnHttpOkAndPage1WithSize2SortedByLabelDesc() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid?page=1&size=2&direction=DESC&properties=label")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.totalItems").value(4))
            .andExpect(jsonPath("$.nbItems").value(2))
            .andExpect(jsonPath("$.items[0].label").value(UuidResourceDtoLibrary.uuidResourceDto2().label))
            .andExpect(jsonPath("$.items[1].label").value(UuidResourceDtoLibrary.uuidResourceDto1().label));
    }

    @Test
    public void shouldReturnPageSortedByLabelDescThenByIdDesc() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid?direction=DESC&properties=label,id")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].label").value(UuidResourceDtoLibrary.uuidResourceDto4().label))
                .andExpect(jsonPath("$.items[1].label").value(UuidResourceDtoLibrary.uuidResourceDto3().label))
                .andExpect(jsonPath("$.items[2].label").value(UuidResourceDtoLibrary.uuidResourceDto2().label))
                .andExpect(jsonPath("$.items[3].label").value(UuidResourceDtoLibrary.uuidResourceDto1().label));
    }

    @Test
    public void shouldReturnHttpBadRequest_whenPageIsNotValid() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid?page=-1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        mockMvc
            .perform(get("/uuid?page=invalidPageNumber")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnHttpBadRequest_whenPageSizeIsNotValid() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid?size=-1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        mockMvc
            .perform(get("/uuid?size=invalidPageSize")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnHttpBadRequest_whenPageDirectionIsNotValid() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(get("/uuid?direction=invalidDirection")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnHttpOkAndUpdatedResource_whenUpdatingResource() throws Exception {
        UuidResourceDto updatedUuidPersistentResourceDto = new UuidResourceDto(
                UuidResourceDtoLibrary.uuidResourceDto1().id,
                Instant.now(),
                "newLabel");

        MvcResult result =
            mockMvc
                .perform(put("/uuid/" + updatedUuidPersistentResourceDto.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updatedUuidPersistentResourceDto)))
                .andExpect(status().isOk())
                .andReturn();

        UuidResourceDto retrievedUuidPersistentResourceDto = jsonMapper.readValue(result.getResponse().getContentAsString(), UuidResourceDto.class);
        assertThat(retrievedUuidPersistentResourceDto.label).isEqualTo(updatedUuidPersistentResourceDto.label);
    }

    @Test
    public void shouldUpdateResource() throws Exception {
        UuidResourceDto updatedUuidPersistentResourceDto = new UuidResourceDto(
                UuidResourceDtoLibrary.uuidResourceDto1().id,
                Instant.now(),
                "newLabel");

        mockMvc
            .perform(put("/uuid/" + updatedUuidPersistentResourceDto.id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(updatedUuidPersistentResourceDto)));

        UuidResourceDto retrievedUuidPersistentResourceDto = conversion.instantiate(service.findOne(updatedUuidPersistentResourceDto.id));

        assertThat(retrievedUuidPersistentResourceDto.label).isEqualTo(updatedUuidPersistentResourceDto.label);
    }

    @Test
    public void shouldReturnHttpBadRequest_whenUpdatingInvalidResource() throws Exception {
        dbFixture.noRollback();

        UuidResourceDto invalidUuidPersistentResourceDto = new UuidResourceDto(
                UuidResourceDtoLibrary.uuidResourceDto1().id,
                Instant.now(),
                " ");

        mockMvc
            .perform(put("/uuid/" + invalidUuidPersistentResourceDto.id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(invalidUuidPersistentResourceDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotUpdateResource_whenUpdatingInvalidResource() throws Exception {
        dbFixture.noRollback();

        UuidResourceDto invalidUuidPersistentResourceDto = new UuidResourceDto(
                UuidResourceDtoLibrary.uuidResourceDto1().id,
                Instant.now(),
                " ");

        mockMvc
            .perform(put("/uuid/" + invalidUuidPersistentResourceDto.id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(invalidUuidPersistentResourceDto)));

        UuidResourceDto retrievedUuidPersistenceResourceDto = conversion.instantiate(service.findOne(invalidUuidPersistentResourceDto.id));

        assertThat(retrievedUuidPersistenceResourceDto).isEqualToComparingFieldByFieldRecursively(UuidResourceDtoLibrary.uuidResourceDto1());
    }

    @Test
    public void shouldReturnHttpNotFound_whenUpdatingNonExistingResource() throws Exception {
        dbFixture.noRollback();

        UuidResourceDto updatedUuidPersistentResourceDto = new UuidResourceDto(
                UuidResourceDtoLibrary.uuidResourceDto1().id,
                Instant.now(),
                "newLabel");

        mockMvc
            .perform(put("/uuid/nonExistingId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(updatedUuidPersistentResourceDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnHttpNoContent_whenDeletingAResource() throws Exception {
        mockMvc
                .perform(delete("/uuid/" + UuidResourceDtoLibrary.uuidResourceDto1().id))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldDeleteAResource() throws Exception {
        mockMvc
            .perform(delete("/uuid/" + UuidResourceDtoLibrary.uuidResourceDto1().id));

        assertThat(service.exists(UuidResourceDtoLibrary.uuidResourceDto1().id)).isFalse();
    }

    @Test
    public void shouldReturnHttpNotFound_whenDeletingNonExistingResource() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(delete("/uuid/nonExistingId"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnHttpNoContent_whenDeletingResources() throws Exception {
        mockMvc
            .perform(post("/uuid/deleteSeveral")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(Arrays.asList(
                    UuidResourceDtoLibrary.uuidResourceDto1().id,
                    UuidResourceDtoLibrary.uuidResourceDto2().id))))
            .andExpect(status().isNoContent());
    }

    @Test
    public void shouldDeleteResources() throws Exception {
        mockMvc
            .perform(post("/uuid/deleteSeveral")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(Arrays.asList(
                    UuidResourceDtoLibrary.uuidResourceDto1().id,
                    UuidResourceDtoLibrary.uuidResourceDto2().id))));

        assertThat(service.exists(UuidResourceDtoLibrary.uuidResourceDto1().id)).isFalse();
        assertThat(service.exists(UuidResourceDtoLibrary.uuidResourceDto2().id)).isFalse();
    }

    @Test
    public void shouldDeletesExistingResources_whenDeletingNonExistingAndExistingResourceIds() throws Exception {
        mockMvc
            .perform(post("/uuid/deleteSeveral")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(Arrays.asList(
                    UuidResourceDtoLibrary.uuidResourceDto1().id,
                    UuidResourceDtoLibrary.uuidResourceDto2().id,
                    "nonExistingId"))));

        assertThat(service.exists(UuidResourceDtoLibrary.uuidResourceDto1().id)).isFalse();
        assertThat(service.exists(UuidResourceDtoLibrary.uuidResourceDto2().id)).isFalse();
    }

    @Test
    public void shouldReturnHttpBadRequest_whenDeletingNull() throws Exception {
        dbFixture.noRollback();

        mockMvc
            .perform(post("/uuid/deleteSeveral")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(null)))
            .andExpect(status().isBadRequest());
    }

    private int countRows() throws Exception {
        return dbMemory.countRows(QUuidResource.uuidResource.getTableName());
    }
}
