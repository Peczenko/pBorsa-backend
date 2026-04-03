package com.pborsa.trading.credentials;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for user API credentials.
 */
@Repository
public interface UserApiCredentialsRepository extends JpaRepository<UserApiCredentials, Long> {

    /**
     * Finds credentials by user ID.
     */
    Optional<UserApiCredentials> findByUserId(Long userId);

    /**
     * Checks if credentials exist for a user.
     */
    boolean existsByUserId(Long userId);

    /**
     * Finds all active credentials.
     */
    List<UserApiCredentials> findByActiveTrue();

    /**
     * Finds credentials for paper trading accounts.
     */
    List<UserApiCredentials> findByPaperTradingTrueAndActiveTrue();

    /**
     * Deactivates credentials for a user.
     */
    @Modifying
    @Query("UPDATE UserApiCredentials c SET c.active = false WHERE c.userId = :userId")
    int deactivateByUserId(@Param("userId") Long userId);

    /**
     * Deletes credentials by user ID.
     */
    void deleteByUserId(Long userId);
}

