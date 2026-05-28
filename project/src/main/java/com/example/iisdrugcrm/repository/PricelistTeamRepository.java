package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.PricelistTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PricelistTeamRepository extends JpaRepository<PricelistTeam, Long> {

    boolean existsByName(String name);

    @Query("""
            select distinct t
            from PricelistTeam t
            left join t.memberIds memberId
            where t.leaderId = :userId or memberId = :userId
            order by t.name asc
            """)
    List<PricelistTeam> findTeamsForUser(@Param("userId") Long userId);
}