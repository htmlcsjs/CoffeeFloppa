package gregtech.api.util;

import static gregtech.api.GTValues.V;


// From https://github.com/GregTechCEu/GregTech/blob/master/src/main/java/gregtech/api/util/GTUtility.java, Licensed under LGPL
public class SusGTUtility {
    public static byte getTierByVoltage(long voltage) {
        byte tier = 0;
        while (++tier < V.length) {
            if (voltage == V[tier]) {
                return tier;
            } else if (voltage < V[tier]) {
                return (byte) Math.max(0, tier - 1);
            }
        }
        return (byte) Math.min(V.length - 1, tier);
    }
}
