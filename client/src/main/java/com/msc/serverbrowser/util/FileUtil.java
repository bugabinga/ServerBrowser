package com.msc.serverbrowser.util;

import static java.io.File.separator;
import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.msc.serverbrowser.logging.Logging;

/**
 * Util methods for dealing with downloading and unzipping files.
 *
 * @author oliver
 * @author Marcel
 * @since 01.07.2017
 */
public class FileUtil
{
	/**
	 * Downloads a file and saves it to the given location.
	 *
	 * @param url
	 *            the url to download from
	 * @param outputPath
	 *            the path where to save the downloaded file
	 * @return the downloaded file
	 * @throws IOException
	 *             if an errors occurs while writing the file or opening the stream
	 */
	public static File downloadFile(final String url, final String outputPath) throws IOException
	{
		try (final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
				final FileOutputStream fileOutputStream = new FileOutputStream(outputPath);)
		{
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			return new File(outputPath);
		}
	}

	/**
	 * Unzips a file.
	 *
	 * @param zipFilePath
	 *            input zip file
	 * @param outputLocation
	 *            zip file output folder
	 * @throws IOException
	 *             if there was an error reading the zip file or writing the unzipped data
	 */
	public static void unzip(final String zipFilePath, final String outputLocation) throws IOException
	{
		// Open the zip file
		try (final ZipFile zipFile = new ZipFile(zipFilePath);)
		{
			final Enumeration<? extends ZipEntry> enu = zipFile.entries();
			while (enu.hasMoreElements())
			{
				final ZipEntry zipEntry = enu.nextElement();

				final String name = zipEntry.getName();
				final long size = zipEntry.getSize();
				final long compressedSize = zipEntry.getCompressedSize();

				Logging.logger().fine(() -> format("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize));

				// Do we need to create a directory ?
				final File file = new File(outputLocation + separator + name);
				if (name.endsWith("/"))
				{
					file.mkdirs();
					continue;
				}

				final File parent = file.getParentFile();
				if (parent != null)
				{
					parent.mkdirs();
				}

				// Extract the file
				try (final InputStream inputStream = zipFile.getInputStream(zipEntry);
						final FileOutputStream outputStream = new FileOutputStream(file);)
				{
					/*
					 * The buffer is the max amount of bytes kept in RAM during any given time while
					 * unzipping. Since most windows disks are aligned to 4096 or 8192, we use a
					 * multiple of those values for best performance.
					 */
					final byte[] bytes = new byte[8192];
					int length;
					while ((length = inputStream.read(bytes)) >= 0)
					{
						outputStream.write(bytes, 0, length);
					}
				}
			}
		}
	}
}
