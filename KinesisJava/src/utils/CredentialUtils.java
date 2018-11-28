/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;


/**
 *
 * @author João Pedro Toledo
 */
public class CredentialUtils {

    public static AWSCredentialsProvider getCredentialsProvider() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default] credential profile by
         * reading from the credentials file located at (~/.aws/credentials).
         */
        AWSCredentialsProvider credentialsProvider = null;
        try {
            credentialsProvider = new ProfileCredentialsProvider("default");
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Não foi possível carregar as credenciais. ",
                    e);
        }
        return credentialsProvider;
    }

}
