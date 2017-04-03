import java.util.*;

/**
 * Created by Toby on 3/29/17.
 */
public class HandleVariables {


    //Go through and determine which variables are used Async task
    public static ArrayList<String> GetParams(HashMap<String, String> savedVariable, LinkedHashMap<String, ArrayList<String>> cachedSection){

        //used variables in the Async Task
        ArrayList<String> usedVariable = new ArrayList<>();

        //the originally initialized variables
        ArrayList<String> initVars = new ArrayList<>();


        //save the cached variable to a usable list
        for(Map.Entry<String, String> entry : savedVariable.entrySet()){
            String var = entry.getValue();
            StringBuilder in = new StringBuilder(var);
            if(var.contains(";")) {
                int loc = in.indexOf(";");
                in.replace(loc, loc + 1, "");
                initVars.add(in.toString());
            }
        }

        //run through each section
        for(Map.Entry<String, ArrayList<String>> entry : cachedSection.entrySet()){
            //loop through each line of the entry
            for(String line : entry.getValue()){
                //does it contain any of the initial variables
                if(entry.getKey().equals("extends AsyncTask")) {
                    break;
                }
                for(String variable : initVars){
                    if(line.contains(variable) && !usedVariable.contains(variable)) {
                        usedVariable.add(variable);
                    }
                }
            }

        }


        return usedVariable;
    }


}
