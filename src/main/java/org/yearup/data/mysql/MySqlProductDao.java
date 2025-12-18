package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    // SEARCH PRODUCTS
    @Override
    public List<Product> search(
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String subCategory)
    {
        List<Product> products = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM products
            WHERE 1 = 1
        """);

        List<Object> params = new ArrayList<>();

        if (categoryId != null)
        {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (minPrice != null)
        {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null)
        {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if (subCategory != null && !subCategory.isBlank())
        {
            sql.append(" AND subcategory LIKE ?");
            params.add("%" + subCategory + "%");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString()))
        {
            for (int i = 0; i < params.size(); i++)
            {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                products.add(mapRow(rs));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error searching products", e);
        }

        return products;
    }

    // LIST PRODUCTS BY CATEGORY
    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE category_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                products.add(mapRow(rs));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving products by category", e);
        }

        return products;
    }

    // GET PRODUCT BY ID
    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return mapRow(rs);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving product " + productId, e);
        }

        return null;
    }

    // CREATE PRODUCT
    @Override
    public Product create(Product product)
    {
        String sql = """
            INSERT INTO products
            (name, price, category_id, description, subcategory, image_url, stock, featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, product.getName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getSubCategory());
            ps.setString(6, product.getImageUrl());
            ps.setInt(7, product.getStock());
            ps.setBoolean(8, product.isFeatured());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
            {
                return getById(keys.getInt(1));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating product", e);
        }

        return null;
    }

    // UPDATE PRODUCT (FIXED)
    @Override
    public Product update(int productId, Product product)
    {
        String sql = """
            UPDATE products
            SET name = ?,
                price = ?,
                category_id = ?,
                description = ?,
                subcategory = ?,
                image_url = ?,
                stock = ?,
                featured = ?
            WHERE product_id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, product.getName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getSubCategory());
            ps.setString(6, product.getImageUrl());
            ps.setInt(7, product.getStock());
            ps.setBoolean(8, product.isFeatured());
            ps.setInt(9, productId);

            int rows = ps.executeUpdate();

            if (rows == 0)
            {
                return null;
            }

            return getById(productId);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating product " + productId, e);
        }
    }

    // DELETE PRODUCT
    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting product " + productId, e);
        }
    }

    // MAP ROW → PRODUCT
    protected static Product mapRow(ResultSet row) throws SQLException
    {
        return new Product(
                row.getInt("product_id"),
                row.getString("name"),
                row.getBigDecimal("price"),
                row.getInt("category_id"),
                row.getString("description"),
                row.getString("subcategory"),
                row.getInt("stock"),
                row.getBoolean("featured"),
                row.getString("image_url")
        );
    }
}