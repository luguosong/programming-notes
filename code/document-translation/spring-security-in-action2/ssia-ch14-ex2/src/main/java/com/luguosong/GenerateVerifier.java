package com.luguosong;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateVerifier {

  public static void main(String[] args) {
    System.out.println(generateVerifier());
  }

  public static String generateVerifier() {
    SecureRandom secureRandom = new SecureRandom();
    byte [] code = new byte[32];
    secureRandom.nextBytes(code);
    String codeVerifier = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(code);

    return codeVerifier;
  }
}
