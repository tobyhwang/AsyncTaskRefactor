import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Toby on 3/26/17.
 */
public class HandleOnCreate {


    //Get the name of the async task class
    public String GetNameOfAsyncTaskFunction(ArrayList<String> section){

        for(String line : section)
        {
            if(line.contains("extends AsyncTask<"))
            {
                line.trim();
                return line.split("\\s+")[3];
            }
        }

        return null;

    }

    //Create an instance of async task loader in the abstract class
    public ArrayList<String> HandleInstanceOfAsyncTask(ArrayList<String> refact, String className){
        //Save the name of the instance
        String instance = "";
        String params = "";
        int lastLocation = -1;

        //locate the variable that gets executed by AsyncTasks
        for(String line : refact){
            if(line.contains(".execute(")){
                 params = GetParametersForExecute(line);
                 break;
            }
            lastLocation++;
        }

        //locate the initial instance
        for(String line : refact){
            if(line.contains(className)){
                String[] in = line.split("\\s+");
                for(int i = 0; i < in.length; i++){
                    if(in[i].contains("=")){
                        instance = in[i-1];
                        break;
                    }
                }
            }
        }

        //remove each time the instance of async loader is called
        Iterator<String> iter = refact.iterator();

        while(iter.hasNext()){
            if(iter.next().contains(instance)){
                iter.remove();
            }
        }


        //add the additional section
        ArrayList<String> loaderSection = BuildOnCreate(params);
        refact.addAll(lastLocation, loaderSection);


        return refact;
    }

    //Get the parameters passed in the async task code
    private String GetParametersForExecute(String execute){
        StringBuilder params = new StringBuilder(execute);

        int start = params.indexOf("(");
        int end = params.indexOf(")");

        String value = params.substring(start + 1, end);

        return value;

    }

    //recreate onCreate for async task loader
    public ArrayList<String> BuildOnCreate(String params){
        ArrayList<String> section = new ArrayList<>();
        section.add("\t\tBundle extras = new Bundle();\r");
        section.add("\t\tString[] params = new String[] {" + params + "};\r");
        section.add("\t\textras.putStringArray(\"list\", params);\r");
        section.add("\t\tgetSupportLoaderManager().initLoader(1, extras, loaderCallbacks);\r");
        return section;
    }


    //Get the global variables, but don't remove them
    public ArrayList<String> GetGlobalVariables(ArrayList<String> refact){
        ArrayList<String> globalVariables = new ArrayList<>();
        //Get the list of declared global variables
        for(int i = 0; i < refact.size(); i++){

            //find the first bracket to indicate the class
            if(refact.get(i).contains("{")){
                //don't save the main class declaration
                i++;
                //save each line until it reaches the first function
                for(int j = i; j < refact.size(); j++)
                {
                    if(refact.get(j).contains("@Override") || refact.get(j).contains("onCreate")){
                        break;
                    }
                    globalVariables.add(refact.get(j));
                }
                break;
            }
        }

        globalVariables = CleanEmptyLines(globalVariables, "\r");


        return globalVariables;
    }


    //function to remove any empty lines
    public ArrayList<String> CleanEmptyLines(ArrayList<String> section, String instance)
    {

        Iterator<String> iter = section.iterator();

        while(iter.hasNext()){
            if(iter.next().equals(instance)){
                iter.remove();
            }
        }

        return section;
    }


}
