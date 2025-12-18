package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT category_id, name, description FROM categories";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet results = statement.executeQuery())
        {
            while (results.next())
            {
                categories.add(mapRow(results));
            }
        }
        catch(SQLException e)
        {
            throw new RuntimeException("Error retrieving categories", e);
        }

        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        String query = "SELECT category_id, name, description FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, categoryId);

            try (ResultSet results = statement.executeQuery())
            {
                if (results.next())
                    return mapRow(results);
            }
        }
        catch(SQLException e)
        {
            throw new RuntimeException("Error retrieving category by ID", e);
        }

        return null;
    }

    @Override
    public Category create(Category category)
    {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0)
            {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next())
                {
                    int newId = generatedKeys.getInt(1);
                    return getById(newId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating category", e);
        }

        return null;
    }

    @Override
    public Category update(int categoryId, Category category)
    {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0)
                return null;

            return getById(categoryId);
        }
        catch(SQLException e)
        {
            throw new RuntimeException("Error updating category", e);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(description);

        return category;
    }
}