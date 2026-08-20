package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Living in com.example (the root package) means component
 * scanning, repository scanning, and entity scanning all pick up the
 * sub-packages by default - no @ComponentScan / @EnableJpaRepositories /
 * @EntityScan needed.
 *
 * There used to be a @Scheduled job here (ShelfExpiryScheduler) that
 * swept expired rentals off My Shelf every hour. It was removed once
 * rentals moved to My Library, because My Shelf now only ever holds
 * purchases, which never expire - there is nothing left to sweep.
 */
@SpringBootApplication
public class BookwormApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookwormApplication.class, args);
    }
}
