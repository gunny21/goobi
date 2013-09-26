package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Ldap;

class LdapMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6697737226604394665L;
    private static final Logger logger = Logger.getLogger(LdapMysqlHelper.class);

    public static List<Ldap> getLdaps(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen");
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
            List<Ldap> ret = new QueryRunner().query(connection, sql.toString(), LdapManager.resultSetToLdapListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getLdapCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ldapgruppenID) FROM ldapgruppen");
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

    public static Ldap getLdapById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen WHERE ldapgruppenID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            Ldap ret = new QueryRunner().query(connection, sql.toString(), LdapManager.resultSetToLdapHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveLdap(Ldap ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                String propNames =
                        "titel, homeDirectory, gidNumber, userDN, objectClasses, sambaSID, sn, uid, description, displayName, gecos, loginShell, sambaAcctFlags, sambaLogonScript, sambaPrimaryGroupSID, sambaPwdMustChange, sambaPasswordHistory, sambaLogonHours, sambaKickoffTime";
                String values = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
                Object[] param =
                        { ro.getTitel() == null ? null : ro.getTitel(), ro.getHomeDirectory() == null ? null : ro.getHomeDirectory(),
                                ro.getGidNumber() == null ? null : ro.getGidNumber(), ro.getUserDN() == null ? null : ro.getUserDN(),
                                ro.getObjectClasses() == null ? null : ro.getObjectClasses(), ro.getSambaSID() == null ? null : ro.getSambaSID(),
                                ro.getSn() == null ? null : ro.getSn(), ro.getUid() == null ? null : ro.getUid(),
                                ro.getDescription() == null ? null : ro.getDescription(), ro.getDisplayName() == null ? null : ro.getDisplayName(),
                                ro.getGecos() == null ? null : ro.getGecos(), ro.getLoginShell() == null ? null : ro.getLoginShell(),
                                ro.getSambaAcctFlags() == null ? null : ro.getSambaAcctFlags(),
                                ro.getSambaLogonScript() == null ? null : ro.getSambaLogonScript(),
                                ro.getSambaPrimaryGroupSID() == null ? null : ro.getSambaPrimaryGroupSID(),
                                ro.getSambaPwdMustChange() == null ? null : ro.getSambaPwdMustChange(),
                                ro.getSambaPasswordHistory() == null ? null : ro.getSambaPasswordHistory(),
                                ro.getSambaLogonHours() == null ? null : ro.getSambaLogonHours(),
                                ro.getSambaKickoffTime() == null ? null : ro.getSambaKickoffTime() };

                sql.append("INSERT INTO ldapgruppen (" + propNames + ") VALUES (" + values + ")");

                logger.debug(sql.toString() + ", " + Arrays.toString(param));
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
                if (id != null) {
                    ro.setId(id);
                }

            } else {

                sql.append("UPDATE ldapgruppen SET ");
                sql.append("titel = ?, ");
                sql.append("homeDirectory = ?, ");
                sql.append("gidNumber = ?, ");
                sql.append("userDN = ?, ");
                sql.append("objectClasses = ?, ");
                sql.append("sambaSID = ?, ");
                sql.append("sn = ?, ");
                sql.append("uid = ?, ");
                sql.append("description = ?, ");
                sql.append("displayName = ?, ");
                sql.append("gecos = ?, ");
                sql.append("loginShell = ?, ");
                sql.append("sambaAcctFlags = ?, ");
                sql.append("sambaLogonScript = ?, ");
                sql.append("sambaPrimaryGroupSID = ?, ");
                sql.append("sambaPwdMustChange = ?, ");
                sql.append("sambaPasswordHistory = ?, ");
                sql.append("sambaLogonHours = ?, ");
                sql.append("sambaKickoffTime = ? ");
                sql.append(" WHERE ldapgruppenID = " + ro.getId() + ";");

                Object[] param =
                        { ro.getTitel() == null ? null : ro.getTitel(), ro.getHomeDirectory() == null ? null : ro.getHomeDirectory(),
                                ro.getGidNumber() == null ? null : ro.getGidNumber(), ro.getUserDN() == null ? null : ro.getUserDN(),
                                ro.getObjectClasses() == null ? null : ro.getObjectClasses(), ro.getSambaSID() == null ? null : ro.getSambaSID(),
                                ro.getSn() == null ? null : ro.getSn(), ro.getUid() == null ? null : ro.getUid(),
                                ro.getDescription() == null ? null : ro.getDescription(), ro.getDisplayName() == null ? null : ro.getDisplayName(),
                                ro.getGecos() == null ? null : ro.getGecos(), ro.getLoginShell() == null ? null : ro.getLoginShell(),
                                ro.getSambaAcctFlags() == null ? null : ro.getSambaAcctFlags(),
                                ro.getSambaLogonScript() == null ? null : ro.getSambaLogonScript(),
                                ro.getSambaPrimaryGroupSID() == null ? null : ro.getSambaPrimaryGroupSID(),
                                ro.getSambaPwdMustChange() == null ? null : ro.getSambaPwdMustChange(),
                                ro.getSambaPasswordHistory() == null ? null : ro.getSambaPasswordHistory(),
                                ro.getSambaLogonHours() == null ? null : ro.getSambaLogonHours(),
                                ro.getSambaKickoffTime() == null ? null : ro.getSambaKickoffTime() };

                logger.debug(sql.toString() + ", " + Arrays.toString(param));
                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteLdap(Ldap ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM ldapgruppen WHERE ldapgruppenID = " + ro.getId() + ";";
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
