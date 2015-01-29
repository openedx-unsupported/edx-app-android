package org.edx.utils;

public class StringUtils {

	/**
	 * Check for android version
	 * 
	 * @param version1
	 * @param version2
	 * @return str1>str2 1 str1=str2 0 str1<str2 -1
	 */
	public static int compareAndroidVersion(String version1, String version2) {
		String[] vals1 = version1.split("\\.");
		String[] vals2 = version2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version
		// string
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(
					Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
}
