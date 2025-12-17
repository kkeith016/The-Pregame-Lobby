package org.yearup.data.mysql;

import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    // -------------------------------------------------
    // GET SHOPPING CART BY USER ID
    // -------------------------------------------------
    @Override
    public ShoppingCart getByUserId(int userId)
    {
        String sql = """
            SELECT
                p.product_id,
                p.name,
                p.price,
                p.category_id,
                p.description,
                p.sub_category,
                p.stock,
                p.image_url,
                p.featured,
                sc.quantity
            FROM shopping_cart sc
            JOIN products p ON p.product_id = sc.product_id
            WHERE sc.user_id = ?
        """;

        ShoppingCart cart = new ShoppingCart();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);

            try (ResultSet results = statement.executeQuery())
            {
                while (results.next())
                {
                    Product product = MySqlProductDao.mapRow(results);

                    int quantity = results.getInt("quantity");
                    BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    item.setDiscountPercent(BigDecimal.valueOf(0));
                    item.setLineTotal(lineTotal);

                    cart.getItems().put(product.getProductId(), item);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error pulling up your shopping cart.", e);
        }

        return cart;
    }

    // -------------------------------------------------
    // CHECK IF ITEM EXISTS IN CART
    // -------------------------------------------------
    @Override
    public boolean exists(int userId, int productId)
    {
        String sql = "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error checking if cart item exists.", e);
        }

        return false;
    }

    // -------------------------------------------------
    // ADD PRODUCT
    // -------------------------------------------------
    @Override
    public void add(int userId, int productId, int quantity)
    {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error adding item to cart.", e);
        }
    }

    // -------------------------------------------------
    // INCREMENT QUANTITY
    // -------------------------------------------------
    @Override
    public void incrementQuantity(int userId, int productId)
    {
        String sql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error incrementing cart item quantity.", e);
        }
    }

    // -------------------------------------------------
    // UPDATE QUANTITY
    // -------------------------------------------------
    @Override
    public void update(int userId, int productId, int quantity)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating cart item.", e);
        }
    }

    // -------------------------------------------------
    // DELETE ONE PRODUCT
    // -------------------------------------------------
    @Override
    public void delete(int userId, int productId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting cart item.", e);
        }
    }

    // -------------------------------------------------
    // CLEAR CART
    // -------------------------------------------------
    @Override
    public void clear(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error clearing shopping cart.", e);
        }
    }
}