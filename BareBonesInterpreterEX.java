import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/* Watch our for
 * Debug Mode
 * Nested While Loops
 * Repeatable Code
 * Modular Command System
 */

public class BareBonesInterpreterEX {
    private boolean debugMode = false;
    private static Scanner fileScanner;
    private static Scanner inputScanner;
    private String[] commandKeyWords = {"clear", "incr", "decr", "while", "end"};
    private boolean skip;

    public static void main(String[] args) {
        while(true){
            BareBonesInterpreterEX interpreter = new BareBonesInterpreterEX();
            interpreter.run();
            System.out.println("Run again? (y/n)");
            String answer = inputScanner.nextLine();
            if(answer.toCharArray()[0] == 'n'){
                break;
            }
        }
    }

    public void run(){
        inputScanner = new Scanner(System.in);
        System.out.print("Enter the file name (include extension): ");
        String fileName = inputScanner.nextLine();
        File file = new File(fileName);

        try {
            fileScanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        }

        while(true){
            System.out.println("Run in debug mode? (y/n)");
            String answer = inputScanner.nextLine();
            if(answer.toCharArray()[0] == 'y'){
                setDebugMode(true);
                break;
            }else if(answer.toCharArray()[0] == 'n'){
                setDebugMode(false);
                break;
            }else{
                System.out.println("Invalid response. Try again.");
            }
        }

        fileScanner.useDelimiter(";");
        List<String> lines = new ArrayList<>();
        while(fileScanner.hasNext()){
            lines.add(fileScanner.next().strip());
        }
        String[] commands = lines.toArray(new String[0]);
        debugLog(Arrays.toString(commands));
        String[][] variableTable = interpret(commands);
        System.out.println(Arrays.toString(variableTable[0]) + Arrays.toString(variableTable[1]));
    }

    public String[][] interpret(String[] commands){
        String[][] variableTable = variables(commands);
        for(int i = 0; i < commands.length; i++){
            for(String keyWord : commandKeyWords){
                if(commands[i].contains(keyWord) && !skip){
                    commandRunner(commands[i], commands, variableTable, i + 1, "main");
                }
            }
        }
        return variableTable;
    }

    public void commandRunner(String command, String[] commands, String[][] variableTable, int i, String callingFunction){
        String[] commandWords = command.split(" ");
        String commandKeyWord = commandWords[0];
        String variable = null;
        System.out.println(i + " " + command + " (" + callingFunction + ")");
        if(commandWords.length >= 2){
            variable = commandWords[1];
        }
        switch(commandKeyWord){
            case "clear":
                clear(variable, variableTable);
                break;
            case "incr":
                incr(variable, variableTable);
                break;
            case "decr":
                decr(variable, variableTable);
                break;
            case "while":
                whileLoop(command, commands, variableTable, i);
        }
        debugLog(Arrays.toString(variableTable[0]) + Arrays.toString(variableTable[1]));
    }

    public void clear(String variable, String[][] variableTable){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = "0";
            }
        }
    }

    public void incr(String variable, String[][] variableTable){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(Integer.parseInt(variableTable[1][i]) + 1);
            }
        }
    }

    public void decr(String variable, String[][] variableTable){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(Integer.parseInt(variableTable[1][i]) - 1);
            }
        }
    }

    public void whileLoop(String command, String[] commands, String[][] variableTable, int index){
        String variable = command.split(" ")[1];
        String value = getValue(variable, variableTable);
        int start = 0;
        int end = 0;
        skip = true;
        int whileSkips = 0;
        System.out.println("***"+index);
        for(int i = index - 1; i < commands.length; i++){
            if(commands[i].equals(command)){
                start = i;
            }else if(commands[i].contains("while")){
                whileSkips++;
            }
            if(commands[i].equals("end") && whileSkips > 0){
                whileSkips--;
            }else if(commands[i].equals("end") && whileSkips == 0){
                end = i;
                System.out.println("start: " + (start + 1) + " end: " + (end + 1));
                break;
            }
        }
        while(!value.equals(command.split(" ")[3])){
            for(int i = start + 1; i < (end + 1); i++){
                commandRunner(commands[i], commands, variableTable, i + 1, "while" + start);
            }
            value = getValue(variable, variableTable);
        }
    }

    public String getValue(String variable, String[][] variableTable){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                return variableTable[1][i];
            }
        }
        return null;
    }

    public void end(String variable, String[][] variableTable){
        skip = false;
    }

    public String[][] variables(String[] commands){
        List<String> variables = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for(String command : commands){
            for(String keyWord : commandKeyWords){
                if(command.contains(keyWord) && command.split(" ").length >= 2 && !variables.contains(command.split(" ")[1])){
                    variables.add(command.split(" ")[1]);
                    values.add("0");
                }
            }
        }
        return new String[][] {variables.toArray(new String[0]), values.toArray(new String[0])};
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void debugLog(String message){
        if(debugMode){
            System.out.println(message);
        }
    }
}

