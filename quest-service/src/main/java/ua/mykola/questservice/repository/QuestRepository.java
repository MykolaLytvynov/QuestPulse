package ua.mykola.questservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.mykola.questservice.entity.QuestEntity;

import java.util.Optional;

@Repository
public interface QuestRepository extends JpaRepository<QuestEntity, Long> {
    Optional<QuestEntity> findByName(String title);
}
