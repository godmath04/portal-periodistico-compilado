package com.portalperiodistico.article_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// 1. Para Componentes (@Service, @Controller)
@SpringBootApplication(scanBasePackages = "com.portalperiodistico")
// 2. Para Entidades (@Entity)
@EntityScan(basePackages = "com.portalperiodistico")
// 3. Para Repositorios (JpaRepository)
@EnableJpaRepositories(basePackages = "com.portalperiodistico")
public class ArticleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArticleServiceApplication.class, args);
	}

}
