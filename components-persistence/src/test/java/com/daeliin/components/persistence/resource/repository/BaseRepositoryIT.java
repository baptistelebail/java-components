package com.daeliin.components.persistence.resource.repository;


import com.daeliin.components.core.pagination.Page;
import com.daeliin.components.core.pagination.PageRequest;
import com.daeliin.components.core.pagination.Sort;
import com.daeliin.components.persistence.fake.UuidResourceBaseRepository;
import com.daeliin.components.persistence.fixtures.JavaFixtures;
import com.daeliin.components.persistence.fixtures.UuidResourceRows;
import com.daeliin.components.persistence.sql.BUuidResource;
import com.daeliin.components.persistence.sql.QUuidResource;
import com.daeliin.components.test.rule.DbFixture;
import com.daeliin.components.test.rule.DbMemory;
import com.google.common.collect.Sets;
import com.querydsl.core.types.Predicate;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BaseRepositoryIT {

    @Inject
    private UuidResourceBaseRepository repository;

    @ClassRule
    public static DbMemory dbMemory = new DbMemory();

    @Rule
    public DbFixture dbFixture = new DbFixture(dbMemory, JavaFixtures.uuidResources());

    @Test
    public void shouldProvideTheRowPath() {
        dbFixture.noRollback();

        assertThat(repository.rowPath()).isEqualTo(QUuidResource.uuidResource);
    }

    @Test
    public void shouldCountResources() throws Exception {
        dbFixture.noRollback();

        assertThat(repository.count()).isEqualTo(countRows());
    }

    @Test
    public void shouldCountAllResources_whenCountingWithNullPredicate() throws Exception {
        dbFixture.noRollback();

        Predicate nullPredicate = null;
        assertThat(repository.count(nullPredicate)).isEqualTo(countRows());
    }

    @Test
    public void shouldReturnZero_whenCountingWithPredicateThatDoesntMachAnyResource() {
        dbFixture.noRollback();

        Predicate noMatchPredicate = QUuidResource.uuidResource.label.eq("Foo");
        assertThat(repository.count(noMatchPredicate)).isEqualTo(0);
    }

    @Test
    public void shouldCountResourcesWithPredicate() {
        dbFixture.noRollback();

        Predicate predicate = QUuidResource.uuidResource.label.eq(UuidResourceRows.uuidResource1().getLabel())
            .or(QUuidResource.uuidResource.label.eq(UuidResourceRows.uuidResource2().getLabel()));

        assertThat(repository.count(predicate)).isEqualTo(2);
    }

    @Test
    public void shouldReturnEmpty_whenFindingResourceWithNullPredicate() {
        dbFixture.noRollback();

        Predicate nullPredicate = null;

        assertThat(repository.findOne(nullPredicate).isPresent()).isFalse();
    }

    @Test
    public void shouldReturnEmpty_whenPredicateDoesntMatchAnyRow() {
        dbFixture.noRollback();

        Predicate predicate = QUuidResource.uuidResource.label.eq("nonExistingLabel");

        assertThat(repository.findOne(predicate).isPresent()).isFalse();
    }

    @Test
    public void shouldFindResource_accordingToPredicate() {
        dbFixture.noRollback();

        Predicate predicate = QUuidResource.uuidResource.uuid.eq(UuidResourceRows.uuidResource1().getUuid());

        assertThat(repository.findOne(predicate).get()).isEqualToComparingFieldByField(UuidResourceRows.uuidResource1());
    }

    @Test
    public void shouldFindAllResources_whenPredicateIsNull() throws Exception {
        dbFixture.noRollback();

        Predicate nullPredicate = null;

        Collection<BUuidResource> foundUuidEntities = repository.findAll(nullPredicate);

        assertThat(foundUuidEntities.size()).isEqualTo(countRows());
    }

    @Test
    public void shouldReturnEmptyCollection_whenPredicateDoesntMatchAnyResource() {
        dbFixture.noRollback();

        Predicate labelIsEqualToFoo = QUuidResource.uuidResource.label.eq("Foo");

        Collection<BUuidResource> foundUuidEntities = repository.findAll(labelIsEqualToFoo);

        assertThat(foundUuidEntities).isEmpty();
    }

    @Test
    public void shouldFindResources_accordingToPredicate() {
        dbFixture.noRollback();

        Predicate labelStartsWithLabel = QUuidResource.uuidResource.label.startsWith("label");

        Collection<BUuidResource> foundUuidEntities = repository.findAll(labelStartsWithLabel);

        assertThat(foundUuidEntities).usingFieldByFieldElementComparator().containsOnly(
            UuidResourceRows.uuidResource1(),
            UuidResourceRows.uuidResource2(),
            UuidResourceRows.uuidResource3(),
            UuidResourceRows.uuidResource4()
        );
    }

    @Test
    public void shouldFindAllResources() throws Exception {
        dbFixture.noRollback();

        Collection<BUuidResource> uuidEntities = repository.findAll();

        assertThat(uuidEntities.size()).isEqualTo(countRows());
    }

    @Test
    public void shouldFindPage1WithSize5SortedByIdDesc() throws Exception {
        dbFixture.noRollback();

        int uuidPersistentResourceCount = countRows();

        Collection<BUuidResource> uuidPersistentResourcePageContent = Arrays.asList(
            UuidResourceRows.uuidResource2(),
            UuidResourceRows.uuidResource1());

        Page<BUuidResource> page = repository.findAll(new PageRequest(1, 2, Sets.newLinkedHashSet(
            Arrays.asList(new Sort("uuid", Sort.Direction.DESC)))));

        assertThat(page.items)
            .usingFieldByFieldElementComparator()
            .containsExactly(uuidPersistentResourcePageContent.toArray(new BUuidResource[uuidPersistentResourcePageContent.size()]));
        assertThat(page.nbItems).isEqualTo(uuidPersistentResourcePageContent.size());
        assertThat(page.totalItems).isEqualTo(uuidPersistentResourceCount);
        assertThat(page.totalPages).isEqualTo(uuidPersistentResourceCount / 2);
    }

    @Test
    public void shouldApplySortsInTheSameOrderAsTheyWereRequested() throws Exception {
        dbFixture.noRollback();

        int uuidPersistentResourceCount = countRows();

        Collection<BUuidResource> uuidPersistentResourcePageContent = Arrays.asList(
            UuidResourceRows.uuidResource3(),
            UuidResourceRows.uuidResource2(),
            UuidResourceRows.uuidResource1(),
            UuidResourceRows.uuidResource4());

        Page<BUuidResource> page = repository.findAll(new PageRequest(0, uuidPersistentResourceCount, Sets.newLinkedHashSet(
            Arrays.asList(new Sort("creationDate", Sort.Direction.ASC), new Sort("uuid", Sort.Direction.DESC)))));

        assertThat(page.items)
            .usingFieldByFieldElementComparator()
            .containsExactly(uuidPersistentResourcePageContent.toArray(new BUuidResource[uuidPersistentResourcePageContent.size()]));
    }

    @Test
    public void shouldFindPageWithPredicate() {
        dbFixture.noRollback();

        Predicate uuidDoesntContain511 = QUuidResource.uuidResource.uuid.contains("511").not();
        PageRequest pageRequest = new PageRequest(0, 2, Sets.newLinkedHashSet(Arrays.asList(new Sort("label", Sort.Direction.ASC))));

        Page<BUuidResource> page = repository.findAll(uuidDoesntContain511, pageRequest);

        assertThat(page.items).usingFieldByFieldElementComparator().containsOnly(
            UuidResourceRows.uuidResource2(),
            UuidResourceRows.uuidResource3());

        assertThat(page.totalItems).isEqualTo(3);
        assertThat(page.totalPages).isEqualTo(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExpcetion_whenDeletingWithNullPredicate() {
        dbFixture.noRollback();

        Predicate nullPredicate = null;
        repository.delete(nullPredicate);
    }

    @Test
    public void shouldNotDeleteAnyResource_whenDeletingWithPredicateThatDoesntMachAnyResource() throws Exception {
        dbFixture.noRollback();

        int uuidPersistentResourceCount = countRows();

        Predicate noMatchPredicate = QUuidResource.uuidResource.label.eq("Foo");
        assertThat(repository.delete(noMatchPredicate)).isFalse();

        assertThat(countRows()).isEqualTo(uuidPersistentResourceCount);
    }

    @Test
    public void shouldDeleteResourcesWithPredicate() throws Exception{
        int uuidPersistentResourceCount = countRows();

        Predicate predicate = QUuidResource.uuidResource.label.eq(UuidResourceRows.uuidResource1().getLabel())
                .or(QUuidResource.uuidResource.label.eq(UuidResourceRows.uuidResource2().getLabel()));

        assertThat(repository.delete(predicate)).isTrue();
        assertThat(countRows()).isEqualTo(uuidPersistentResourceCount - 2);
    }

    @Test
    public void shouldDeleteAllResources() throws Exception {
        repository.deleteAll();

        int uuidPersistentResourceCountAfterDelete = countRows();

        assertThat(uuidPersistentResourceCountAfterDelete).isEqualTo(0);
    }

    private int countRows() throws Exception {
        return dbMemory.countRows(QUuidResource.uuidResource.getTableName());
    }
}
