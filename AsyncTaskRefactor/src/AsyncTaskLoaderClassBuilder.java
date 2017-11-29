import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

//===========================================================================================
//This class holds the logic for constructing the nested AsyncTaskLoader static subclass
//===========================================================================================

public class AsyncTaskLoaderClassBuilder {

    String type = "String";
    ArrayList<String> refact = null;
    String[] savedSection = null;
    LinkedHashMap<String, ArrayList<String>> cachedAsyncSection = null;
    HashMap<String, String> savedVariables = null;

    //region ***Build the Async Task Loader nested class***

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
        asyncClass =  ReplaceContext(asyncClass);

        return asyncClass;

    }

    //change any reference to the activity by changing it to say the context
    private ArrayList<String> ReplaceContext(ArrayList<String> asyncClass){
        int start = 0;
        int end = asyncClass.size() - 1;
        for(int i = 0; i < end; i++){
            if(asyncClass.get(i).contains("public static class AsyncTaskLoader extends AsyncTaskLoader")){
                start = i;
            }
        }

        String context = HandleVariables.GetContext(refact) + ".this";

        for(int i = start; i < end; i++){
            if(asyncClass.get(i).contains(context)){
                asyncClass.set(i, asyncClass.get(i).replace(context, "context"));
            }
        }
        return asyncClass;
    }

    //Get the variables needed for building the inner async task loader section
    private ArrayList<String> VariableSection(){
        String param = "";
        ArrayList<String> section = GetBody(GetHashMapValueByKey("extends AsyncTask"), param,false);

        HandleOnCreate emptyLines = new HandleOnCreate();
        section = emptyLines.CleanEmptyLines(section, "\r");
        section.add(0, "\r\r");

        //get the variables saved for the class
        ArrayList<String> initializeVariable = new ArrayList<>();
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

        section.add("\t\tprivate Context context;\r");
        section.add("\t\tprivate Bundle bundle;\r");

        return section;
    }

    //Create the constructor for the inner class
    private ArrayList<String> ConstructorSection(){
        ArrayList<String> section = new ArrayList<>();

        ArrayList<String> parameters = HandleVariables.GetParams(savedVariables,cachedAsyncSection);
        String paramsLine = "";


        //walk through the saved variable dict and get the parameter
        for(String var : parameters){
            for(String key : savedVariables.keySet()){
                if(key.contains(var)){
                    key.trim();
                    String [] temp = key.split("\\s+");
                    String semi = temp[temp.length-1];
                    StringBuilder str = new StringBuilder(semi);
                    semi = str.replace(semi.length()-1, semi.length(), "").toString();
                    paramsLine += (", " + temp[temp.length - 2] + " " + semi);
                }
            }
        }


        section.add("\r");
        section.add("\t\tpublic AsyncTaskLoaderRunner(Context context, Bundle bundle " + paramsLine + "){\r");
        section.add("\t\t\tsuper(context);\r");
        section.add("\t\t\tthis.context = context;\r");
        section.add("\t\t\tthis.bundle=bundle;\r");

        for(String param: parameters){
            section.add("\t\t\tthis." + param + "=" + param + ";\r");
        }
        section.add("\t\t}\r");

        return section;
    }

    //Create the onStartLoading() function - equivalent to onPreExecute()
    private ArrayList<String> OnStartLoading(){
        ArrayList<String> section = new ArrayList<>();
        String param = GetFunParam("onPreExecute");
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic void onStartLoading(){\r\r");
        section.addAll(GetBody(GetHashMapValueByKey("onPreExecute"), param, false));
        section.add("\t\t\tforceLoad();\r");
        section.add("\t\t}\r");
        return section;
    }

    //Create the LoadInBackground() function - equivalent to doInBackground()
    private ArrayList<String> LoadInBackground(){
        ArrayList<String> section = new ArrayList<>();
        String param = GetFunParam("doInBackground");
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic String loadInBackground(){\r\r");
        section.add("\t\t\tString[] params = bundle.getStringArray(\"list\");\r");
        section.addAll(GetBody(GetHashMapValueByKey("doInBackground"), param, false));
        section.add("\t\t}\r");
        return section;
    }

    //Create the PublishProgress() Function - equivalent to onProgressUpdate()
    private ArrayList<String> PublishProgress(){
        ArrayList<String> section = new ArrayList<>();
        String param = GetFunParam("onProgressUpdate");
        section.add("\r");
        section.add("\t\tprotected void publishProgress( String str){\r");
        section.add("\t\t\tfinal String update = str;\r");
        section.add("\t\t\tHandler UIHandler = new Handler(Looper.getMainLooper());\r");
        section.add("\t\t\tUIHandler.post(new Runnable() {\r");
        section.add("\t\t\t\t@Override\r");
        section.add("\t\t\t\tpublic void run() {\r");
        section.addAll(AddTab(GetBody(GetHashMapValueByKey("onProgressUpdate"), param, true)));
        section.add("\t\t\t\t}\r");
        section.add("\t\t\t});\r");
        section.add("\t\t}\r");
        return section;
    }

    //Create Deliver results function - equivalent to onPostExecute()
    private ArrayList<String> DeliverResults(){
        ArrayList<String> section = new ArrayList<>();
        String param = GetFunParam("onPostExecute");
        section.add("\r");
        section.add("\t\t@Override\r");
        section.add("\t\tpublic void deliverResult(" + type + " params){\r");
        section.add("\t\t\tsuper.deliverResult(params);\r");
        section.addAll(GetBody(GetHashMapValueByKey("onPostExecute"), param, false));
        section.add("\t\t}\r\r");
        return section;
    }

    //endregion


    //region
    //===========================================================================================
    //Private functions that determine the parameters for the build functions
    //===========================================================================================

    //index into the hashmap toget the specific value
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
    //handle the original parameters
    private ArrayList<String> GetBody(ArrayList<String> sec, String param, boolean isProgressUpdate){
        ArrayList<String> body = new ArrayList<>();

        //loop through and replace the parameters
        if(!(param.equals(""))) {
            for (int i = 0; i < sec.size(); i++) {
                if(sec.get(i).contains(param)) {

                    if (isProgressUpdate) {
                        String onProgUp = sec.get(i);
                        if(onProgUp.contains("[") && onProgUp.contains("]")){
                            int firstBraket = onProgUp.indexOf("[");
                            int lastBracket = onProgUp.indexOf("]");
                            StringBuilder temp = new StringBuilder(onProgUp);
                            String sub = temp.substring(firstBraket, lastBracket + 1);
                            sec.set(i, sec.get(i).replace(sub, ""));
                        }
                        sec.set(i, sec.get(i).replace(param, "update"));
                    }
                    else {
                        sec.set(i, sec.get(i).replace(param, "params"));
                    }
                }
            }
        }

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

    //attempts to locate if the parameter exists in the function
    private String GetFunParam(String function)
    {
        //Get the parameters
        for(String line : GetHashMapValueByKey(function)){
            if(line.contains(function)){
                StringBuilder temp = new StringBuilder(line);
                //go backwards to get the string param (by character)
                int parLast = temp.indexOf(")");

                for(int i = parLast; i >= 0; i--){
                    if(temp.substring(parLast-1, parLast + 1).equals("()")){
                        break;
                    }
                    else if(temp.charAt(i) == ' '){
                        return temp.substring(i+1, parLast).toString();
                    }
                }
            }
        }

        return "";
    }

    //endregion


}
