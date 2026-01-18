import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		// Your code goes here
        
        String partWord = "";
        char c;
        // Use in class
        In in = new In(fileName);

        // read chars
        for (int i = 0; i < windowLength; i++) {
            if (!in.isEmpty()) {
                partWord += in.readChar();
            }
        }

        // Rest of text, 1char at a time
        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(partWord);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(partWord, probs);
            }
            probs.update(c);
            partWord = partWord.substring(1) + c;
        }

        // Use calculateProbabilities
        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
	}

    // set probabilities
	void calculateProbabilities(List probs) {				
		// Your code goes here
        int totCnt=0;

        //total number of chars
        ListIterator itr = probs.listIterator(0);
            while (itr.hasNext()) {
                CharData current = itr.next();
                totCnt += current.count;
            }

        itr = probs.listIterator(0);
        double cumulativeProb = 0.0;

        while (itr.hasNext()) {
        CharData current = itr.next();
        current.p = (double) current.count / totCnt; //moneh helkey mehaneh
        cumulativeProb += current.p;
        current.cp = cumulativeProb;
    }

	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		// Your code goes here
        double rnd=Math.random();
        //int size = probs.getSize();

        ListIterator itr = probs.listIterator(0);

        while (itr.hasNext()) {
            CharData current = itr.next();
            if (current.cp > rnd) {
                return current.chr;
            }
        }
		return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		// Your code goes here

        if (initialText.length() < windowLength) {
            return initialText;
        }

        String generatedText = initialText;
        String window = "";

        // add chars until text length
        while (generatedText.length() < textLength) {
            window = generatedText.substring(generatedText.length() - windowLength);
            List probs = CharDataMap.get(window);
            // Wasnt Seen on training
            if (probs == null) {
                return generatedText;
            }
            // rand char
            char c = getRandomChar(probs);
            generatedText += c;
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here

        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        // creates languge model
        LanguageModel lm;
        if (randomGeneration) {
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);
        }

        //model traning
        lm.train(fileName);

        //generate text and print it
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
