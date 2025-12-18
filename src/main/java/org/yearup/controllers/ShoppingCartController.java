package org.yearup.controllers;

import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao,
                                  UserDao userDao,
                                  ProductDao productDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // GET /cart
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        User user = userDao.getByUserName(principal.getName());
        return shoppingCartDao.getByUserId(user.getId());
    }

    // POST /cart/products/{productId}
    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> addProduct(@PathVariable int productId, Principal principal)
    {
        User user = userDao.getByUserName(principal.getName());

        Product product = productDao.getById(productId);
        if (product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        if (shoppingCartDao.exists(user.getId(), productId))
            shoppingCartDao.incrementQuantity(user.getId(), productId);
        else
            shoppingCartDao.add(user.getId(), productId, 1);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // PUT /cart/products/{productId}
    @PutMapping("/products/{productId}")
    public void updateProduct(@PathVariable int productId,
                              @RequestBody ShoppingCartItem item,
                              Principal principal)
    {
        User user = userDao.getByUserName(principal.getName());

        if (!shoppingCartDao.exists(user.getId(), productId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart");

        if (item.getQuantity() <= 0)
            shoppingCartDao.delete(user.getId(), productId);
        else
            shoppingCartDao.update(user.getId(), productId, item.getQuantity());
    }

    // DELETE /cart
    @DeleteMapping
    public void clearCart(Principal principal)
    {
        User user = userDao.getByUserName(principal.getName());
        shoppingCartDao.clear(user.getId());
    }
}
