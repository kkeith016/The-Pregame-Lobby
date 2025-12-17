package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    // GET: return full shopping cart for a user
    ShoppingCart getByUserId(int userId);

    // Check if a product is already in the cart
    boolean exists(int userId, int productId);

    // Add a product to the cart (quantity can be 1 for new items)
    void add(int userId, int productId, int quantity);

    // Increment quantity of an existing product in the cart
    void incrementQuantity(int userId, int productId);

    // Update the quantity of a product in the cart
    void update(int userId, int productId, int quantity);

    // Delete a single product from the cart
    void delete(int userId, int productId);

    // Clear all items from a user's cart
    void clear(int userId);
}