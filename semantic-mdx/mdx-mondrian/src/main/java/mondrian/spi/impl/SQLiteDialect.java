package mondrian.spi.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link mondrian.spi.Dialect} for the SQLite database.
 *
 * @author xiaobao
 * @since Jul 13th, 2025
 */
public class SQLiteDialect extends JdbcDialectImpl {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(
            SQLiteDialect.class,
            DatabaseProduct.SQLite);

    public SQLiteDialect(Connection connection) throws SQLException {
        super(connection);
    }

    protected void quoteDateLiteral(
        StringBuilder buf,
        String value,
        Date date)
    {
        throw new RuntimeException("Not implemented");
    }

    public String generateInline(
        List<String> columnNames,
        List<String> columnTypes,
        List<String[]> valueList)
    {
        throw new RuntimeException("Not implemented");
    }
}

// End SQLiteDialect.java
