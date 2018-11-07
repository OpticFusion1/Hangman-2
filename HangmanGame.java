import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.io.IOException;

/**
 * Author: Otoman25
 * Email: g b richards @ gmail.com
 *
 * Features:
 *  Uses a state based system to direct the flow through methods
 *  Text file dictionary with sample words if the dictionary is not found.
 *  Scores can be saved along with the users name.
 *  Scores are sorted at the time of loading and when a new score is added.
 *  Users can also add their own words to the dictionary.
 *  Custom words are displayed separately to dictionary words.
 *  Users can view both dictionary and custom words.
 *
 *  Classes:
 *   HangmanGame - Handles menus and all input and most output
 *   Hangman - Each instance is a game of hangman. Stores the word in play, users guesses, alphabet and prints out the game screen
 *   Loader - Handles reading from and saving to files. Handles storage of and operations on dictionaries and scores.
 *
 * Date: 07/11/2018
 */

public class HangmanGame {
    enum STATE {
        MENU,
        GAME,
        DICTIONARY,
        GAMEEND,
        SCORE,
        EXIT
    }

    private static Scanner scan = new Scanner(System.in);
    private static Hangman hangman;
    private static STATE currentState = STATE.MENU;
    private static Loader loader = new Loader();

    public static void main(String[] args) {
        if(!loader.filesLoaded()) {
            System.out.println("Something is wrong. Cannot open or create files dictionary.txt and highscores.txt.\nExiting.");
            currentState = STATE.EXIT;
        }

        while(currentState != STATE.EXIT) {
            switch(currentState){
                case MENU:
                    currentState = vMainMenu();
                    break;
                case GAME:
                    currentState = vGameLoop();
                    break;
                case DICTIONARY:
                    currentState = vDictionary();
                    break;
                case GAMEEND:
                    currentState = vGameOver();
                    break;
                case SCORE:
                    currentState = vScore();
                    break;
                default:
                    currentState = vMainMenu();
                    break;
            }
        }
        loader.saveWords();
        loader.saveScores();
        System.out.println("--\nThanks for playing");
        scan.close();
    }

    static private STATE vMainMenu(){
        String input;
        System.out.println("--");
        System.out.println("Hangman");
        System.out.println("--");
        System.out.println("1. Play game");
        System.out.println("2. High-Scores");
        System.out.println("3. Dictionary");
        System.out.println("X. Exit");
        System.out.print("Choose an option: ");
        input = scan.nextLine().toLowerCase();

        switch (input.charAt(0)) {
            case '1':
                return STATE.GAME;
            case '2':
                return STATE.SCORE;
            case '3':
                return STATE.DICTIONARY;
            case 'x':
                return STATE.EXIT;
            default:
                return STATE.MENU;
        }
    }

    static private STATE vDictionary(){
        //view list of words
        //add new words
        String input = " ";

        while(input.charAt(0) != 'x') {
            System.out.println("--");
            System.out.println("Dictionary");
            System.out.println("--");
            System.out.println("1. View standard words");
            System.out.println("2. View custom words");
            System.out.println("3. Add a word");
            System.out.println("x. Back to the main menu");
            System.out.print("Choose an option: ");
            input = scan.nextLine().toLowerCase();

            switch (input.charAt(0)) {
                case '1':
                    System.out.println("--");
                    System.out.println("Standard word list (" + loader.getWordCount() + " words)");
                    System.out.println("--");
                    printWordList();
                    break;
                case '2':
                    System.out.println("--");
                    System.out.println("Custom word list (" + loader.getCustomWordCount() + " words)");
                    System.out.println("--");
                    printCustomWordList();
                    break;
                case '3':
                    boolean validInput = false;
                    while(!validInput){
                        System.out.print("Type a new word: ");
                        input = scan.nextLine().toLowerCase();

                        validInput = true;
                        for(char c : input.toCharArray()){
                            if(!Character.isAlphabetic(c)) validInput = false;
                        }
                    }
                    loader.addWord(input);
                    break;
                case 'x':
                    return STATE.MENU;
            }
        }
        return STATE.MENU;
    }

