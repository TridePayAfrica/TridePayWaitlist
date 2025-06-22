package com.tride.tridewaitlist.repository;

import com.tride.tridewaitlist.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
   // boolean existsByEmail(String email);
}
