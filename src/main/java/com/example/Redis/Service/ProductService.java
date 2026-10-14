package com.example.Redis.Service;

import java.math.BigDecimal;
import java.util.List;

import com.example.Redis.Entity.Product;
import com.example.Redis.Repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository repository;

    private static final Logger log =
            LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // GET BY ID: Redis first, PostgreSQL on cache miss.
    @Cacheable(cacheNames = "products", key = "#id")
    public Product getProduct(Long id) {

        log.info("CACHE MISS: Fetching product ID={} from PostgreSQL",
                id);

        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("PRODUCT NOT FOUND: ID={}", id);
                    return new RuntimeException(
                            "Product not found: " + id);
                });

        log.info(
                "DATABASE FETCH SUCCESS: ID={}, name={}",
                product.getId(),
                product.getName()
        );

        return product;
    }

    // GET ALL: Redis first, PostgreSQL on cache miss.
    @Cacheable(cacheNames = "productLists", key = "'all'")
    public List<Product> getAllProducts() {

        log.info(
                "CACHE MISS: Fetching all products from PostgreSQL");

        List<Product> products = repository.findAll();

        log.info(
                "DATABASE FETCH SUCCESS: Retrieved {} products",
                products.size()
        );

        return products;
    }

    // CREATE: Save to PostgreSQL and evict cached product lists.
    @Transactional
    @CacheEvict(
            cacheNames = "productLists",
            allEntries = true
    )
    public Product createProduct(
            String name, BigDecimal price) {

        log.info("CREATE PRODUCT: name={}, price={}",
                name, price);

        Product product = new Product(name, price);
        Product savedProduct = repository.save(product);

        log.info(
                "PRODUCT CREATED: ID={}, name={}. " +
                        "Product list cache eviction is scheduled.",
                savedProduct.getId(),
                savedProduct.getName()
        );

        return savedProduct;
    }

    // UPDATE: Refresh individual product cache and evict lists.
    @Transactional
    @Caching(
            put = {
                    @CachePut(
                            cacheNames = "products",
                            key = "#id"
                    )
            },
            evict = {
                    @CacheEvict(
                            cacheNames = "productLists",
                            allEntries = true
                    )
            }
    )
    public Product updateProduct(
            Long id,
            String name,
            BigDecimal price) {

        log.info(
                "UPDATE PRODUCT: ID={}, newName={}, newPrice={}",
                id, name, price
        );

        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "UPDATE FAILED: Product ID={} not found",
                            id
                    );
                    return new RuntimeException(
                            "Product not found: " + id);
                });

        product.setName(name);
        product.setPrice(price);

        Product updatedProduct = repository.save(product);

        log.info(
                "PRODUCT UPDATED: ID={}, name={}, price={}. " +
                        "Cache refresh and list eviction are configured.",
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getPrice()
        );

        return updatedProduct;
    }

    // DELETE: Remove from PostgreSQL and evict both caches.
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "products",
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = "productLists",
                    allEntries = true
            )
    })
    public void deleteProduct(Long id) {

        log.info("DELETE PRODUCT: ID={}", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "DELETE FAILED: Product ID={} not found",
                            id
                    );
                    return new RuntimeException(
                            "Product not found: " + id);
                });

        repository.delete(product);

        log.info(
                "PRODUCT DELETED: ID={}. " +
                        "Individual and list cache eviction are configured.",
                id
        );
    }
}