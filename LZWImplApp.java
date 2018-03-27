package apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import io.BitSink;
import io.BitSource;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

/**
 * 
 * Author: Ramkumar Rao
 * Date: March 23, 2018
 * COMP 590 Project
 * The following program takes a input file, and runs it
 * through the classic LZW algorithm. The algorithm outputs
 * a compressed representation of the input file, as well
 * as a decoded version of the compressed file, which should
 * be the same as the original input file. This file is self
 * contained; there are no new external files which have been
 * added to the framework.
 *
 */
public class LZWImplApp {
	
	public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
		//Timing purposes
		long startTime = System.nanoTime();
		
		//Numbers used to initialize/update dictionary
		final int MAX_PIXEL_VALUE = 256;
		int runningCount = 256;
		
		//File selection
		String base = "tractor";
		String filename = "/Users/ramrao/Desktop/COMP590/" + base + ".450p.yuv";
		//String filename = "/Users/ramrao/Desktop/data.txt";
		//String filename = "/Users/ramrao/Desktop/test2.txt";
		File file = new File(filename);
		
		//Initialize dictionary
		InputStream message = new FileInputStream(file);
		Map<String, Integer> dictionary = new HashMap<String,Integer>();
		for (int j=0; j<MAX_PIXEL_VALUE; j++) {
			dictionary.put("" + (char)j, j);
		}
		
		//Select output file
		File out_file = new File("/Users/ramrao/Desktop/test-compressed.dat");
		OutputStream out_stream = new FileOutputStream(out_file);
		BitSink bit_sink = new OutputStreamBitSink(out_stream);
		
		//Encoding algorithm
		String w = "";
		String k = "";
		while (true) {
			int tmp = message.read();
			if (tmp < 0) {
				break;
			}
			k = "" + (char)tmp;
			if (dictionary.get(w + k) != null) {
				w = w + k;
			} else {
				dictionary.put(w + k, runningCount);
				runningCount++;
				//System.out.println(dictionary.get(w));
				bit_sink.write(String.format("%24s", Integer.toBinaryString(dictionary.get("" + w))).replace(' ', '0'));
				w = k;
			}
		}
		//System.out.println(dictionary.get(w));
		bit_sink.write(String.format("%24s", Integer.toBinaryString(dictionary.get("" + w))).replace(' ', '0'));
		
		//Timing Purposes
		long encodeTime = System.nanoTime();
		long howLongToEncode = encodeTime - startTime;
		double howLongToEncodeSecs = (double)howLongToEncode / 1000000000.0;
		System.out.println("Took " + howLongToEncodeSecs + " seconds to encode");
		
		//Initialize decoding dictionary
		Map<Integer, String> decodeDictionary = new HashMap<Integer, String>();
		for (int m=0; m<MAX_PIXEL_VALUE; m++) {
			decodeDictionary.put(m, "" + (char)m);
		}
		
		//Select decode file destination
		BitSource bit_source = new InputStreamBitSource(new FileInputStream(out_file));
		OutputStream decoded_file = new FileOutputStream(new File("/Users/ramrao/Desktop/test-decoded.dat"));
		
		//Decoding algorithm
		String l = "";
		String v = "";
		String entry = "";
		int runningCountDecode = 256;
		
		l = "" + bit_source.next(24);
		//System.out.println(decodeDictionary.get(Integer.parseInt(l)));
		decoded_file.write(decodeDictionary.get(Integer.parseInt(l)).charAt(0));
		v = "" + (char)Integer.parseInt(l);
		while (true) {
			try {
				l = "" + bit_source.next(24);
			} catch(io.InsufficientBitsLeftException e) {
				break;
			}
			if (decodeDictionary.get(Integer.parseInt(l)) != null) {
				entry = "" + decodeDictionary.get(Integer.parseInt(l));
				//System.out.println(entry);
				char[] entryArr = entry.toCharArray();
				for (int x=0; x<entryArr.length; x++) {
					decoded_file.write(entryArr[x]);
				}
				decodeDictionary.put(runningCountDecode, v + entry.charAt(0));
				runningCountDecode++;
				v = entry;
			} else {
				entry = v + v.charAt(0);
				//System.out.println(entry);
				char[] entryArr = entry.toCharArray();
				for (int x=0; x<entryArr.length; x++) {
					decoded_file.write(entryArr[x]);
				}
				decodeDictionary.put(runningCountDecode, entry);
				runningCountDecode++;
				v = entry;
			}
			
		}
		
		//Timing Purposes
		long decodeTime = System.nanoTime();
		long howLongToDecode = decodeTime - encodeTime;
		double howLongToDecodeSecs = (double)howLongToDecode / 1000000000.0;
		System.out.println("Took " + howLongToDecodeSecs + " seconds to decode");
	}
}
