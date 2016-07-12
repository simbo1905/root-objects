package com.github.simbo1905.rootobjects.contract;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This is not a public class deliberately as we want a service class to save things in the correct order in a transaction.
 */
interface LineItemRepository extends JpaRepository<LineItem, Long> {

}
