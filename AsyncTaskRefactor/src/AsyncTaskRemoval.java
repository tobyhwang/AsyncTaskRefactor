import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Remove the specific function from Aync Task
 */
public class AsyncTaskRemoval {

    //attributes of the object
    ArrayList<String> refact;
    String function;

    //Global Variables
    ArrayList<String> removedSection = new ArrayList<>();

    //constructor
    public AsyncTaskRemoval(ArrayList<String> refact, String function){

        this.refact = refact;
        this.function = function;
    }

    //Finds the start of the section (including overrides and comments)
    private int FindFunctionStart(String func){

        //find the index of where the function starts
        int index = 0;
        for(String line : refact){
            if(line.contains(func)){
                break;
            }
            index++;
        }

        //go backwards and find where te previous closed bracket is
        for(int i = index; i >=0; i--){
            if(refact.get(i).equals("\r")){
                //found the last blank line, move up 1 space to not include
                //the empty line
                return i+1;
            }
        }
        return 0;
    }

    //Finds the last bracket of the section
    private int FindEndBracketIndex(int index){
        Stack stack = new Stack();

        //Go through and inspect each line
        for(int i = index; i < refact.size(); i++){
            if(refact.get(i).contains("{")){
                stack.push("{");
            }
            if(refact.get(i).contains("}")){
                stack.pop();
                if(stack.isEmpty()){
                    return i;
                }
            }
        }
        return 0;
    }

    //remove the specific section
    public void RemoveSection(){
        int start = FindFunctionStart(function);
        int end = FindEndBracketIndex(start) + 1;
        List<String> test = new ArrayList<>(refact.subList(start, end));
        try {
            removedSection.addAll(test);
        }
        catch(Exception e){
            System.out.println(e);
        }
        refact.subList(start, end).clear();

    }






}
