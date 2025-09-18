package com.nitin.grok.dao.db2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<StateEntity, Long> {
}

