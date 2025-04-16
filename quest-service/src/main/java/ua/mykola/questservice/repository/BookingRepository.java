package ua.mykola.questservice.repository;

import commons.dto.QuestCode;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.mykola.questservice.entity.BookingEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

@Query("""
        SELECT b.time
        FROM BookingEntity b
        WHERE b.quest.name = :questName
            AND b.date = :date
        """)
List<LocalTime> findBookedTimesByQuestNameAndDate(
        @Param("questName") String questName,
        @Param("date") LocalDate date
);

    List<BookingEntity> findByChatIdAndDateGreaterThanEqual(String chatId, LocalDate date, Sort sort);

    @Query("""
        SELECT b.chatId FROM BookingEntity b
        WHERE b.quest.code = :code
            AND b.date = :date
            AND b.time = :time
    """)
    Optional<String> findChatIdByQuestCodeDateTime(
            @Param("code") QuestCode code,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );
}
