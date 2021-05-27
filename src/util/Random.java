/**
 * @Author: RogerDTZ
 * @FileName: Random.java
 */

package util;

public class Random {

    private static java.util.Random random = new java.util.Random();

    public static int nextInt(int bound) {
        return nextInt(0, bound);
    }

    public static int nextInt(int low, int high) {
        return low + random.nextInt(high - low);
    }

    public static double nextDouble(double low, double high) {
        return low + random.nextDouble() * (high - low);
    }

    public static char nextChar() {
        int x = nextInt(26 + 26 + 10);
        if (x < 26)
            return (char)('a' + x);
        else if (x < 52)
            return (char)('A' + x - 26);
        else
            return (char)('0' + x - 52);
    }

    public static String randomString(int length) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; ++i)
            res.append(nextChar());
        return res.toString();
    }

}