    static private STATE vGameLoop(){
        String input;
        hangman = new Hangman(loader.getRandomWord());
        hangman.drawHangman();
        System.out.print("Guess a letter. Type exit to the menu: ");
        input = scan.nextLine().toLowerCase();

        while(!input.equals("exit") && !hangman.hasLost() && !hangman.hasWon()) {
            hangman.guessLetter(input.charAt(0));
            hangman.drawHangman();

            if(hangman.hasWon() || hangman.hasLost()){
                return STATE.GAMEEND;
            }

            System.out.print("Guess a letter: ");
            input = scan.nextLine().toLowerCase();
        }

        return STATE.MENU;
    }

    static private STATE vScore(){
        System.out.println("--");
        System.out.println("High Scores");
        System.out.println("--");
        printScores();
        System.out.println("--");
        System.out.println("1. Reset all scores");
        System.out.println("x. Go back to the main menu");
        if(scan.nextLine().toLowerCase().charAt(0) == '1'){
            loader.resetScores();
        }

        return STATE.MENU;
    }

    static private STATE vGameOver(){
        System.out.println("--");
        System.out.println("Game over");
        System.out.println("--");
        if(hangman.hasWon()){
            System.out.println("Congratulations you won! The word was: " + hangman.getCurrentWord());
            System.out.println("You took " + hangman.getGuessCount() + " guesses.");

            if(hangman.getGuessCount() < loader.getAverageGuesses())
                System.out.println("You scored better than average. The average is " + loader.getAverageGuesses() + " guesses.");
            else
                System.out.println("You scored lower than average. The average is " + loader.getAverageGuesses() + " guesses.");

            System.out.print("Would you like save your score? yes/no: ");
            if(scan.nextLine().equals("yes")){
                System.out.print("Please enter your name: ");
                loader.addHighScore(scan.nextLine(), hangman.getGuessCount());
            }
        } else {
            System.out.println("Unlucky. Try another game!");
        }
        return STATE.MENU;
    }

    static private void printScores(){
        if(loader.getScores().size() == 0) System.out.println("High-scores are empty.");
        for(Map.Entry<String, Integer> entry : loader.getScores().entrySet()){
            System.out.print(entry.getKey());
            if(entry.getKey().length() < 3) System.out.print("\t");
            System.out.print("\t");
            System.out.println(entry.getValue());
        }
    }

    static private void printWordList(){
        if(loader.getWordCount() == 0) System.out.println("Standard word list empty.");
        for(String word : loader.getWords()){
            System.out.println(word);
        }
    }

    static private void printCustomWordList(){
        if(loader.getCustomWordCount() == 0) System.out.println("Custom word list empty.");
        for(String word : loader.getCustomWords()){
            System.out.println(word);
        }
    }
}

class Hangman {
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int DEFAULT_LIVES = 8;

    private int lives;
    private char[] currentAlphabet;
    private String currentWord;
    private char[] playerWord;
    private int guessCount;

    Hangman(String word){
        currentWord = word;
        playerWord = new char[word.length()];
        currentAlphabet = new char[ALPHABET.length];
        lives = DEFAULT_LIVES;
        guessCount = 0;
        System.arraycopy(ALPHABET, 0, currentAlphabet, 0, ALPHABET.length);

        for(int i = 0; i < playerWord.length; i++){
            playerWord[i] = '-';
        }
    }

    boolean hasLost() {
        return lives == 0;
    }

    boolean hasWon(){
       return currentWord.equals(String.valueOf(playerWord));
    }

    void guessLetter(char c){
        for(int i = 0; i < currentAlphabet.length; i++){
            if(c == currentAlphabet[i]) {
                fillIn(c);
                currentAlphabet[i] = ' ';
                guessCount++;
            }
        }
    }

