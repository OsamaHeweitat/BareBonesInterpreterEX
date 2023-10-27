import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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
    private String[] commandKeyWords = {"clear", "incr", "decr", "while", "end", "add", "sub", "times", "div", "if"};
    private int interpreterIndex = 0;

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
        System.out.print("Enter the file name (excluding extension): ");
        String fileName = inputScanner.nextLine();

        try {
            File file = new File(fileName + ".txt");
            if(!file.exists())
                file = new File(fileName + ".bb");
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
        getRidOfComments(lines);
        String[] commands = lines.toArray(new String[0]);
        debugLog(Arrays.toString(commands));
        String[][] variableTable = interpret(commands);
        System.out.println(Arrays.toString(variableTable[0]) + Arrays.toString(variableTable[1]));
    }

    public void getRidOfComments(List<String> commands){
        for(int i = 0; i < commands.size(); i++){
            String command = commands.get(i);
            if(command.contains("//")){
                debugLog("Comment found: " + command.substring(command.indexOf("//")));
                String strippedCommand = command.substring(0, command.indexOf("//")).strip();
                String variable = command.substring(command.strip().indexOf(".") + 1).strip();
                commands.set(i, strippedCommand);
                if(variable.length() > 0){
                    commands.add(i + 1, variable);
                }
                if(strippedCommand.equals("")){
                    commands.remove(i);
                }
            }
        }
    }

    public String[][] interpret(String[] commands){
        String[][] variableTable = variables(commands);
        for(interpreterIndex = 0; interpreterIndex < commands.length; interpreterIndex++){
            for(String keyWord : commandKeyWords){
                if(commands[interpreterIndex].contains(keyWord)){
                    commandRunner(commands[interpreterIndex], commands, variableTable, interpreterIndex + 1, "main");
                }
            }
        }
        return variableTable;
    }

    public void commandRunner(String command, String[] commands, String[][] variableTable, int i, String callingFunction){
        String[] commandWords = command.split(" ");
        String commandKeyWord = commandWords[0];
        String variable = null;
        debugLog(i + " " + command + " (" + callingFunction + ")");
        if(commandWords.length >= 2){
            variable = commandWords[1];
        }

        try {
            Method commandMethod = this.getClass().getDeclaredMethod(commandKeyWord + "Cmd", String.class, String[][].class, String.class, String[].class, int.class);
            commandMethod.invoke(this, variable, variableTable, command, commands, i);
        } catch (NoSuchMethodException e) {
            System.out.println("Command not recognized: " + commandKeyWord);
            System.exit(0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        }
        debugLog(Arrays.toString(variableTable[0]) + Arrays.toString(variableTable[1]) + "\n");
    }

    public void clearCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = "0";
            }
        }
    }

    public void incrCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(Integer.parseInt(variableTable[1][i]) + 1);
            }
        }
    }

    public void decrCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(Integer.parseInt(variableTable[1][i]) - 1);
            }
        }
    }

    public void whileCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        String value = getValue(variable, variableTable);
        int start = 0;
        int end = 0;
        int whileSkips = 0;
        for(int i = index - 1; i < commands.length; i++){
            if(commands[i].equals(command)){
                start = i;
            }else if(commands[i].contains("while") || commands[i].contains("if")){
                whileSkips++;
            }
            if(commands[i].equals("end") && whileSkips > 0){
                whileSkips--;
            }else if(commands[i].equals("end") && whileSkips == 0){
                end = i;
                debugLog("start: " + (start + 1) + " end: " + (end + 1) + "\n");
                break;
            }
        }

        String desiredValue = command.split(" ")[3];
        boolean not;
        if(command.split(" ")[2].equals("not")){
            not = true;
        }else if(command.split(" ")[2].equals("is")){
            not = false;
        }else{
            throw new IllegalArgumentException("Invalid while statement");
        }
        if(!(not && !value.equals(desiredValue) || (!not && value.equals(desiredValue)))){
            for(int i = start + 1; i < (end + 1); i++){
                if(commands[i].equals("end")){
                    commandRunner(commands[i], commands, variableTable, i + 1, "if (from line " + (start + 1) + ")");
                }
            }
        }
        while((not && !value.equals(desiredValue) || (!not && value.equals(desiredValue)))){
            whileSkips = 0;
            for(int i = start + 1; i < (end + 1); i++){
                if(commands[i].equals("end") && whileSkips > 0){
                    whileSkips--;
                }
                if(whileSkips == 0){
                    commandRunner(commands[i], commands, variableTable, i + 1, "while (from line " + (start + 1) + ")");
                }
                if(commands[i].contains("while") || commands[i].contains("if")){
                    whileSkips++;
                }
            }
            value = getValue(variable, variableTable);
        }
    }

    public void ifCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        String value = getValue(variable, variableTable);
        int start = 0;
        int end = 0;
        int whileSkips = 0;
        for(int i = index - 1; i < commands.length; i++){
            if(commands[i].equals(command)){
                start = i;
            }else if(commands[i].contains("while") || commands[i].contains("if")){
                whileSkips++;
            }
            if(commands[i].equals("end") && whileSkips > 0){
                whileSkips--;
            }else if(commands[i].equals("end") && whileSkips == 0){
                end = i;
                debugLog("start: " + (start + 1) + " end: " + (end + 1) + "\n");
                break;
            }
        }

        String desiredValue = command.split(" ")[3];
        boolean not;
        if(command.split(" ")[2].equals("not")){
            not = true;
        }else if(command.split(" ")[2].equals("is")){
            not = false;
        }else{
            throw new IllegalArgumentException("Invalid if statement");
        }
        if((not && !value.equals(desiredValue) || (!not && value.equals(desiredValue)))){
            whileSkips = 0;
            for(int i = start + 1; i < (end + 1); i++){
                if(commands[i].equals("end") && whileSkips > 0){
                    whileSkips--;
                }
                if(commands[i].equals("end")){
                    commandRunner(commands[i], commands, variableTable, i + 1, "if (from line " + (start + 1) + ")");
                }
                if(whileSkips == 0){
                    commandRunner(commands[i], commands, variableTable, i + 1, "if (from line " + (start + 1) + ")");
                }
                if(commands[i].contains("while") || commands[i].contains("if")){
                    whileSkips++;
                }
            }
            value = getValue(variable, variableTable);
        }else{
            for(int i = start + 1; i < (end + 1); i++){
                if(commands[i].equals("end")){
                    commandRunner(commands[i], commands, variableTable, i + 1, "if (from line " + (start + 1) + ")");
                }
            }
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

    public void endCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        if((index) == commands.length){
            interpreterIndex = index - 1;
        }else{
            interpreterIndex = index - 1;
        }
    }

    public void addCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        int value1 = -1;
        int value2 = -1;
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(command.split(" ")[2])){
                value1 = Integer.parseInt(variableTable[1][i]);
            }
            if(variableTable[0][i].equals(command.split(" ")[3])){
                value2 = Integer.parseInt(variableTable[1][i]);
            }
        }
        if(value1 == -1){
            value1 = Integer.parseInt(command.split(" ")[2]);
        }
        if(value2 == -1){
            value2 = Integer.parseInt(command.split(" ")[3]);
        }
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(value1 + value2);
            }
        }
    }

    public void subCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        int value1 = -1;
        int value2 = -1;
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(command.split(" ")[2])){
                value1 = Integer.parseInt(variableTable[1][i]);
            }
            if(variableTable[0][i].equals(command.split(" ")[3])){
                value2 = Integer.parseInt(variableTable[1][i]);
            }
        }
        if(value1 == -1){
            value1 = Integer.parseInt(command.split(" ")[2]);
        }
        if(value2 == -1){
            value2 = Integer.parseInt(command.split(" ")[3]);
        }
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(value1 - value2);
            }
        }
    }

    public void timesCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        int value1 = -1;
        int value2 = -1;
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(command.split(" ")[2])){
                value1 = Integer.parseInt(variableTable[1][i]);
            }
            if(variableTable[0][i].equals(command.split(" ")[3])){
                value2 = Integer.parseInt(variableTable[1][i]);
            }
        }
        if(value1 == -1){
            value1 = Integer.parseInt(command.split(" ")[2]);
        }
        if(value2 == -1){
            value2 = Integer.parseInt(command.split(" ")[3]);
        }
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString(value1 * value2);
            }
        }
    }

    public void divCmd(String variable, String[][] variableTable, String command, String[] commands, int index){
        int value1 = -1;
        int value2 = -1;
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(command.split(" ")[2])){
                value1 = Integer.parseInt(variableTable[1][i]);
            }
            if(variableTable[0][i].equals(command.split(" ")[3])){
                value2 = Integer.parseInt(variableTable[1][i]);
            }
        }
        if(value1 == -1){
            value1 = Integer.parseInt(command.split(" ")[2]);
        }
        if(value2 == -1){
            value2 = Integer.parseInt(command.split(" ")[3]);
        }
        for(int i = 0; i < variableTable[0].length; i++){
            if(variableTable[0][i].equals(variable)){
                variableTable[1][i] = Integer.toString((int) value1/value2);
            }
        }
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