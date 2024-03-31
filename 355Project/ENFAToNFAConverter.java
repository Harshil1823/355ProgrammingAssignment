import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.ArrayDeque;

public class ENFAToNFAConverter {
    private int numStates;
    private int alphabetSize;
    private Set<Integer> acceptingStates;
    private Set<Integer>[][] transitions;

    public ENFAToNFAConverter(String filename) {
        readENFA(filename);
    }

    // Read ε-NFA from file
    private void readENFA(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            numStates = Integer.parseInt(line.substring("Number of states: ".length()));

            line = br.readLine();
            alphabetSize = Integer.parseInt(line.substring("Alphabet size: ".length()));

            line = br.readLine();
            String[] acceptingStatesStr = line.substring("Accepting states: ".length()).trim().split("\\s+");
            acceptingStates = new HashSet<>();
            for (String state : acceptingStatesStr) {
                acceptingStates.add(Integer.parseInt(state));
            }

            transitions = new Set[numStates][alphabetSize + 1]; // +1 for epsilon transitions
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j <= alphabetSize; j++) {
                    transitions[i][j] = new HashSet<>();
                }
            }

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

    // Convert ε-NFA to NFA
    public void convertENFAToNFA() {
        stage1();
        stage2();
        stage3();
    }

    // Stage 1: Mark states as accepting if they have ε-transitions to accepting states
    private void stage1() {
        // Stage 1: Mark states as accepting if they have ε-transitions to accepting states

        for (int state = 0; state < numStates; state++) {
            Set<Integer> epsilonClosure = epsilonClosure(state);
            for (int nextState : epsilonClosure) {
                if (acceptingStates.contains(nextState)) {
                    acceptingStates.add(state);
                    break; // Once an accepting state is found, no need to continue checking
                }
            }
        }

    }

    // Stage 2: Propagate transitions through ε-transitions
    private void stage2() {
        Queue<Integer> queue = new ArrayDeque<>();
        boolean[] visited = new boolean[numStates];
        for (int state : acceptingStates) {
            Set<Integer> epsilonClosure = epsilonClosure(state);
            queue.addAll(epsilonClosure);
            visited[state] = true;
        }

        while (!queue.isEmpty()) {
            int currentState = queue.poll();
            for (int symbol = 1; symbol <= alphabetSize; symbol++) {
                Set<Integer> epsilonClosure = epsilonClosure(currentState);
                for (int nextState : epsilonClosure) {
                    if (!visited[nextState]) {
                        queue.add(nextState);
                        visited[nextState] = true;
                    }
                    transitions[currentState][symbol].addAll(transitions[nextState][symbol]);
                }
            }
        }
    }
// Calculate epsilon closure of a state
private Set<Integer> epsilonClosure(int state) {
    Set<Integer> closure = new HashSet<>();
    Stack<Integer> stack = new Stack<>();
    boolean[] visited = new boolean[numStates];

    stack.push(state);
    closure.add(state);
    visited[state] = true;

    while (!stack.isEmpty()) {
        int currentState = stack.pop();
        Set<Integer> epsilonTransitions = transitions[currentState][0]; // Epsilon transitions
        for (int nextState : epsilonTransitions) {
            if (!visited[nextState]) {
                closure.add(nextState);
                stack.push(nextState);
                visited[nextState] = true;
            }
        }
    }

    return closure;
}

    // Stage 3: Remove ε-transitions
    private void stage3() {
        for (int state = 0; state < numStates; state++) {
            transitions[state][0].clear();
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

    // public static void main(String[] args) {
    //     ENFAToNFAConverter converter = new ENFAToNFAConverter("test-e-NFA5.txt");
    //     converter.convertENFAToNFA();
    //     converter.printNFA();
    // }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ENFAToNFAConverter <ENFA_File>");
            return;
        }
    
        ENFAToNFAConverter converter = new ENFAToNFAConverter(args[0]);
        converter.convertENFAToNFA();
        converter.printNFA();
    }
    
}