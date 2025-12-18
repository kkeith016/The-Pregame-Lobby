package org.yearup.data.mysql;

import org.yearup.data.ProductDao;
import org.yearup.models.Product;
import org.springframework.stereotype.Component;

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

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory)
    {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if(minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }

        if(maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if(subCategory != null && !subCategory.isEmpty()) {
            sql.append(" AND LOWER(subcategory) = LOWER(?)"); // case-insensitive
            params.add(subCategory);
        }

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            for(int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            ResultSet row = statement.executeQuery();
            while (row.next()) {
                products.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();
            while (row.next()) {
                products.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();
            if (row.next()) {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Product create(Product product)
    {
        String sql = "INSERT INTO products(name, price, category_id, description, subcategory, image_url, stock, featured) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return getById(generatedKeys.getInt(1));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void update(int productId, Product product)
    {
        String sql = "UPDATE products SET name=?, price=?, category_id=?, description=?, subcategory=?, image_url=?, stock=?, featured=? WHERE product_id=?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id=?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

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