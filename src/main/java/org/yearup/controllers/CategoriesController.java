package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoriesController
{
    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Category> getAll()
    {
        try {
            return categoryDao.getAllCategories();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Category> getById(@PathVariable int id)
    {
        try {
            Category category = categoryDao.getById(id);
            if (category == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(category);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsByCategory(
            @PathVariable int categoryId,
            @RequestParam(required = false) String subCategory
    ) {
        try {
            Category category = categoryDao.getById(categoryId);
            if(category == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            List<Product> products;
            if(subCategory == null || subCategory.isEmpty()) {
                products = productDao.listByCategoryId(categoryId);
            } else {
                products = productDao.search(categoryId, null, null, subCategory);
            }

            if(products == null || products.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return products;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        try {
            Category created = categoryDao.create(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        try {
            Category updated = categoryDao.update(id, category);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id)
    {
        try {
            Category category = categoryDao.getById(id);
            if(category == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            categoryDao.delete(id);
            return ResponseEntity.noContent().build();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}