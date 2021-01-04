package com.wso2.carbon.custom.user.store.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

public class CustomUserStoreManager extends JDBCUserStoreManager {
    private static Log log = LogFactory.getLog(CustomUserStoreManager.class);

    public CustomUserStoreManager() {
    }

    public CustomUserStoreManager(org.wso2.carbon.user.api.RealmConfiguration realmConfig,
                               Map<String, Object> properties,
                               ClaimManager claimManager,
                               ProfileConfigurationManager profileManager,
                               UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            log.error("Anonymous user trying to login");
            return false;
        }

        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt = null;
        String password = (String) credential;
        boolean isAuthed = false;

        try {
            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);
            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER);

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userName);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("ENCRYPTED_USER_PASSWORD");
                if ((storedPassword != null) && (storedPassword.trim().equals(password))) {
                    isAuthed = true;
                }
            }
        } catch (SQLException e) {
            throw new UserStoreException("Authentication Failure. Using sql :" + sqlstmt);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("User " + userName + " login attempt. Login success :: " + isAuthed);
        }

        return isAuthed;
    }

    @Override
    public boolean doCheckExistingUser(String userName) throws UserStoreException {
        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_EXISTING);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for is user existing null");
        }

        return isValueExisting(sqlStmt, null, userName);
    }

    public Date getPasswordExpirationTime(String userName) throws UserStoreException {
        // not supporting this by sample user store manager.
        return null;
    }

    @Override
    public org.wso2.carbon.user.api.Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        properties.setMandatoryProperties(CustomUserStoreManagerConstants.MANDATORY_PROPERTIES.toArray
                (new Property[CustomUserStoreManagerConstants.MANDATORY_PROPERTIES.size()]));
        properties.setOptionalProperties(CustomUserStoreManagerConstants.OPTIONAL_PROPERTIES.toArray
                (new Property[CustomUserStoreManagerConstants.OPTIONAL_PROPERTIES.size()]));
        properties.setAdvancedProperties(CustomUserStoreManagerConstants.ADVANCED_PROPERTIES.toArray
                (new Property[CustomUserStoreManagerConstants.ADVANCED_PROPERTIES.size()]));
        return properties;
    }

    public String[] getProfileNames(String userName) throws UserStoreException {
        return new String[]{UserCoreConstants.DEFAULT_PROFILE};
    }

    public boolean isValueExisting(String sqlStmt, Connection dbConnection, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        boolean doClose = false;
        try {
            if (dbConnection == null) {
                dbConnection = getDBConnection();
                doClose = true; // because we created it
            }
            if (DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, params).length > 0) {
                isExisting = true;
            }
            return isExisting;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            if (doClose) {
                DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }
        }
    }
}
