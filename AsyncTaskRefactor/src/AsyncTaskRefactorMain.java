import com.sun.javafx.tk.quantum.GlassAppletWindow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Toby on 3/25/17.
 */
public class AsyncTaskRefactorMain {

    private static ArrayList<String> inputFile = new ArrayList<>();
    private static ArrayList<String> refact = new ArrayList<>();
    private static HashMap<String, String> cachedGlobalVariables = new HashMap<>();
    private LinkedHashMap<String, ArrayList<String>> asyncCached = new LinkedHashMap<>();
    private static String[] asyncFunc = new String[] {"onPreExecute", "doInBackground",
            "onProgressUpdate", "onPostExecute", "extends AsyncTask"};

    private static final int LOADER_CALLBACK = 0;
    private static final int LOADER_CLASS = 1;


    public void CacheAsyncTaskFunctions(){

        //handle the functions dealing with async
        for(String func : asyncFunc){

            //cache the removed sections
            AsyncTaskRemoval remove = new AsyncTaskRemoval(refact, func);
            remove.RemoveSection();

            //remove the section from the original code
            refact = remove.refact;
            asyncCached.put(func, remove.removedSection);
        }


    }


    public void HandleAysncFromOnCreate(){
        HandleOnCreate create = new HandleOnCreate();

        ArrayList<String> GlobalVariables = create.GetGlobalVariables(refact);

        for(String variable : GlobalVariables){
            String var = variable.trim().split("\\s+")[2];
            cachedGlobalVariables.put(variable, var);
        }

        String className = create.GetNameOfAsyncTaskFunction(asyncCached.get("extends AsyncTask"));

        //remove any instances associated with async task
        refact = create.HandleInstanceOfAsyncTask(refact, className);
    }

    public void HandleBuilder(int select){
        ArrayList<String> generatedSection = null;

        if(select == 0) {
            LoaderCallbackBuilder build = new LoaderCallbackBuilder();
            generatedSection = build.GenerateLoaderCallbacks(refact, cachedGlobalVariables, asyncCached);
        }
        else if(select == 1) {
            AsyncTaskLoaderClassBuilder build = new AsyncTaskLoaderClassBuilder();
            generatedSection = build.GenerateAsyncClass(refact, asyncFunc, asyncCached, cachedGlobalVariables);
        }

        //look for the last bracket and insert right above it
        int lastBracket = refact.size() - 1;

        //go backwards until we find the closing bracket
        for(int i = lastBracket; i > 0; i--){
            if(refact.get(i).contains("}")){
                lastBracket = i-1;
                break;
            }
        }

        refact.addAll(lastBracket, generatedSection);

    }



    //read the input file and save it to a list
    private void ReadInputFile(String filePath){
        String line;

        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            while((line = br.readLine()) != null){
                //add a return at each index
                inputFile.add(line + "\r");
            }


        }catch(IOException ex){
            System.out.print(ex);
        }

    }


    public static void main(String[] args){
        String filePath = "/Users/Toby/Box Sync/CS597/Research Project/AsyncTaskRefactorMain.java";

        AsyncTaskRefactorMain async = new AsyncTaskRefactorMain();
        async.ReadInputFile(filePath);
        refact = inputFile;

        //refactor the entire async task file
        async.CacheAsyncTaskFunctions();
        async.HandleAysncFromOnCreate();
        async.HandleBuilder(LOADER_CALLBACK);
        async.HandleBuilder(LOADER_CLASS);


        //write to the new file
        try{
            PrintWriter writer = new PrintWriter("Refactored.java", "UTF-8");

            for(String in: inputFile){
                writer.print(in);
            }
            writer.close();
        }
        catch(IOException ex){
            System.out.println(ex);
        }

        System.out.println("Done");
    }

}
