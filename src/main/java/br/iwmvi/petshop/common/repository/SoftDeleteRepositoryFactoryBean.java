package br.iwmvi.petshop.common.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import jakarta.persistence.EntityManager;
import java.io.Serializable;

public class SoftDeleteRepositoryFactoryBean<R, T, ID extends Serializable>
        extends JpaRepositoryFactoryBean<R, T, ID> {

    public SoftDeleteRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new SoftDeleteRepositoryFactory(entityManager);
    }

    private static class SoftDeleteRepositoryFactory extends JpaRepositoryFactory {

        public SoftDeleteRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            if (SoftDeleteRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                return SoftDeleteRepositoryImpl.class;
            }
            return super.getRepositoryBaseClass(metadata);
        }
    }
}
