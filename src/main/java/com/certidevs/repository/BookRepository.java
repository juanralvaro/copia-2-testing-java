package com.certidevs.repository;

import com.certidevs.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByCategories_Id(Long id);

    List<Book> findByTitleLikeIgnoreCase(String title);

    @Query("""
select b from Book b
join fetch b.categories
where b.title = ?1
""")
    Optional<Book> findBookEagerByTitle(String title);
}