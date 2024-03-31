import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class NFASimulator {
    private int numStates;
    private int alphabetSize;
    private Set<Integer> acceptingStates;
    private Set<Integer>[][] transitions;

    @SuppressWarnings("unchecked")
    public NFASimulator(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read the number of states from the first line of the file
            String line = br.readLine();
            numStates = Integer.parseInt(line.substring("Number of states: ".length()));

            // Read the alphabet size from the second line of the file
            line = br.readLine();
            alphabetSize = Integer.parseInt(line.substring("Alphabet size: ".length()));

            // Read the accepting states from the third line of the file
            line = br.readLine();
            String[] acceptingStatesStr = line.substring("Accepting states: ".length()).trim().split("\\s+");
            acceptingStates = new HashSet<>();
            for (String state : acceptingStatesStr) {
                acceptingStates.add(Integer.parseInt(state));
            }

            // Initialize the transitions table with sets for each state and alphabet symbol
            transitions = new Set[numStates][alphabetSize + 1]; // +1 for epsilon transitions
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j <= alphabetSize; j++) {
                    transitions[i][j] = new HashSet<>();
                }
            }

            // Read the transition table from the rest of the file
            for (int i = 0; i < numStates; i++) {
                line = br.readLine().trim();
                String[] sets = line.split("\\s+");
                for (int j = 0; j < sets.length; j++) {
                    if (!sets[j].equals("{}")) {
                        String[] values = sets[j].replaceAll("[{}]", "").split(",");
                        for (String value : values) {
                            int val = Integer.parseInt(value);
                            transitions[i][j].add(val);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Print the NFA representation
    public void printNFA() {
        System.out.println("Number of states: " + numStates);
        System.out.println("Alphabet size: " + alphabetSize);
        System.out.print("Accepting states: ");
        for (int state : acceptingStates) {
            System.out.print(state + " ");
        }
        System.out.println();
    
        // Print transitions
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j <= alphabetSize; j++) {
                System.out.print("{");
                int count = 0;
                for (int transition : transitions[i][j]) {
                    System.out.print(transition);
                    if (++count < transitions[i][j].size()) {
                        System.out.print(",");
                    }
                }
                System.out.print("} ");
            }
            System.out.println();
        }
    }
    
    // Method to read a file containing strings, one per line
    public List<String> readStringsFromFile(String filename) {
        List<String> strings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                strings.add(line); // Add each line as a string to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    // Method to check if strings are accepted or rejected by the NFA
    public void checkStrings(List<String> strings) {
        for (String str : strings) {
            boolean accepted = isAccepted(str);
            System.out.println(str + ": " + (accepted ? "Accepted" : "Rejected"));
        }
    }

    
    // Method to check if a single string is accepted by the NFA
    private boolean isAccepted(String str) {
        
            // Start from the initial states
            Set<Integer> currentStates = Collections.singleton(0); // Assuming state 0 is the initial state
        
            // Iterate through each character in the string
            for (char ch : str.toCharArray()) {
                Set<Integer> nextStates = new HashSet<>();
                // Find possible next states using the character's transitions
                for (int state : currentStates) {
                    int symbolIndex = ch - 'a'; // Assuming alphabet is lowercase letters
                    nextStates.addAll(transitions[state][symbolIndex + 1]); // +1 to skip epsilon transitions
                }
                // Update current states with the next possible states
                currentStates = nextStates;
            }
        
            // Check if any of the final states are accepting states
            return !Collections.disjoint(currentStates, acceptingStates);
        
        
    }

    private Set<Integer> epsilonClosure(Set<Integer> states) {
        Set<Integer> closure = new HashSet<>(states); 
        boolean changed = true;
    
        // Iterate until no new states are added through epsilon transitions
        while (changed) {
            changed = false;
            Set<Integer> temp = new HashSet<>();
            for (int state : closure) {
                // Add epsilon transitions from current state
                temp.addAll(transitions[state][alphabetSize]); // Last index for epsilon
            }
            // Check for newly added states and update closure
            if (!closure.containsAll(temp)) {
                closure.addAll(temp);
                changed = true;
            }
        }
        return closure;
    }
    

    // public static void main(String[] args) {
    //     // Create an instance of NFASimulator with the input file
    //     NFASimulator simulator = new NFASimulator("testNFA2.txt");
    //     // Print the NFA representation
    //     simulator.printNFA();

    //     // Read strings from another file and check if they are accepted or rejected
    //     List<String> strings = simulator.readStringsFromFile("testNFA2-strings.txt");
    //     System.out.println("Strings read from file:");
    //     for (String str : strings) {
    //         System.out.println(str);
    //     }
    //     System.out.println();
    //     System.out.println();
    //     System.out.println();

    //     // Check if strings are accepted or rejected by the NFA
    //     simulator.checkStrings(strings);
    // }
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java NFASimulator <NFA_File> <Strings_File>");
            return;
        }
    
        // Create an instance of NFASimulator with the input file
        NFASimulator simulator = new NFASimulator(args[0]);
        // Print the NFA representation
        simulator.printNFA();
    
        // Read strings from another file and check if they are accepted or rejected
        List<String> strings = simulator.readStringsFromFile(args[1]);
        
        System.out.println();
        System.out.println();
        // Check if strings are accepted or rejected by the NFA
        simulator.checkStrings(strings);
    }
    
}
