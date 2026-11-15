package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {
    //@Modifying
    //@Query("DELETE FROM invitations i WHERE i.createdAt < :t")
    //int deleteBefore(@Param("t") Instant timestamp);
}
