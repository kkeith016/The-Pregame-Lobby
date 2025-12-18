package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*") // adjust to frontend
public class ProductsController
{
    private final ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name="cat", required = false) Integer categoryId,
            @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name="subCategory", required = false) String subCategory
    ) {
        try {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search products");
        }
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        try {
            Product product = productDao.getById(id);
            if(product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            return product;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load product");
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product addProduct(@RequestBody Product product)
    {
        try {
            return productDao.create(product);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product");
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try {
            productDao.update(id, product);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product");
        }
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteProduct(@PathVariable int id)
    {
        try {
            Product product = productDao.getById(id);
            if(product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            productDao.delete(id);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete product");
        }
    }
}