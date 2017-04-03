import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * Created by Toby on 3/26/17.
 */
public class AsyncTaskLoaderClassBuilder {

    String type = "String";
    ArrayList<String> refact = null;
    String[] savedSection = null;
    LinkedHashMap<String, ArrayList<String>> cachedAsyncSection = null;
    HashMap<String, String> savedVariables = null;
    //region Build the Async Task Loader nested class


    //===========================================================================================
    //This section hold the logic for constructing the nested Async Task Loader class
    //===========================================================================================

    //Build AsyncTaskLoader class
    public ArrayList<String> GenerateAsyncClass(ArrayList<String> refact,
                                                String[] savedSection,
                                                LinkedHashMap<String, ArrayList<String>> cachedAsyncSection,
                                                HashMap<String, String> savedVariables){
        this.refact = refact;
        this.savedSection = savedSection;
        this.cachedAsyncSection = cachedAsyncSection;
        this.savedVariables = savedVariables;

        ArrayList<String> asyncClass = new ArrayList<>();
        asyncClass.add("\t//*** AUTO_REFACTORED: Loader Class ***\r");
        asyncClass.add("\tpublic static class AsyncTaskLoaderRunner extends AsyncTaskLoader<" + type + ">{");
        asyncClass.addAll(VariableSection());
        asyncClass.addAll(ConstructorSection());
        asyncClass.addAll(OnStartLoading());
        asyncClass.addAll(LoadInBackground());
        asyncClass.addAll(PublishProgress());
        asyncClass.addAll(DeliverResults());
        asyncClass.add("\t}");

        return asyncClass;

    }


    private ArrayList<String> VariableSection(){
        ArrayList<String> section = GetBody(GetHashMapValueByKey("extends AsyncTask"));

        //TODO: possibly create a new class to handle this instead of borrowing
        HandleOnCreate emptyLines = new HandleOnCreate();
        section = emptyLines.CleanEmptyLines(section, "\r");
        section.add(0, "\r\r");

        //get the variables saved for the class
        ArrayList<String> initializeVariable = new ArrayList<>();

        //TODO: make this better - save each of the hashmap functions to index
        ArrayList<String> initVar = new ArrayList<>(savedVariables.keySet());
        for(String line : initVar) {
            for (String var : HandleVariables.GetParams(savedVariables, cachedAsyncSection)) {
                if(line.contains(var)){
                    initializeVariable.add(line);
                }
            }
        }


        for(String init :initializeVariable){
            section.add("\t\t" + init.trim() + "\r");
        }

        return section;
    }

    private ArrayList<String> ConstructorSection(){
        ArrayList<String> section = new ArrayList<>();

        ArrayList<String> parameters = HandleVariables.GetParams(savedVariables,cachedAsyncSection);
        String paramsLine = "";

        for(String param : parameters){
            paramsLine += (", " + param);
        }

        section.add("\r");
        section.add("\t\tpublic AsyncTaskLoaderRunner(Context context, Bundle bundle " + paramsLine + "){\r");
        section.add("\t\t\tsuper(context);\r");
        section.add("\t\t\tthis.context=context;\r");
        section.add("\t\t\tthis.bundle=bundle;\r");

        for(String param: parameters){
            section.add("\t\t\tthis." + param + "=" + param + ";\r");
        }
        section.add("\t\t}\r");


        return section;
    }

    private ArrayList<String> OnStartLoading(){
        ArrayList<String> section = new ArrayList<>();
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic void onStartLoading(){\r\r");
        section.addAll(GetBody(GetHashMapValueByKey("onPreExecute")));
        section.add("\t\t\tForceLoad();\r");
        section.add("\t\t}\r");
        return section;
    }

    private ArrayList<String> LoadInBackground(){
        ArrayList<String> section = new ArrayList<>();
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic String loadInBackground(){\r\r");
        section.addAll(GetBody(GetHashMapValueByKey("doInBackground")));
        section.add("\t\t}\r");
        return section;
    }

    private ArrayList<String> PublishProgress(){
        ArrayList<String> section = new ArrayList<>();
        section.add("\r");
        section.add("\t\tprotected void publishProgress( String str){\r");
        section.add("\t\t\tfinal String update = str;\r");
        section.add("\t\t\tHandler UIHandler = new Handler(Looper.getMainLooper())\r");
        section.add("\t\t\tUIHandler.post(new Runnable() {\r");
        section.add("\t\t\t\t@Override\r");
        section.add("\t\t\t\tpublic void run() {\r");
        section.addAll(AddTab(GetBody(GetHashMapValueByKey("onProgressUpdate"))));
        section.add("\t\t\t\t}\r");
        section.add("\t\t\t});\r");
        section.add("\t\t}\r");
        return section;
    }

    private ArrayList<String> DeliverResults(){
        ArrayList<String> section = new ArrayList<>();
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic void deliverResult(" + type + " data){\r");
        section.add("\t\t\tsuper.deliverResult(data);\r");
        section.addAll(GetBody((GetHashMapValueByKey("onPostExecute"))));
        section.add("\t\t}\r\r");
        return section;
    }

    //endregion


    //region
    //===========================================================================================
    //Private functions that determine the parameters for the build functions
    //===========================================================================================

    //TODO: Create the variables needed for the class
    private ArrayList<String> GetVariables(){
        ArrayList<String> initVariables = new ArrayList<>();
        return initVariables;
    }

    private ArrayList<String> GetHashMapValueByKey(String function){

        int index = 0;
        for(int i = 0; i < savedSection.length; i++){
            if(savedSection[i].equals(function)){
                index = i;
                break;
            }
        }

        //workaround to index into a
        ArrayList<ArrayList<String>> indexedMap = new ArrayList<>(cachedAsyncSection.values());
        return indexedMap.get(index);
    }

    //gets the body of the the async task functions
    private ArrayList<String> GetBody(ArrayList<String> sec){
        ArrayList<String> body = new ArrayList<>();

        //find the index of the first open bracket
        int first = 0;
        for(String line : sec){
            if(line.contains("{")){
                break;
            }
            first++;
        }

        //find the last index
        int last = 0;
        for(int i = sec.size() - 1; i > first; i--){
            if(sec.get(i).contains("}")){
                last = i;
                break;
            }
        }

        //grab the body and store it
        for(int i = first + 1; i < last; i++) {
            body.add(sec.get(i));
        }

        return body;
    }

    //Generic function to add a single additional tab to each line
    private ArrayList<String> AddTab(ArrayList<String> tab){

        ArrayList<String> addTab = new ArrayList<>();
        for(String line : tab)
        {
            addTab.add("\t" + line);
        }

        return addTab;
    }


    //endregion


}
