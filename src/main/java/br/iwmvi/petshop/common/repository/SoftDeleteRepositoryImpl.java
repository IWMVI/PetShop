package br.iwmvi.petshop.common.repository;

import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public class SoftDeleteRepositoryImpl<T extends SoftDeleteEntity, ID>
        extends SimpleJpaRepository<T, ID>
        implements SoftDeleteRepository<T, ID> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;

    public SoftDeleteRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public Optional<T> findActiveById(ID id) {
        return findById(id).filter(SoftDeleteEntity::isActive);
    }

    @Override
    public List<T> findAllActive() {
        return findAll().stream()
                .filter(SoftDeleteEntity::isActive)
                .toList();
    }

    @Override
    public void softDelete(ID id) {
        findById(id).ifPresent(entity -> {
            entity.delete();
            save(entity);
        });
    }

    @Override
    public void softDeleteAll(Iterable<ID> ids) {
        ids.forEach(this::softDelete);
    }
}
