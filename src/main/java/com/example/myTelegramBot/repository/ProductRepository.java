package com.example.myTelegramBot.repository;

import com.example.myTelegramBot.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
