package br.iwmvi.petshop.config;

import br.iwmvi.petshop.common.repository.SoftDeleteRepositoryFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "br.iwmvi.petshop",
        repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class
)
public class JpaConfig {
}
