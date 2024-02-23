package com.example.myTelegramBot.service.serviceImpl;

import com.example.myTelegramBot.domain.Product;
import com.example.myTelegramBot.repository.ProductRepository;
import com.example.myTelegramBot.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> readAll() {
        return productRepository.findAll();
    }

    @Override
    public Product updateById(Product product, Long id) {
        Product product1 = productRepository.findById(id).orElse(null);
        product1.setName(product1.getName());
        product1.setPrice(product1.getPrice());
        product1.setDescription(product.getDescription());
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
