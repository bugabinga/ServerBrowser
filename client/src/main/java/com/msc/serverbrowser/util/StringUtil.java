package com.msc.serverbrowser.util;

public class StringUtil
{
	/**
	 * Puts <code>http://</code> infront of the url if not it already has <code>http://</code> or
	 * <code>https://</code> infront of it.
	 *
	 * @param url
	 *            the url to fix
	 * @return the fixed url or the original if there was no need to fix
	 */
	public static String fixUrlIfNecessary(final String url)
	{
		String website = url;
		if (!website.startsWith("http://") || !website.startsWith("https://"))
		{
			website = "http://" + website;
		}
		return website;
	}

	public static String getHexChars(final String string)
	{
		final char[] chars = string.toCharArray();

		final StringBuilder charsAsHex = new StringBuilder();

		charsAsHex.append("Number of chars: (" + chars.length + ") ");

		for (final char character : chars)
		{
			charsAsHex.append(Integer.toHexString(character));
			charsAsHex.append(" ");
		}

		return charsAsHex.toString();
	}

	/**
	 * Checks if a {@link String} conforms to the uri format.
	 *
	 * @param possibleUrl
	 *            the {@link String} to check.
	 * @return true if it was valid and false otherwise
	 */
	public static boolean isValidURL(final String possibleUrl)
	{
		if (possibleUrl == null)
		{
			return false;
		}

		return possibleUrl
				.matches("^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:/[^\\s]*)?$");
	}
}
