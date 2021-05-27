/**
 * @Author: RogerDTZ
 * @FileName: ByteUtil.java
 */

package util;

public class ByteUtil {

    public static byte[] merge(byte[] a, byte[] b, int begin, int end) {
        byte[] res = new byte[a.length + end - begin];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, begin, res, a.length, end - begin);
        return res;
    }

}