    int getGuessCount(){
        return guessCount;
    }

    void drawHangman() {
        int currentImage = 8 - lives;

        for (int line = 0; line < 7 && lives < 8; line++) {
            int start = line * 10;
            System.out.println(image[currentImage].substring(start, start + 10));
        }

        System.out.println(currentAlphabet);
        System.out.println("Your word: " + String.valueOf(playerWord));
        System.out.println("You have " + lives + " lives left.");

        if (lives == 0) {
            System.out.println("Game over.");
        }
    }

    private void fillIn(char c){
        boolean foundLetter = false;

        for(int i = 0; i < currentWord.length(); i++){
            if(currentWord.charAt(i) == c) {
                playerWord[i] = c;
                foundLetter = true;
            }
        }

        if(!foundLetter) lives--;
    }

    String getCurrentWord(){
        return currentWord;
    }

    private String[] image = { // 10 x 7 'image'
            "          " + //0
            "          " +
            "          " +
            "          " +
            "          " +
            "          " +
            "          ",

            "|-------  " + //1
            "|         " +
            "|         " +
            "|         " +
            "|         " +
            "|         " +
            "|         ",

            "|-------  " + //2
            "|      |  " +
            "|         " +
            "|         " +
            "|         " +
            "|         " +
            "|         ",

            "|-------  " + //3
            "|      |  " +
            "|      o  " +
            "|         " +
            "|         " +
            "|         " +
            "|         ",

            "|-------  " + //4
            "|      |  " +
            "|      o  " +
            "|      |  " +
            "|         " +
            "|         " +
            "|         ",

            "|-------  " + //5
            "|      |  " +
            "|      o  " +
            "|     -|- " +
            "|      |  " +
            "|         " +
            "|         ",

            "|-------  " + //6
            "|      |  " +
            "|      o  " +
            "|     -|- " +
            "|    / | \\" +
            "|         " +
            "|         ",

            "|-------  " + //7
            "|      |  " +
            "|      o  " +
            "|     -|- " +
            "|    / | \\" +
            "|     / \\ " +
            "|         ",

            "|-------  " + //8
            "|      |  " +
            "|      o  " +
            "|     -|- " +
            "|    / | \\" +
            "|     / \\ " +
            "|    /   \\",
    };
}

class Loader {
    private File wordDictionary = new File("dictionary.txt");
    private File highScores = new File("highscores.txt");
    private boolean areFilesLoaded;

    private HashMap<String, Integer> scores = new HashMap<>();
    private Vector<String> words = new Vector<>();
    private Vector<String> customWords = new Vector<>();

    Loader(){
        try {
            //Creates new files if the don't already exist
            wordDictionary.createNewFile(); //Warning - Do not need to know result (returns whether file was created)
            highScores.createNewFile();
            areFilesLoaded = true;

        } catch(IOException e){
            System.out.println("Error: cannot find or create dictionary.txt and highscores.txt");
            areFilesLoaded = false;
        }

        readInScores();
        readInWords();
    }

    private void readInScores(){
        //Scores in format
        // name,score,guesses,name2,score2,guesses2
        // -
        //Whitespaces are removed

        try (Scanner scan = new Scanner(highScores)) {
            while(scan.hasNext()){
                String line = scan.nextLine().replace(" ", "");
                String[] values = line.replace(" ", "").split(",");
                String tempName = "";
                int guesses;

                for(String val : values){
                    if(tempName.equals("")){
                        tempName = val;
                    } else {
                        try {
                            guesses = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            guesses = 0;
                        }
                        scores.put(tempName, guesses);
                        tempName = "";
                    }
                }
            }

            sortScores();

        } catch (FileNotFoundException e){
            System.out.println("Error: cannot find or create dictionary.txt and highscores.txt");
        }
    }

