package org.apache.lucene.analysis.kr.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.WordEntry;

public class TSTreeCompiler {

	public void compile(File input, String charset, File output) throws IOException {
		TSTree tst = new TSTree();
		BufferedReader brDictionary = null;
		try {
			brDictionary = new BufferedReader(new InputStreamReader(new FileInputStream(input), charset));
			addSubDictionary(brDictionary, tst);
			tst.saveToFile(output);
		}
		catch ( FileNotFoundException e)
		{
			System.out.println("Input file Path is not valid");
		}
		finally {
			if (brDictionary != null) {
				try {
					brDictionary.close();
				} catch (Exception e) {

				}
			}
		}

	}

	public void compile(File[] inputList, String charset, File output) throws IOException {

		TSTree tst = new TSTree();
		BufferedReader reader = null;
		for (int i = 0; i < inputList.length; i++) {
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputList[i]), charset));
				addSubDictionary(reader, tst);
			} finally {
				reader.close();
			}
		}
	}

	private void addSubDictionary(BufferedReader br, TSTree tst) throws IOException {
		String line = "";
		while (true) {
			line = br.readLine();
			
			if (line == null){
				break;
			}

			String[] infos = StringUtil.split(line, ",");
			if (infos.length != 2){
				continue;
			}
			
			infos[1] = infos[1].trim();
			if (infos[1].length() == 6){
				infos[1] = infos[1].substring(0, 5) + "000" + infos[1].substring(5);
			}

			WordEntry entry = new WordEntry(infos[0].trim(), infos[1].trim().toCharArray());
			tst.put(entry.getWord(), entry);
		}
	}

	private static List compoundArrayToList(String source, String[] arr) {
		List list = new ArrayList();
		for (String str : arr) {
			CompoundEntry ce = new CompoundEntry(str);
			ce.setOffset(source.indexOf(str));
			list.add(ce);
		}
		return list;
	}

	private void addCompound(BufferedReader br, TSTree tst) {
		String line = "";
		while (true) {
			try {
				line = br.readLine();
			} catch (Exception e) {
				continue;
			}
			if (line == null)
				break;

			String[] infos = StringUtil.split(line, ":");
			if (infos.length != 2)
				continue;
			WordEntry entry = new WordEntry(infos[0].trim(), "20000000X".toCharArray());
			entry.setCompounds(compoundArrayToList(infos[1], StringUtil.split(infos[1], ",")));
			tst.put(entry.getWord(), entry);
		}
	}
}
