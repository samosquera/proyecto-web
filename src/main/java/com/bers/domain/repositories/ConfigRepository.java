package com.bers.domain.repositories;

import com.bers.domain.entities.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {

    Optional<Config> findByKey(String key);

    boolean existsByKey(String key);

    void deleteByKey(String key);
}
