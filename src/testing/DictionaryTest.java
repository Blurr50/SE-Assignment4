package testing;

import myhre.Dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DictionaryTest {

	@TempDir
	Path tempDir;

	// Writes text to a temp file and builds a Dictionary from it.
	private Dictionary build(String text) throws IOException {
		Path file = tempDir.resolve("words.txt");
		Files.write(file, text.getBytes(StandardCharsets.UTF_8));
		return new Dictionary(file.toString());
	}

	@Test
	void findsWordsThatWereInTheDocument() throws IOException {
		Dictionary d = build("The quick brown fox jumps over the lazy dog.");
		assertTrue(d.spellcheck("quick"));
		assertTrue(d.spellcheck("fox"));
		assertTrue(d.spellcheck("dog"));
		assertTrue(d.spellcheck("the"));
	}

	@Test
	void rejectsWordsNotInTheDocument() throws IOException {
		Dictionary d = build("The quick brown fox");
		assertFalse(d.spellcheck("cat"));
		assertFalse(d.spellcheck("quik"));   // misspelling
		assertFalse(d.spellcheck("qui"));    // prefix of a real word
		assertFalse(d.spellcheck("quickly")); // extension of a real word
	}

	@Test
	void spellcheckIsCaseInsensitive() throws IOException {
		Dictionary d = build("Hello WORLD");
		assertTrue(d.spellcheck("hello"));
		assertTrue(d.spellcheck("HELLO"));
		assertTrue(d.spellcheck("World"));
	}

	@Test
	void nullAndEmptyInputAreNotWords() throws IOException {
		Dictionary d = build("hello world");
		assertFalse(d.spellcheck(null));
		assertFalse(d.spellcheck(""));
		assertFalse(d.spellcheck("'"));
		assertFalse(d.spellcheck("   "));
	}

	@Test
	void duplicatesAreStoredOnce() throws IOException {
		Dictionary d = build("go Go GO go stop stop");
		assertEquals(2, d.size());
		assertTrue(d.spellcheck("go"));
		assertTrue(d.spellcheck("stop"));
	}

	@Test
	void punctuationAndNumbersActAsSeparators() throws IOException {
		Dictionary d = build("Hello, world! Well-known facts; 42 items...\nnew\tline");
		assertTrue(d.spellcheck("hello"));
		assertTrue(d.spellcheck("world"));
		assertTrue(d.spellcheck("well"));
		assertTrue(d.spellcheck("known"));
		assertTrue(d.spellcheck("line"));
		assertFalse(d.spellcheck("well-known"));
		assertFalse(d.spellcheck("42"));
		assertEquals(8, d.size()); // hello world well known facts items new line
	}

	@Test
	void apostropheInsideWordIsKept() throws IOException {
		Dictionary d = build("I don't know, it's fine.");
		assertTrue(d.spellcheck("don't"));
		assertTrue(d.spellcheck("it's"));
		assertFalse(d.spellcheck("don"));
		assertFalse(d.spellcheck("t"));
	}

	@Test
	void curlyApostrophesMatchStraightOnes() throws IOException {
		Dictionary d = build("I don\u2019t know");
		assertTrue(d.spellcheck("don't"));
		assertTrue(d.spellcheck("don\u2019t"));
	}

	@Test
	void surroundingQuotesAreTrimmed() throws IOException {
		Dictionary d = build("She said 'hello' to him");
		assertTrue(d.spellcheck("hello"));
		assertTrue(d.spellcheck("'hello'"));
		assertFalse(d.spellcheck("'hellox'"));
	}

	@Test
	void nonAsciiLettersStayInsideWords() throws IOException {
		Dictionary d = build("A caf\u00e9 serves na\u00efve r\u00e9sum\u00e9s");
		assertTrue(d.spellcheck("caf\u00e9"));
		assertTrue(d.spellcheck("CAF\u00c9"));
		assertTrue(d.spellcheck("na\u00efve"));
		assertFalse(d.spellcheck("caf"));
	}

	@Test
	void emptyFileGivesEmptyDictionary() throws IOException {
		Dictionary d = build("");
		assertEquals(0, d.size());
		assertFalse(d.spellcheck("anything"));
	}

	@Test
	void fileWithOnlyPunctuationGivesEmptyDictionary() throws IOException {
		Dictionary d = build("... !!! ??? 123 '' ---");
		assertEquals(0, d.size());
		assertFalse(d.spellcheck("123"));
	}

	@Test
	void sortedInputStillWorks() throws IOException {
		// Alphabetical order makes the tree a straight line (worst case)
		StringBuilder sb = new StringBuilder();
		for (char a = 'a'; a <= 'z'; a++) {
			for (char b = 'a'; b <= 'z'; b++) {
				sb.append(a).append(b).append(' ');
			}
		}
		Dictionary d = build(sb.toString());
		assertEquals(26 * 26, d.size());
		assertTrue(d.spellcheck("aa"));
		assertTrue(d.spellcheck("mm"));
		assertTrue(d.spellcheck("zz"));
	}

	@Test
	void missingFileThrowsIOException() {
		String missing = tempDir.resolve("does-not-exist.txt").toString();
		assertThrows(IOException.class, () -> new Dictionary(missing));
	}
}
