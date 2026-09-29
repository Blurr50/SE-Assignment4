package myhre;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Builds a Binary Search Tree of words from a text document and lets you check
 * whether a given word is in that tree.
 */
public class Dictionary {

	private Node root;
	private int size; // number of unique words

	/**
	 * Reads the file at the given path and inserts every word into the BST.
	 */
	public Dictionary(String filePath) throws IOException {
		String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		loadWords(text);
	}

	// standardizes all words into lowercase and ignore punctuation such as '
	private static String normalize(String s) {
		return s.toLowerCase(Locale.ROOT).replace('\u2019', '\'').replace('\u2018', '\'').replaceAll("^'+|'+$", "");
	}

	// Splits text into words and inserts each one into the tree.
	private void loadWords(String text) {
		// Split on anything that isn't a letter (any language) or an apostrophe
		String[] tokens = text.replace('\u2019', '\'').replace('\u2018', '\'').split("[^\\p{L}']+");
		for (String token : tokens) {
			String word = normalize(token);
			if (!word.isEmpty()) {
				insert(word);
			}
		}
	}

	// Iterative insert. Duplicates are ignored.
	private void insert(String word) {
		if (root == null) {
			root = new Node(word);
			size++;
			return;
		}

		Node current = root;
		while (true) {
			int cmp = word.compareTo(current.word);
			if (cmp == 0) {
				return; // already in the tree
			} else if (cmp < 0) {
				if (current.left == null) {
					current.left = new Node(word);
					size++;
					return;
				}
				current = current.left;
			} else {
				if (current.right == null) {
					current.right = new Node(word);
					size++;
					return;
				}
				current = current.right;
			}
		}
	}

	/**
	 * Returns true if s is one of the words in the tree
	 */
	public boolean spellcheck(String s) {
		if (s == null) {
			return false;
		}

		String target = normalize(s);
		if (target.isEmpty()) {
			return false;
		}

		Node current = root;
		while (current != null) {
			int cmp = target.compareTo(current.word);
			if (cmp == 0) {
				return true;
			} else if (cmp < 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}
		return false;
	}

	/** Number of unique words stored in the tree. */
	public int size() {
		return size;
	}

	// Quick demo: java myhre.Dictionary <path-to-text-file> <word-to-check>
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java myhre.Dictionary <textfile> <word>");
			return;
		}
		try {
			Dictionary dict = new Dictionary(args[0]);
			System.out.println("Loaded " + dict.size() + " unique words.");
			System.out.println("\"" + args[1] + "\" is "
					+ (dict.spellcheck(args[1]) ? "spelled correctly." : "not in the dictionary."));
		} catch (IOException e) {
			System.out.println("Could not read file: " + e.getMessage());
		}
	}
}
