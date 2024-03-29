package com.duty.manager.repository;

import com.duty.manager.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByName(String name);

    @Query(
            nativeQuery = true,
            value = """
                    WITH similarity_query AS (
                        SELECT *, word_similarity(name, :name) AS name_similarity
                        FROM templates
                    )
                    SELECT id, name, description, version
                    FROM similarity_query
                    WHERE name_similarity >= 0.1
                    ORDER BY name_similarity desc
                    LIMIT 50;
                    """
    )
    List<Template> fuzzySearchByName(@Param("name") String name);

}
