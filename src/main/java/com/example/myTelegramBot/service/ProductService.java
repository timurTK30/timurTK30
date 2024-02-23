package com.example.myTelegramBot.service;

import com.example.myTelegramBot.domain.Product;

import java.util.List;

public interface ProductService {

    Product save(Product product);
    List<Product> readAll();
    Product updateById(Product product, Long id);
    void deleteById(Long id);
}
