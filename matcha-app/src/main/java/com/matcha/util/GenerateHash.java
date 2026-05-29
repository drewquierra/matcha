package com.matcha.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * GenerateHash — Developer utility for producing BCrypt password hashes.
 *
 * Run this class to generate a fresh BCrypt hash for a given password.
 * Useful for regenerating the db/schema.sql seed insert if needed.
 *
 * Usage (from project root):
 *   mvn compile exec:java -Dexec.mainClass=com.matcha.util.GenerateHash -Dexec.args="matchamama"
 *
 * Or without arguments to hash the default seed password:
 *   mvn compile exec:java -Dexec.mainClass=com.matcha.util.GenerateHash
 */
public class GenerateHash {

    public static void main(String[] args) {
        String password = (args.length > 0) ? args[0] : "matchamama";
        int workFactor  = 10;

        System.out.println("Generating BCrypt hash (work factor " + workFactor + ") for: [" + password + "]");
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(workFactor));
        System.out.println("\nGenerated hash:\n" + hash);
        System.out.println("\nVerification test: " + BCrypt.checkpw(password, hash));
        System.out.println("\nSQL seed update command:");
        System.out.println("UPDATE users SET password_hash = '" + hash + "' WHERE username = 'drewquierra';");
    }
}
