package com.holictechnology.kidfriendly.library.utilites;


import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class CriptographUtilities implements Serializable {

    private static final long serialVersionUID = 1623424675100052733L;
    private static final String PREFIX = "fRiEnDlY";
    private static final String SUFFIX = "KiD";

    private CriptographUtilities() {}

    /**
     * Return instance class
     * 
     * @return
     */
    public synchronized static CriptographUtilities getInstance() {
        return new CriptographUtilities();
    }

    /**
     * @param email
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String createToken(String email, String password) throws NoSuchAlgorithmException {
        return createToken(email.concat(password));
    }

    /**
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String createToken(String value) throws NoSuchAlgorithmException {
        return criptograph(PREFIX.concat(criptograph(value)).concat(SUFFIX));
    }

    /**
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String criptograph(String value) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte [] criptograph = messageDigest.digest(value.getBytes());

        return new BigInteger(1, criptograph).toString(16);
    }
}