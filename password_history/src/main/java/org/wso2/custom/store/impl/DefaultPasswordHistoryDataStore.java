package org.wso2.custom.store.impl;

import org.wso2.custom.Constants;
import org.wso2.custom.exeption.IdentityPasswordHistoryException;
import org.wso2.custom.store.PasswordHistoryDataStore;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface provides to plug module for preferred persistence store.
 */
public class DefaultPasswordHistoryDataStore implements PasswordHistoryDataStore {
    private static final String SHA_1_PRNG = "SHA1PRNG";
    private static final Log log = LogFactory.getLog(DefaultPasswordHistoryDataStore.class);
    private String digestFunction;
    private int maxHistoryCount;
    private int passwordHistoryTimeInHours;

    public DefaultPasswordHistoryDataStore(String digestFunction, int maxHistoryCount, int passwordHistoryTimeInHours) {
        this.digestFunction = digestFunction;
        this.maxHistoryCount = maxHistoryCount;
        this.passwordHistoryTimeInHours = passwordHistoryTimeInHours;
    }

    @Override
    public void store(User user, Object credential) throws
            IdentityPasswordHistoryException {

        //History not validate if password is empty
        if (credential == null) {
            return;
        }
        String saltValue = generateSaltValue();

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        List<Integer> recordsToDelete = new ArrayList<>();
        int storedHistoryCount = 0;
        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt3 = null;

        ResultSet resultSet = null;
        try {
            connection.setAutoCommit(false);
            prepStmt1 = connection.prepareStatement(Constants.SQLQueries.LOAD_HISTORY_DATA);
            prepStmt1.setString(1, user.getUserName());
            prepStmt1.setString(2, user.getUserStoreDomain());
            prepStmt1.setInt(3, IdentityTenantUtil.getTenantId(user.getTenantDomain()));

            resultSet = prepStmt1.executeQuery();
            //Password cannot have been previously used in the past passwordHistoryTimeInHours (24) hours
            Timestamp historyValidationTime = new Timestamp(new java.util.Date().getTime() - passwordHistoryTimeInHours * 3600 * 1000l);
            while (resultSet.next()) {
                storedHistoryCount++;
                if (storedHistoryCount >= maxHistoryCount) {
                    //
                    recordsToDelete.add(resultSet.getInt("ID"));
                }

                if (recordsToDelete.size() > 0) {
                    Timestamp storedTimeStamp = resultSet.getTimestamp("TIME_CREATED");
                    if (storedTimeStamp.after(historyValidationTime)) {
                        recordsToDelete.clear();
                    }
                }
            }

            if (recordsToDelete.size() > 0) {
                for (int i = 0; i < recordsToDelete.size(); i++) {
                    prepStmt2 = connection.prepareStatement(
                            Constants.SQLQueries.DELETE_HISTORY_RECORD);
                    prepStmt2.setInt(1, recordsToDelete.get(i));
                    prepStmt2.execute();
                }
            }

            prepStmt3 = connection.prepareStatement(Constants.SQLQueries.STORE_HISTORY_DATA);
            prepStmt3.setString(1, user.getUserName());
            prepStmt3.setString(2, user.getUserStoreDomain().toUpperCase());
            prepStmt3.setInt(3, IdentityTenantUtil.getTenantId(user.getTenantDomain()));
            prepStmt3.setString(4, saltValue);
            prepStmt3.setString(5, preparePassword(credential.toString(), saltValue));
            prepStmt3.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
            prepStmt3.execute();
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new IdentityPasswordHistoryException("Error while rollback password history storing", e1);
            }
            throw new IdentityPasswordHistoryException("Error while storing password history", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt1);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
            IdentityDatabaseUtil.closeStatement(prepStmt3);
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public void remove(User user) throws IdentityPasswordHistoryException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(Constants.SQLQueries
                    .DELETE_USER_HISTORY);
            prepStmt.setString(1, user.getUserName());
            prepStmt.setString(2, user.getUserStoreDomain());
            prepStmt.setInt(3, IdentityTenantUtil.getTenantId(user.getTenantDomain()));
            prepStmt.execute();
            connection.commit();

        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while removing password history date from user :" +
                    user.getUserName(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }

    }

    @Override
    public boolean validate(User user, Object credential) throws IdentityPasswordHistoryException {
        //History not validate if password is empty
        if (credential == null) {
            return true;
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        int storedHistoryCount = 0;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        try {
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(Constants.SQLQueries
                    .LOAD_HISTORY_DATA);
            prepStmt.setString(1, user.getUserName());
            prepStmt.setString(2, user.getUserStoreDomain());
            prepStmt.setInt(3, IdentityTenantUtil.getTenantId(user.getTenantDomain()));

            resultSet = prepStmt.executeQuery();
            //Password cannot have been previously used in the past passwordHistoryTimeInHours(24) hours
            Timestamp historyValidationTime = new Timestamp(new java.util.Date().getTime() - passwordHistoryTimeInHours * 3600 * 1000l);

            while (resultSet.next()) {
                storedHistoryCount++;
                if (storedHistoryCount <= maxHistoryCount) {
                    if (isHistoryExists(resultSet.getString("SALT_VALUE"), resultSet.getString("HASH"), credential)) {
                        return false;
                    }
                } else {
                    Timestamp storedTimeStamp = resultSet.getTimestamp("TIME_CREATED");
                    if (storedTimeStamp.after(historyValidationTime) && isHistoryExists(resultSet.getString("SALT_VALUE"), resultSet.getString("HASH"), credential)) {
                        return false;
                    }
                }
            }

        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return true;
    }

    private boolean isHistoryExists(String saltValue, String storedPassword, Object credential) throws
            IdentityPasswordHistoryException {

        String password;
        password = this.preparePassword(credential.toString(), saltValue);
        if ((storedPassword != null) && (storedPassword.equals(password))) {
            return true;
        }
        return false;
    }

    /**
     * This private method returns a saltValue using SecureRandom.
     *
     * @return saltValue
     */
    private String generateSaltValue() {
        String saltValue;
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SHA_1_PRNG);
            byte[] bytes = new byte[16];
            //secureRandom is automatically seeded by calling nextBytes
            secureRandom.nextBytes(bytes);
            saltValue = Base64.encode(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1PRNG algorithm could not be found.");
        }
        return saltValue;
    }

    /**
     * @param password
     * @param saltValue
     * @return
     * @throws UserStoreException
     */
    private String preparePassword(String password, String saltValue) throws
            IdentityPasswordHistoryException {
        try {
            String digestInput = password;
            if (saltValue != null) {
                digestInput = password + saltValue;
            }

            if (digestFunction != null) {

                if (digestFunction.equals(UserCoreConstants.RealmConfig.PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
                    return password;
                }

                MessageDigest dgst = MessageDigest.getInstance(digestFunction);
                byte[] byteValue = dgst.digest(digestInput.getBytes());
                password = Base64.encode(byteValue);
            }
            return password;
        } catch (NoSuchAlgorithmException e) {
            String msg = "Error occurred while preparing password.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new IdentityPasswordHistoryException(msg, e);
        }
    }
}
