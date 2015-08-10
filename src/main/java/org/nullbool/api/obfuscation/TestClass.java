package org.nullbool.api.obfuscation;

import java.util.Random;

/**
 * Created by polish on 17.05.15.
 */
public class TestClass {

    static int ifElse(int i) {
        boolean b = i * 3232 == 3;
        if (b) {
            i = 3283278;
        } else {
            i = 0;
        }
        return i;
    }

    int ternaryOOP(int var1) {
        boolean b = var1 * 3232 == 3;
        var1 = b ? 3283278 : ternaryOOP(var1 == 0 ? 1 : var1 == 3 ? 1 : 2 == (var1 == 4 ? 1 : 2) ? var1 == 5 ? 1 : 2 == (var1 == 7 ? 1 : 2) ? 1 : 1 == (var1 == 1 ? 3 : 22) ? var1 == 0 ? 1 : 2 == 0 ? 1 : var1 == 0 ? 1 : 2 : 2 : var1 == 0 ? 1 : 2 == 0 ? 1 : 2 == 0 ? var1 == 0 ? 1 : 2 : 2);
        return var1;
    }
    
    static void method796(int[] var0, int[] var1, int[] var2, byte[] var3, int var4, int var5, int var6) {
        int var7 = 0;

        int var9;
        for(var9 = var4; var9 <= var5; ++var9) {
            for(int var10 = 0; var10 < var6; ++var10) {
                if(var3[var10] == var9) {
                    var2[var7] = var10;
                    ++var7;
                }
            }
        }

        for(var9 = 0; var9 < 23; ++var9) {
            var1[var9] = 0;
        }

        for(var9 = 0; var9 < var6; ++var9) {
            ++var1[var3[var9] + 1];
        }

        for(var9 = 1; var9 < 23; ++var9) {
            var1[var9] += var1[var9 - 1];
        }

        for(var9 = 0; var9 < 23; ++var9) {
            var0[var9] = 0;
        }

        int var8 = 0;

        for(var9 = var4; var9 <= var5; ++var9) {
            var8 += var1[var9 + 1] - var1[var9];
            var0[var9] = var8 - 1;
            var8 <<= 1;
        }

        for(var9 = var4 + 1; var9 <= var5; ++var9) {
            var1[var9] = (var0[var9 - 1] + 1 << 1) - var1[var9];
        }

    }
    public boolean am(int e,int[] q) {
        return e < 0 || e >= q.length << 8;
    }

    public static Object e(int var0, int[] var3) {

        for (int var4 = 0; var4 < var3.length; ++var4) {
            int var5 = var3[var4];
            if (var0 == -1429148917 * var5) {
                return var5;
            }
        }

        return null;
    }

    static boolean test_2(boolean v1, boolean v2) {
        return !test_2(333, v2) & !test_2(32, v2);
    }

    static boolean test_2(int i, boolean v2) {
        return !v2;
    }

    static void lookupSwitch(int i) {
        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 100:
                System.out.println("1");
                break;
            case 1000:
                System.out.println("2");
                break;
            default:
                System.out.println("def");
                break;
        }
    }

    static void tableSwitch(String d, Random random) {
        switch (d) {
            case "0":
                System.out.println("0");
                break;
            case "1":
                for (int i = 0; i < 321; i++) {
                    for (int j = 0; j < 3123; j++) {
                        for (int k = 0; k < (j = random.nextInt()); k++) {
                            System.out.println();
                            while (j < random.nextInt()) {
                                System.out.println("");
                            }
                        }
                    }
                }
                System.out.println("1");
                break;
            case "2":
                System.out.println("2");
                break;
            default:
                System.out.println("def");
                break;
        }
    }

    public synchronized int t(int c) {
        return c < 0 ? -1 : c;
    }

    int e(int var1, int var2, int var3) {
        return -1798405177671744557L * var1 < -1798405177671744557L * var2 ? -1 : (-1798405177671744557L * var2 == var1 * -1798405177671744557L ? 0 : 1);
    }

    int a(byte var1, int b, Object g, int[] o, int al, int q, Object[] ao) {
        for (int i = 0; i < 1337; i++) {
            for (int j = 0; j < (q = j) + ((i = j * 2)) + 22; j++) {

            }
        }
        System.out.println("LOOP END");
        if (-1 == 970437277 * al) {
            al = (q * -1945821617 != 22 ? 1337 : 133337) * -2141428811;
        }
        return al * b;
    }


    int test(int var1, Random random) {
        for (int i = 0; i < 321; i++) {
            for (int j = 0; j < 3123; j++) {
                for (int k = 0; k < (j = random.nextInt()); k++) {
                    System.out.println();
                    while (j < random.nextInt()) {
                        System.out.println("");
                    }
                }
            }
        }
        return var1;
    }
}

