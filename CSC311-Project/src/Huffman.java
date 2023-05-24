import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class Huffman {

	public static void main(String[] args) {
		// Scanner to read input from the user.
		Scanner input = new Scanner(System.in);
		// Scanner to read input from file.
		Scanner sc = null;
		String choice;
		String textFile = "";
		int oSize;
		int sizeAfterCom;
		
		char[] arrChar;
		int temp;
		// In m I will store char and its frequency.
		Map<Character, Integer> m = new HashMap<Character, Integer>();

		System.out.println("Enter in the file name.");
		String userFile = input.nextLine();

		// Scans the file and counts the freq.
		try {
			sc = new Scanner(new File(userFile));
			while (sc.hasNext()) {
				String line = sc.nextLine();
				arrChar = new char[line.length()];
				// loop through all characters in the text.
				for(int i = 0; i < line.length(); i++) {
					arrChar[i] = line.charAt(i);
					// check if character already exist,
					// increase its frequency.
					if(m.containsKey(arrChar[i])) {
						temp = m.get(arrChar[i]);
						temp++;
						m.put(arrChar[i], temp);
					}else // otherwise add it to HashMap
						m.put(arrChar[i], 1);
				}
				textFile += line;
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found, exiting program");
			System.exit(0);
		} finally {
			sc.close();
		}
		
		// root of the Huffman tree.
		final HuffmanNode root = buildTree(m);
		// charCode contains codeword for each character.
		final Map<Character, String> charCode = generateCodes(root);

		final String encodedMessage = encodeMessage(charCode, textFile);

		// Menu for the user to select the output
		while (true) {
			System.out.println(" _______________________________");
			System.out.println("|           Menu                |");
			System.out.println("|      Enter in a char          |");
			System.out.println("|===============================|");
			System.out.println("|a = The Huffman tree.          |");
			System.out.println("|b = The Huffman tree graphviz. |");
			System.out.println("|c = The Code Table.            |");
			System.out.println("|d = The Encoded text.          |");
			System.out.println("|e = The Decoded text.          |");
			System.out.println("|f = The Compression rate.      |");
			System.out.println("|g = Exit the program.          |");
			System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			choice = input.next();

			// The Huffman tree.
			if (choice.equals("a")) {
				System.out.println(" ");
				printBinaryTree(root);
				System.out.println(" ");
			}
			// The Huffman tree graphviz.
			else if(choice.equals("b"))
				graphvizTree(root);
			// Code table.
			else if (choice.equals("c")) {
				System.out.println("\nChar| Huffman code ");
				System.out.println("--------------------");
				printCode(root, "");
				System.out.println(" ");
			}
			// Encoded text.
			else if (choice.equals("d")) 
				System.out.println("\nEncoded text: " + encodedMessage + "\n");
			// Decoded text.
			else if (choice.equals("e"))
				System.out.println("\nDecoded text: " + decodeMessage(encodedMessage, charCode) + "\n");
			// Compression rate.
			else if(choice.equals("f")) {
				oSize = textFile.length() * 8;
				sizeAfterCom = calcSize(charCode, m);
				System.out.println("Size before compression: "+oSize+" bits");
				System.out.println("Size after Compression: "+sizeAfterCom+ " bits");
				float rate = 100-sizeAfterCom/(float)oSize*100;
				System.out.println("Data compression rate: "+ String.format("%.2f", rate)+"%");
			}
			// Exit the program.
			else if (choice.equals("g")) {
				System.out.println("\nExiting");
				input.close();
				System.exit(0);
			}
			else {
				System.out.println("Wrong input");
			}
		}
	}

	private static class HuffmanNode {

		char c;
		int frequency;
		HuffmanNode leftChild;
		HuffmanNode rightChild;

		HuffmanNode(char c, int freq, HuffmanNode leftChild, HuffmanNode rightChild) {
			this.c = c;
			this.frequency= freq;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
		}
		
		boolean isLeaf() {
			return leftChild == null && rightChild == null;
		}
	}

	// Implement a comparator to compare the frequency count
	private static class HuffmanComparator implements Comparator<HuffmanNode> {
		@Override
		public int compare(HuffmanNode node1, HuffmanNode node2) {
			return node1.frequency - node2.frequency;
		}
	}
	
	// Calculte size of text after compression
	private static int calcSize(Map<Character, String> charCode, Map<Character, Integer> m) {
		int size = 0;
		for(Entry<Character, String> entry : charCode.entrySet())
			size = size + (entry.getValue().length() * m.get(entry.getKey()));
		return size;
	}

	// Implements a Priority Queue
	private static Queue<HuffmanNode> createNodeQueue(Map<Character, Integer> m) {
		final Queue<HuffmanNode> pq = new PriorityQueue<HuffmanNode>(new HuffmanComparator());
		char temp;
		int t;
		for(Entry<Character, Integer> entry : m.entrySet()) {
			temp = entry.getKey().charValue();
			t = entry.getValue().intValue();
			pq.add(new HuffmanNode(temp, t, null, null));
		}
		return pq;
	}

	// Builds the tree, and return root.	
	private static HuffmanNode buildTree(Map<Character, Integer> m) {
		final Queue<HuffmanNode> nodeQueue = createNodeQueue(m);
		while (nodeQueue.size() > 1) {
			final HuffmanNode node1 = nodeQueue.remove();
			final HuffmanNode node2 = nodeQueue.remove();
			HuffmanNode combinedNode = new HuffmanNode('#', node1.frequency + node2.frequency, node1, node2);
			nodeQueue.add(combinedNode);
		}

		return nodeQueue.remove();
	}

	// Return HashMap contains codeword for each character
	private static Map<Character, String> generateCodes(HuffmanNode root) {
		final Map<Character, String> map = new HashMap<Character, String>();
		doGenerateCode(root, map, "");
		return map;
	}

	private static void doGenerateCode(HuffmanNode node, Map<Character, String> map, String s) {
		if (node.leftChild == null && node.rightChild == null) {
			map.put(node.c, s);
			return;
		}
		doGenerateCode(node.leftChild, map, s + "0");
		doGenerateCode(node.rightChild, map, s + "1");
	}
	
	// Print Huffman codeword
	private static void printCode(HuffmanNode root, String s) {
		if (root.leftChild == null && root.rightChild == null && (Character.isLetterOrDigit(root.c) || Character.isSpaceChar(root.c))) {

			System.out.println(root.c + "   |  " + s);

			return;
		}
		printCode(root.leftChild, s + "0");
		printCode(root.rightChild, s + "1");
	}

	// Returns the binary encodeing for the text.
	private static String encodeMessage(Map<Character, String> charCode, String text) {
		String c = "";
		for (int i = 0; i < text.length(); i++) 
			c += charCode.get(text.charAt(i));
		return c;
	}

	// Take binary encoding as a parameter, then decode it and return decoded text.
	private static String decodeMessage(String s, Map<Character, String> map) {
		String temp = "";
		String result = "";
		// Loop through all s.
		for (int i = 0; i < s.length(); i++) {
			temp += s.charAt(i);
			// If I find a Huffman codeword
			if (map.containsValue(temp)) {
				for (Map.Entry<Character, String> entry : map.entrySet()) {
					if (Objects.equals(temp, entry.getValue())) {
						result = result + entry.getKey();
						temp = new String();
					}
				}
			}
		}
		return result;
	}
	
	// Prints binary tree in consle.
	public static void printBinaryTree(HuffmanNode root) {
		LinkedList<HuffmanNode> treeLevel = new LinkedList<HuffmanNode>();
		treeLevel.add(root);
		LinkedList<HuffmanNode> temp = new LinkedList<HuffmanNode>();
		int counter = 0;
		int height = heightOfTree(root) - 1;
		// System.out.println(height);
		double numberOfElements = (Math.pow(2, (height + 1)) - 1);
		// System.out.println(numberOfElements);
		while (counter <= height) {
			HuffmanNode removed = treeLevel.removeFirst();
			if (temp.isEmpty()) {
				printSpace(numberOfElements / Math.pow(2, counter + 1), removed);
			} else {
				printSpace(numberOfElements / Math.pow(2, counter), removed);
			}
			if (removed == null) {
				temp.add(null);
				temp.add(null);
			} else {
				temp.add(removed.leftChild);
				temp.add(removed.rightChild);
			}

			if (treeLevel.isEmpty()) {
				System.out.println("");
				System.out.println("");
				treeLevel = temp;
				temp = new LinkedList<>();
				counter++;
			}

		}
	}
	
	// Helping method for printBinaryTree.
	public static void printSpace(double n, HuffmanNode removed) {
		for (; n > 0; n--) {
			System.out.print(" ");
		}
		if (removed == null) {
			System.out.print("");
		} else {
			System.out.print(removed.c);
		}
	}
	
	// Return height of tree.
	public static int heightOfTree(HuffmanNode root) {
		if (root == null) {
			return 0;
		}
		return 1 + Math.max(heightOfTree(root.leftChild), heightOfTree(root.rightChild));
	}
	
	public static void graphvizTree(HuffmanNode root) {

		HuffmanNode nRoot = root;
		Queue<HuffmanNode> q = new LinkedList<HuffmanNode>();

		int i = 0;
		int k = 0;
		q.add(nRoot);

		String text = "Graph\n{ \n";
		String valueParent = "n" + i;
		String parent = "n" + i + "[" + "label=\"" + nRoot.frequency + "\n " + "\"];\n";
		text += parent;

		while (!q.isEmpty()) {
			valueParent = "n" + k;
			HuffmanNode nn = q.poll();

			if (nn.leftChild != null && nn.rightChild != null) {
				q.add(nn.leftChild);

				// if it's leaf print frequency and letter
				// , if not then print frequency.
				String leftChild = "n" + (++i) + "[color=black," + "label=\""
						+ (nn.leftChild.isLeaf()
								? nn.leftChild.frequency + "\n" + nn.leftChild.c
										: nn.rightChild.frequency + "\n ")
						+ "\"];\n";
				String vLeftChild = "n" + (i);
				text += leftChild;
				text += valueParent + "--" + vLeftChild + "[color=black," + "label=\"0\"];\n";

				q.add(nn.rightChild);
				// if it's leaf print frequency and letter
				// , if not then print frequency.
				String rightChild = "n" + (++i) + "[color=green," + "label=\""
						+ (nn.rightChild.isLeaf()
								? nn.rightChild.frequency + "\n" + nn.rightChild.c
								: nn.rightChild.frequency + "\n ")
						+ "\"];\n";
				String vRightChild = "n" + (i);
				text += rightChild;
				text += valueParent + "--" + vRightChild + "[color=green," + "label=\"1\"];\n";
			}
			k++;
		}

		text += "\n" + "}";
		System.out.println(text);
	}
}