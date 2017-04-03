import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Toby on 3/25/17.
 */
public class LoaderCallbackBuilder {

    ArrayList<String> refact = null;
    HashMap<String, String> savedVariable = null;
    LinkedHashMap<String, ArrayList<String>> cachedSection = null;


    //region Build Loader Callback

    //===========================================================================================
    //This section hold the logic for constructing the loader callback abstract class
    //===========================================================================================

    public ArrayList<String> GenerateLoaderCallbacks(ArrayList<String> refact,
                                                     HashMap<String, String> savedVariable,
                                                     LinkedHashMap<String, ArrayList<String>> cachedSection){

        //save the variables for later use
        this.savedVariable = savedVariable;
        this.cachedSection = cachedSection;

        ArrayList<String> callbackSection = new ArrayList<>();
        this.refact = refact;

        callbackSection.add("\r");
        callbackSection.add("\t//*** AUTO_REFACTORED: Loader Callback abstract class ***\r");
        callbackSection.add("\tprivate LoaderManager.LoaderCallbacks<String> loaderCallbacks " +
                            "= new LoaderManager.LoaderCallbacks<String>() {\r\r");
        callbackSection.addAll(BuildOnCreateLoader());
        callbackSection.addAll(BuildOnLoadFinished());
        callbackSection.addAll(BuildOnLoaderReset());
        callbackSection.add("\t};\r");
        callbackSection.add("\r");
        callbackSection.add("\r");
        return callbackSection;
    }


    private ArrayList<String> BuildOnCreateLoader(){

        ArrayList<String> section = new ArrayList<>();
        section.add("\t@Override\r");
        section.add("\tpublic Loader<String> onCreateLoader(int id, Bundle bundle){\r");

        //Use StringBuilder to handle varying parameter
        StringBuilder instance = new StringBuilder();
        instance.append("return new AsyncTaskLoaderRunner(" + GetContext() +".this, bundle");

        for(String param : HandleVariables.GetParams(savedVariable, cachedSection)){
            instance.append(", " + param);
        }
        instance.append(");");
        section.add("\t\t" + instance.toString() + "\r");
        section.add("\t}\r\r");


        return section;
    }

    private ArrayList<String> BuildOnLoadFinished(){
        int loaderId = 1;
        String type1 = "String", type2 = "String";
        ArrayList<String> section = new ArrayList<>();

        section.add("\t@Override\r");
        section.add("\tpublic void onLoadFinished(Loader<" + type1 + "> listLoader, " + type2 + " strings){\r");
        section.add("\t\tgetSupportLoaderManager().destroyLoader(" + loaderId +");\r");
        section.add("\t}\r\r");

        return section;
    }

    private ArrayList<String> BuildOnLoaderReset(){
        String type = "String";
        ArrayList<String> section = new ArrayList<>();

        section.add("\tOverride\r");
        section.add("\tpublic void onLoaderReset(Loader<" + type + "> listLoader){}\r");

        return section;
    }

    //endregion


    //region Determine Parameters

    //===========================================================================================
    //Private functions to determine the parameters for the build functions
    //===========================================================================================

    //get the activity context and pass it to the loader
    private String GetContext(){
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

    //endregion


}
