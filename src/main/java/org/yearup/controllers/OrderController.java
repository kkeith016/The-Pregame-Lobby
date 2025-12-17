package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController
{
    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrdersController(OrderDao orderDao,
                            ShoppingCartDao shoppingCartDao,
                            UserDao userDao,
                            ProfileDao profileDao)
    {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    // POST http://localhost:8080/orders
    // Converts the user's shopping cart into an order
    @PostMapping
    public Order checkout(Principal principal)
    {
        try
        {
            // 1. Get the current user
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // 2. Get the user's shopping cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            // Check if cart is empty
            if (cart.getItems().isEmpty())
            {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot checkout with an empty cart"
                );
            }

            // 3. Get the user's profile for shipping address
            Profile profile = profileDao.getByUserId(userId);
            String shippingAddress = buildShippingAddress(profile);

            // 4. Create a new Order
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderDate(LocalDate.now());
            order.setShippingAddress(shippingAddress);
            order.setShippingAmount(java.math.BigDecimal.ZERO); // You can calculate shipping if needed

            // 5. Insert the order into the database
            order = orderDao.create(order);

            if (order == null || order.getOrderId() == 0)
            {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to create order"
                );
            }

            // 6. Convert each cart item to an OrderLineItem
            for (Map.Entry<Integer, ShoppingCartItem> entry : cart.getItems().entrySet())
            {
                ShoppingCartItem cartItem = entry.getValue();
                Product product = cartItem.getProduct();

                // Create OrderLineItem
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setOrderId(order.getOrderId());
                lineItem.setProductId(product.getProductId());
                lineItem.setSalesPrice(product.getPrice());
                lineItem.setQuantity(cartItem.getQuantity());
                lineItem.setDiscount(cartItem.getDiscountPercent());

                // Add line item to database
                orderDao.addLineItem(lineItem);

                // Add to order object for response
                order.addLineItem(lineItem);
            }

            // 7. Clear the shopping cart
            shoppingCartDao.clear(userId);

            // 8. Return the completed order
            return order;
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error processing checkout: " + ex.getMessage()
            );
        }
    }

    // Helper method to build shipping address from profile
    private String buildShippingAddress(Profile profile)
    {
        if (profile == null)
        {
            return "No address on file";
        }

        StringBuilder address = new StringBuilder();

        if (profile.getAddress() != null && !profile.getAddress().isEmpty())
        {
            address.append(profile.getAddress());
        }

        if (profile.getCity() != null && !profile.getCity().isEmpty())
        {
            if (address.length() > 0) address.append(", ");
            address.append(profile.getCity());
        }

        if (profile.getState() != null && !profile.getState().isEmpty())
        {
            if (address.length() > 0) address.append(", ");
            address.append(profile.getState());
        }

        if (profile.getZip() != null && !profile.getZip().isEmpty())
        {
            if (address.length() > 0) address.append(" ");
            address.append(profile.getZip());
        }

        return address.length() > 0 ? address.toString() : "No address on file";
    }
}