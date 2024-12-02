package com.certidevs;

import com.certidevs.model.*;
import com.certidevs.repository.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		var context = SpringApplication.run(Main.class, args);

		var productRepository = context.getBean(ProductRepository.class);
		var manufacturerRepo = context.getBean(ManufacturerRepository.class);
		var categoryRepository = context.getBean(CategoryRepository.class);
		var bookRepository = context.getBean(BookRepository.class);

		if (productRepository.count() == 0) {
			var prod1 = Product.builder().name("Zumo multifrutas").price(1.33).quantity(1).active(true).build();
			var prod2 = Product.builder().name("Granola").price(4.33).quantity(4).active(false).build();
			productRepository.save(prod1);
			productRepository.save(prod2);
		}
		if (manufacturerRepo.count() == 0) {
			var address1 = Address.builder().street("Calle Alfonso").city("Zaragoza").state("Aragón").zipcode("50003").build();
			var address2 = Address.builder().street("Calle Córdoba").city("Jaén").state("Andalucía").zipcode("23007").build();

			var manufacturer1 = Manufacturer.builder()
					.name("Adidas")
					.description("description A")
					.year(2024)
					.imageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Adidas_2022_logo.svg/1920px-Adidas_2022_logo.svg.png")
					.address(address1)
					.build();

			var manufacturer2 = Manufacturer.builder()
					.name("CertiDevs")
					.description("description A")
					.year(2024)
					.imageUrl("https://app.certidevs.com/content/images/CertiDevs-logo.svg")
					.address(address2)
					.build();
			manufacturerRepo.saveAll(
					List.of(manufacturer1, manufacturer2)
			);
		}
		if (categoryRepository.count() == 0){
			Category cat1 = categoryRepository.save(Category.builder().name("Ficción").description("description larga").build());
			Category cat2 = categoryRepository.save(Category.builder().name("Comedia").description("description larga").build());
			Category cat3 = categoryRepository.save(Category.builder().name("Romántica").description("description larga").build());

			Book book1 = new Book(); // Crearlo con constructor para que tenga el Set de categories inicializado en vez de null
			book1.setTitle("Libro 1");
			book1.setPrice(30.5);
			book1.getCategories().add(cat1);
			book1.getCategories().add(cat2);
			book1.getCategories().add(cat3);
			//book1.getCategories().addAll(Set.of(cat1, cat2, cat3));
			bookRepository.save(book1); // Owner de la asociación con Category, se tiene que guardar el book para que se guarde la asociación.
		}
	}
}
