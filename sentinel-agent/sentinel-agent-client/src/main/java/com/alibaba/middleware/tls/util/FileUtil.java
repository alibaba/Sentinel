package com.alibaba.middleware.tls.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public class FileUtil {

	public static File writeStringToFile(String privateKeyEncryptPath, String data,
			Charset encoding)
			throws IOException {

		File file = null;
		OutputStream out = null;

		try {
			file = new File(privateKeyEncryptPath);
			out = openOutputStream(file);
			write(data, out, encoding);
			out.close();
		} finally {
			closeQuietly(out);
		}
		return file;
	}

	public static void write(String data, OutputStream output, Charset encoding) 
			throws IOException {

		if (data != null) {
			output.write(data.getBytes(encoding));
		}

	}

	public static void closeQuietly(Closeable closeable) {
		try {

			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	public static FileOutputStream openOutputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if ((parent != null) && (!parent.mkdirs()) && (!parent.isDirectory())) {
				throw new IOException("Directory '" + parent + "' could not be created");
			}
		}
		return new FileOutputStream(file);
	}

//	public static String readFile7(String path, Charset encoding) throws IOException
//
//	{
//		byte[] encoded = Files.readAllBytes(Paths.get(path));
//		return new String(encoded, encoding);
//	}

	public static String readFile6(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			return stringBuilder.toString();
		} finally {
			closeQuietly(reader);
		}

	}

	public static File writeBytesToFile(byte[] byteArray, String path) throws IOException  {
		FileOutputStream fileOuputStream = null;
		File file=new File(path);
		try {
			fileOuputStream=openOutputStream(file);
			fileOuputStream.write(byteArray);
		} finally {
			closeQuietly(fileOuputStream);
		}
		return file;
	}
	
	
	public static byte[] readBytesFromFile(String filePath) throws IOException {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } finally {
        	closeQuietly(fileInputStream);

        }

        return bytesArray;

    }

	public static Reader byteToInputStream(byte[] bytes) {
		
	     ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	     InputStreamReader inputStreamReader=new InputStreamReader(bis);
	     return inputStreamReader;
	}
	
	
    
}
