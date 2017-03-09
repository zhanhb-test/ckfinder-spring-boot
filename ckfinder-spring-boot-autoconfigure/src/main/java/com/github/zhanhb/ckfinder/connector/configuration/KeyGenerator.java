package com.github.zhanhb.ckfinder.connector.configuration;

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

  private int indexOf(char ch) {
    return index[ch - '1'];
  }

  public String generateKey(boolean host, String licenseName, int len) {
    if (len < 26) {
      throw new IllegalArgumentException();
    }
    char[] licenseKey = new char[len];

    // Create a 34-character random license key
    for (int i = 0; i < len; i++) {
      licenseKey[i] = chars[nextInt(chars.length)];
    }

    // Important characters: 0, 3, 12, 25
    /*
     * ----------------------------------------------
     * Create the 0th character
     * ----------------------------------------------
     * The letter:
     * The characters in the character set that have the sequence number when divided by 5 will be left 4(non host) or 1(host).
     */
    int[] zeroCharsIndex = host ? new int[]{1, 6, 11, 16, 21, 26, 31} : new int[]{4, 9, 14, 19, 24, 29};
    licenseKey[0] = chars[zeroCharsIndex[nextInt(zeroCharsIndex.length)]];

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
    int[] twentyFiveChars = {7, 15, 23, 31};
    licenseKey[25] = chars[twentyFiveChars[nextInt(twentyFiveChars.length)]];

    return new String(licenseKey);
  }

}
