import java.util.*;

//===========================================================================================
//This class holds the logic for handling the variables from the AsyncTask Implementation
//===========================================================================================


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


    //get the activity context and pass it to the loader
    public static String GetContext(ArrayList<String> refact){
        String context = "";

        //start at the top of the file
        //find the first class, it will contain the context
        for(String line : refact){
            if(line.contains("public class")){
                context = line.trim().split("\\s+")[2];
                break;
            }
        }

        return context;
    }

}
