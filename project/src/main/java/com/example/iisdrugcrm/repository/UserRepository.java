package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findTop10ByUsernameContainingIgnoreCaseAndRoleAndIsActiveTrueOrderByUsernameAsc(String username, UserRole role);
}