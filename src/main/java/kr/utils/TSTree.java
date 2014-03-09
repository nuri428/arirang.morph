package org.apache.lucene.analysis.kr.utils;

import java.io.File;
import java.util.Vector;

import org.apache.lucene.analysis.kr.morph.WordEntry;

public class TSTree {

	private TSTreeNode root;
	private char keyArr[] = new char[100]; // 키 값의 길이는 100자까지 허용한다.
	
	private TSTreeNode pool; // 풀에서 현재 위치를 가리킨다.
	private int remain; // 현재 풀에 남아 있는 노드의 개수
	private int length;
	private int maxLength;
	private int nodeCount;
	private Vector<WordEntry> v;
	private Vector<WordEntry> valueList;

	public TSTree() {
		nodeCount = 0;
		v = new Vector<WordEntry>();
		valueList = new Vector<WordEntry>();
		createPool(100);
		remain = 100;
		maxLength = 0;
	}

	public TSTree(int poolSize) {
		nodeCount = 0;
		v = new Vector<WordEntry>();
		valueList = new Vector<WordEntry>();
		createPool(poolSize);
		remain = poolSize;
		maxLength = 0;
	}

	private void createPool(int poolSize) {
		TSTreeNode temp;
		for (int i = poolSize; i > 0; i--) {
			temp = pool;
			pool = new TSTreeNode(' ');
			pool.equal = temp;
		}
		length = 0;
	}

	// 풀에서 하나의 노드를 읽어온다.
	private TSTreeNode newNode(char c) {
		if (remain == 0) {
			nodeCount++;
			return new TSTreeNode(c);
		}

		TSTreeNode ret = pool;
		pool = pool.equal; // 풀에 있는 다음 객체를 가리킴.
		ret.equal = ret.high = ret.low = null;
		ret.splitChar = c;
		remain--; // 풀에 남아 있는 객체의 개수 1 감소.
		nodeCount++;
		return ret;
	}

	public WordEntry get(String key) {
		if (root == null)
			return null;

		if (key.length() > maxLength)
			return null;

		key.getChars(0, key.length(), keyArr, 0);
		return root.search(keyArr, 0, key.length() - 1);
	}

	public Vector<WordEntry> prefixMatch(String prefix) {
		if (prefix.isEmpty())
			return new Vector<WordEntry>();

		if (prefix.length() > maxLength)
			return new Vector<WordEntry>();

		Vector<WordEntry> queue = new Vector<WordEntry>();
		prefix.getChars(0, prefix.length(), keyArr, 0);
		TSTreeNode node = root.search1(keyArr, 0, prefix.length() - 1);
		if (node == null)
			return queue;
		if (node.value != null)
			queue.add(node.value);
		collect(node.equal, prefix, queue);
		return queue;
	}

	

	private void collect(TSTreeNode x, String prefix, Vector<WordEntry> queue) {
		if (x == null)
			return;

		collect(x.low, prefix, queue);

		if (x.value != null)
			queue.add(x.value);
		collect(x.equal, prefix + x.splitChar, queue);
		collect(x.high, prefix, queue);
	}


	public WordEntry put(String key, WordEntry value) {
		if (key == null)
			return null;
		key = key.trim();
		key.getChars(0, key.length(), keyArr, 0);
		if (root == null)
			root = newNode(keyArr[0]);
		length++;
		v.add(value);
		if (maxLength < key.length())
			maxLength = key.length();
		return root.insert(keyArr, 0, key.length() - 1, value);
	}

	
	
	

//	public void printInfo() {
//		System.out.println("Node  Count : " + nodeCount);
//		if (v != null)
//			System.out.println("value Count : " + v.size());
//		else
//			System.out.println("value Count : " + valueList.size());
//	}

	public void balance() {
		if (v != null) {
			nodeCount = 0;
			valueList.clear();
			valueList.addAll(v);
			root = null;
			insertBalanced(0, v.size());
			v = null;
		}
	}

	private void insertBalanced(int offset, int n) {
		int m;
		if (n < 1)
			return;
		m = n >> 1;

		insertBalanced(offset, m);
		insertBalanced(offset + m + 1, n - m - 1);
	}

	class TSTreeNode {

		char splitChar;
		TSTreeNode low;
		TSTreeNode equal;
		TSTreeNode high;
		WordEntry value;

		public WordEntry search(char key[], int index, int lastIndex) {
			char c;
			for (TSTreeNode node = this; node != null;) {
				if ((c = key[index]) < node.splitChar)
					node = node.low;
				else if (c == node.splitChar) {
					if (index == lastIndex)
						return node.value;
					index++;
					node = node.equal;
				} else
					node = node.high;
			}
			return null;
		}

		public TSTreeNode search1(char key[], int index, int lastIndex) {
			char c;
			for (TSTreeNode node = this; node != null;) {
				if ((c = key[index]) < node.splitChar)
					node = node.low;
				else if (c == node.splitChar) {
					if (index == lastIndex)
						return node;
					index++;
					node = node.equal;
				} else
					node = node.high;
			}
			return null;
		}

		public WordEntry insert(char key[], int index, int lastIndex, WordEntry value) {
			TSTreeNode node = this;
			char c;
			while (true) {
				if ((c = key[index]) < node.splitChar) {
					if (node.low == null)
						node.low = newNode(c);
					node = node.low;
				} else if (c == node.splitChar) {
					if (index == lastIndex) {
						WordEntry old = node.value;
						node.value = value;
						return old;
					}
					index++;
					c = key[index];
					if (node.equal == null)
						node.equal = newNode(c);
					node = node.equal;
				} else {
					if (node.high == null)
						node.high = newNode(c);
					node = node.high;
				}
			}
		}

		public TSTreeNode(char c) {
			splitChar = c;
		}
	}

	// test client
	public static void main(String[] args) {

	}

	public void saveToFile(File output) {
		// TODO Auto-generated method stub
		// save to file 구현 필요. 
	}

}