    private void readInWords() {
        try (Scanner scan = new Scanner(wordDictionary)) {
            while(scan.hasNext()){
                String line = scan.nextLine();
                String[] listOfWords = line.split(",");

                for(String word : listOfWords){
                    if(word.endsWith("~")){
                        word = word.replace("~", "");
                        customWords.add(word);
                    } else {
                        words.add(word);
                    }
                }
            }
        } catch (FileNotFoundException e){
            System.out.println("Error: cannot find or create dictionary.txt and highscores.txt");
        }

        if(words.isEmpty()){
            words.add("these");
            words.add("are");
            words.add("some");
            words.add("standard");
            words.add("words");

            words.add("there");
            words.add("were");
            words.add("random");
        }
    }

    void saveWords(){
        try (PrintWriter out = new PrintWriter(wordDictionary)) {
            StringBuilder wordConcatenation = new StringBuilder();

            for (String word : words) {
                wordConcatenation.append(word);
                wordConcatenation.append(",");
            }
            StringBuilder wordDashConcatenation = new StringBuilder();
            for (String customWord : customWords) {
                wordDashConcatenation.append(customWord);
                wordDashConcatenation.append("~,");
            }

            out.print(wordConcatenation.toString());
            out.print(wordDashConcatenation.toString());
        } catch ( FileNotFoundException e) {
            System.out.println("Unable to save dictionary.txt");
        }
    }

    void saveScores(){
        try(PrintWriter out = new PrintWriter(highScores)){
            StringBuilder builder = new StringBuilder();

            for(Map.Entry<String, Integer> entry : scores.entrySet()){
                builder.append(entry.getKey());
                builder.append(",");
                builder.append(entry.getValue());
                builder.append(",");
            }

            out.print(builder.toString());
        } catch (FileNotFoundException e){
            System.out.println("Unable to save highScores.txt");
        }
    }

    void addHighScore(String name, int guesses){
        scores.put(name, guesses);
        sortScores();
    }

    void addWord(String word){
        customWords.add(word);
    }

    private void sortScores(){
        HashMap<String, Integer> newMap = new HashMap<>();
        HashMap<String, Integer> toRemove = new HashMap<>();
        HashMap<String, Integer> tempMap = new HashMap<>(scores);
        int highestValue = 0;
        boolean sorted = false;

        while(!sorted){
            //Get highest score
            for(Map.Entry<String, Integer> entry : tempMap.entrySet()){
               if(highestValue < entry.getValue()) highestValue = entry.getValue();
            }

            //Add all matches to newMap
            for(Map.Entry<String, Integer> entry : tempMap.entrySet()){
                if(highestValue == entry.getValue()) {
                    newMap.put(entry.getKey(), entry.getValue());
                    toRemove.put(entry.getKey(), entry.getValue());
                }
            }

            //Remove old
            for(Map.Entry<String, Integer> entry : toRemove.entrySet()){
                tempMap.remove(entry.getKey(), entry.getValue());
            }

            toRemove.clear();
            highestValue = 0;

            if(newMap.size() == scores.size()) sorted = true;
        }

        scores = newMap;
    }

    int getAverageGuesses(){
        int sum = 0;

        for(int i : scores.values()){
            sum += i;
        }

        return Math.round(sum / (float) scores.size());
    }

    //Getters
    String getRandomWord(){
        Random rand = new Random();
        boolean chooseCustomWord = rand.nextBoolean();
        int wordIndex;

        if(chooseCustomWord && customWords.size() > 0){
            wordIndex = rand.nextInt(customWords.size());
            return customWords.get(wordIndex);
        } else {
            wordIndex = rand.nextInt(words.size());
            return words.get(wordIndex);

        }
    }

    Vector<String> getWords(){
        return words;
    }

    Vector<String> getCustomWords() {
        return customWords;
    }

    HashMap<String, Integer> getScores() {
        return scores;
    }

    int getWordCount(){
        return words.size();
    }

    int getCustomWordCount(){
        return customWords.size();
    }

    void resetScores(){
        scores.clear();
    }

    boolean filesLoaded() {
        return areFilesLoaded;
    }
}