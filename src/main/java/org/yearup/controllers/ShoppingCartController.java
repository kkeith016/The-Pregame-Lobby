package org.yearup.controllers;

import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
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
@CrossOrigin(origins = "https://localhost:8080")
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
        try
        {
            User user = userDao.getByUserName(principal.getName());
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve shopping cart."
            );
        }
    }

    // POST /cart/products/{productId}
    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> addProduct(@PathVariable int productId, Principal principal)
    {
        try
        {
            User user = userDao.getByUserName(principal.getName());

            if (shoppingCartDao.exists(user.getId(), productId))
                shoppingCartDao.incrementQuantity(user.getId(), productId);
            else
                shoppingCartDao.add(user.getId(), productId, 1);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to add product to cart."
            );
        }
    }

    // PUT /cart/products/{productId}
    @PutMapping("/products/{productId}")
    public void updateProduct(@PathVariable int productId,
                              @RequestBody ShoppingCartItem item,
                              Principal principal)
    {
        try
        {
            User user = userDao.getByUserName(principal.getName());

            if (shoppingCartDao.exists(user.getId(), productId))
                shoppingCartDao.update(user.getId(), productId, item.getQuantity());
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to update shopping cart."
            );
        }
    }

    // DELETE /cart
    @DeleteMapping
    public void clearCart(Principal principal)
    {
        try
        {
            User user = userDao.getByUserName(principal.getName());
            shoppingCartDao.clear(user.getId());
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to clear shopping cart."
            );
        }
    }
}