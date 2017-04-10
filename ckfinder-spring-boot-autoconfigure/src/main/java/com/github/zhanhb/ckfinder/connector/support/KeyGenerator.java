package com.github.zhanhb.ckfinder.connector.support;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public enum KeyGenerator {
  INSTANCE;

  private final char[] chars;
  private final int[] index;

  KeyGenerator() {
    // Character set used in license key
    final String str = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    this.chars = str.toCharArray();
    int[] arr = new int['Z' - '1' + 1];
    Arrays.fill(arr, -1);
    for (int i = 0, len = str.length(); i < len; ++i) {
      arr[str.charAt(i) - '1'] = i;
    }
    this.index = arr;
  }

  private int nextInt(int n) {
    return ThreadLocalRandom.current().nextInt(n);
  }

  public int indexOf(char ch) {
    try {
      return index[ch - '1'];
    } catch (IndexOutOfBoundsException ex) {
      return -1;
    }
  }

  int r(int div, int rem, int lim) {
    return nextInt((lim - rem + div - 1) / div) * div + rem;
  }

  public String generateKey(String licenseName, int len, boolean host) {
    if (len < 26) {
      throw new IllegalArgumentException();
    }
    char[] licenseKey = new char[len];
    int charsLength = chars.length;

    // Create a 34-character random license key
    for (int i = 0; i < len; i++) {
      licenseKey[i] = chars[nextInt(charsLength)];
    }

    // Important characters: 0, 3, 12, 25
    /*
     * ----------------------------------------------
     * Create the 0th character
     * ----------------------------------------------
     * The letter:
     * The characters in the character set that have the sequence number when divided by 5 will be left 4(non host) or 1(host).
     */
    licenseKey[0] = chars[r(5, host ? 1 : 4, charsLength)];

    /*
     * ----------------------------------------------
     * Create a 3rd character
     * ----------------------------------------------
     * The letter:
     * Check where the first character is located in the character set. Then
     * plus the length of the license name. Then multiply by 9, then divide the
     * left bit translation by 2 and 4 by taking the remainder => The position
     * of the third character in the character set.
     */
    licenseKey[3] = chars[(licenseName.length() + indexOf(licenseKey[1])) * 9 % 32];

    /*
     * ----------------------------------------------
     * Create the 12th character
     * ----------------------------------------------
     * The letter:
     * Get the position in the character set of the 11th character plus the
     * position in the character set of the 8th character and multiply by 9,
     * then divide by the length of the character set minus 1 => the position of
     * the character. 12th in the character set
     */
    licenseKey[12] = chars[(indexOf(licenseKey[11]) + indexOf(licenseKey[8])) * 9 % 32];

    /*
     * ----------------------------------------------
     * Create the 25th character
     * ----------------------------------------------
     * Formula:
     * Make the characters in the character set that have a sequence number
     * when divided by 8 will have a balance of 7.
     */
    licenseKey[25] = chars[r(8, 7, charsLength)];

    return new String(licenseKey);
  }

  public String generateKey(String licenseName, boolean host) {
    return generateKey(licenseName, 34, host);
  }

}
