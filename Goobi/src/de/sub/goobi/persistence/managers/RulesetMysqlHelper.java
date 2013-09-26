package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Ruleset;

class RulesetMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4768263760579941811L;
    private static final Logger logger = Logger.getLogger(RulesetMysqlHelper.class);

    public static List<Ruleset> getRulesets(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Ruleset> getAllRulesets() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getRulesetCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(MetadatenKonfigurationID) FROM metadatenkonfigurationen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Ruleset getRulesetForId(int rulesetId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] params = { rulesetId };
            logger.debug(sql.toString() + ", " + rulesetId);
            Ruleset ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetHandler, params);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveRuleset(Ruleset ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                String propNames = "Titel, Datei, orderMetadataByRuleset";
                String propValues = "?,? ,?";
                Object[] param = {ro.getTitel(),ro.getDatei(), ro.isOrderMetadataByRuleset() };
                
                sql.append("INSERT INTO metadatenkonfigurationen (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");

                logger.debug(sql.toString() + ", " + Arrays.toString(param));
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                Object[] param = {ro.getTitel(),ro.getDatei(), ro.isOrderMetadataByRuleset() };
                sql.append("UPDATE metadatenkonfigurationen SET ");
                sql.append("Titel = ?, ");
                sql.append("Datei = ?, ");
                sql.append("orderMetadataByRuleset = ?");
                sql.append(" WHERE MetadatenKonfigurationID = " + ro.getId() + ";");
                logger.debug(sql.toString() + ", " + Arrays.toString(param));
                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteRuleset(Ruleset ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = " + ro.getId() + ";";
                logger.debug(sql);
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }
}
