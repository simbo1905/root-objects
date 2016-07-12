package com.github.simbo1905.rootobjects.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * This is not a public class deliberately as we want a service class to save things in the correct order in a transaction.
 */
interface ContractRespository extends JpaRepository<Contract, Long> {
    @Query("select c from Contract c where c.name = ?1")
    Contract findByName(String name);
}
