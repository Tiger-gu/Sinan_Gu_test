package utils;
import java.util.ArrayList;

public class StringUtilities {

    /**
     * @param v - the version string
     * @return a list of integers as the result of parsing.
     */
    private static ArrayList<Integer> parseAndValidate(String v) {
        // initialize
        String[] temp = v.split("\\.");
        ArrayList<Integer> result = new ArrayList<Integer>();
        IllegalArgumentException e = 
            new IllegalArgumentException("Invalid version format");

        // validate and parse
        for (int i = 0; i < temp.length; i++) {
            try {
                result.add(Integer.parseInt(temp[i].trim()));
                if (result.get(i) < 0) throw e;
            } catch(NumberFormatException nfe) {
                throw e;
            }
        }
        return result;
    }

    /**
     * @param version1 - the first version string
     * @param version2 - the second version string
     * @return 0 if version1 == version2; 1 if version1 > version2; otherwise, -1
     */
    public static int versionCompare(String version1, String version2) {
        // Validate and parse version strings to arraylists of integers
        // E.g. "1.1.1" will be parse into [1, 1, 1]
        ArrayList<Integer> version1_arr = parseAndValidate(version1.trim());
        ArrayList<Integer> version2_arr = parseAndValidate(version2.trim());

        ArrayList<Integer> shortVersion;
        ArrayList<Integer> longVersion;
        int result;
        
        // Determine which version is long version and which is short version
        if (version1_arr.size() <= version2_arr.size()) {
            shortVersion = version1_arr;
            longVersion = version2_arr;
            // this is for the potential extra nums loop
            // In the case that version2 is longer, if any
            // extra num is found to be greater than 0,
            // since version1 < version2, then the result will be -1.
            result = -1;
        } else {
            shortVersion = version2_arr;
            longVersion = version1_arr;
            // this is for the potential extra nums loop
            // In the case that version1 is longer, if any
            // extra num is found to be greater than 0,
            // since version1 > version2, then the result will be 1.
            result = 1;

        }
                        
        // Loop through both version lists at the same time
        // Similar to numeric comparisons, we scan the digits
        // from left to right, while making comparisons. 
        for (int i = 0; i < shortVersion.size(); i++) {
            if (version1_arr.get(i) < version2_arr.get(i)) {
                return -1;
            } else if (version1_arr.get(i) > version2_arr.get(i)) {
                return 1;
            }
        }

        // if this point is reached, we know all the previous comparisons result in equal. 
        // We now just have to look at extra nums if there is any.
        // If all of the extra nums are zero, we will assume versions are the same.
        // (e.g 2.3.1 will be equal to 2.3.1.0.0.0.0)
        // if any of the extra nums are not zero, we will assume versions are not equal.
        // (e.g. 2.3.1 will be smaller than 2.3.1.0.1.0.0)

        // Then we look at all the extra nums
        for (int i = shortVersion.size(); i < longVersion.size(); i++) {
            if (longVersion.get(i) != 0) {
                return result;
            }
        }  
        return 0;
    }
}
