//import com.sun.javafx.tk.quantum.GlassAppletWindow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class AsyncTaskRefactorProcessor {

    //save the input file
    private static ArrayList<String> inputFile = new ArrayList<>();

    //as the code is refactored, write it to this variable
    private static ArrayList<String> refact = new ArrayList<>();

    //cache the global variables from async task
    private static HashMap<String, String> cachedGlobalVariables = new HashMap<>();

    //cache for each section of the async task
    private LinkedHashMap<String, ArrayList<String>> asyncCached = new LinkedHashMap<>();

    //list of each section contained in async task
    private static String[] asyncFunc = new String[] {"onPreExecute", "doInBackground",
            "onProgressUpdate", "onPostExecute", "extends AsyncTask"};

    //List of import statements needed for AsyncTaskLoaders
    private static String[] libraries= {"import android.content.Context;", "import android.os.Handler;",
                                "import android.os.Looper;", "import android.support.v4.app.LoaderManager;",
                                "import android.support.v4.content.AsyncTaskLoader;",
                                "import android.support.v4.content.Loader;"};

    //constants for functions that need to be built
    private static final int LOADER_CALLBACK = 0;
    private static final int LOADER_CLASS = 1;


    //Import the correct libraries
    private void ImportLibrary(){
        for(int i = 0; i < refact.size(); i++){
            if(refact.get(i).contains("import")){
                for(String library : libraries){
                    refact.add(i, library + "\r");
                }
                break;
            }
        }


    }

    /*For each of the async task functions cache the respective sections*/
    private void CacheAsyncTaskFunctions(){

        //handle the functions dealing with async
        for(String func : asyncFunc){

            //cache the removed sections
            AsyncTaskRemoval remove = new AsyncTaskRemoval(refact, func);
            remove.RemoveSection();

            //remove the section from the original code
            refact = remove.refact;
            asyncCached.put(func, remove.removedSection);
        }

        //prompt user
        System.out.println("(1) Caching all the Async Functions and references to global variables\n");
        DisplaySleep();

    }

    /* Recreate the onCreate function the original async task code*/
    private void HandleAysncFromOnCreate(){
        HandleOnCreate create = new HandleOnCreate();

        ArrayList<String> GlobalVariables = create.GetGlobalVariables(refact);

        for(String variable : GlobalVariables){
            String [] var_list = variable.trim().split("\\s+");
            String var = var_list[var_list.length-1];
            cachedGlobalVariables.put(variable, var);
        }

        String className = create.GetNameOfAsyncTaskFunction(asyncCached.get("extends AsyncTask"));

        //remove any instances associated with async task
        refact = create.HandleInstanceOfAsyncTask(refact, className);

        //prompt user
        System.out.println("(2) Reformatting the activity onCreate() function\n");
        DisplaySleep();

    }

    /* Two sections need to be built
    * (1) build the abstract class of the loader callback
    * (2) construct the static inner class that imitates async tasks*/
    private void HandleBuilder(int select){
        ArrayList<String> generatedSection = null;

        //construct the loader callback abstract class
        if(select == 0) {
            LoaderCallbackBuilder build = new LoaderCallbackBuilder();
            generatedSection = build.GenerateLoaderCallbacks(refact, cachedGlobalVariables, asyncCached);
            System.out.println("(3) Generating the Loader Callback abstract class\n");
            DisplaySleep();
        }

        //create the main inner class of the async task loader
        else if(select == 1) {
            AsyncTaskLoaderClassBuilder build = new AsyncTaskLoaderClassBuilder();
            generatedSection = build.GenerateAsyncClass(refact, asyncFunc, asyncCached, cachedGlobalVariables);
            System.out.println("(4) Generating the AsyncTaskLoader subclass\n\n");
            DisplaySleep();
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

    //sleeps the program for the user to see the progress of the transformation
    private void DisplaySleep(){

        try {
            TimeUnit.SECONDS.sleep(1);
        }
        catch(InterruptedException e){}
    }

    /*
        Process method for the entire transofrmation
    */
    public void Run(String filePath){

        //read in the input file from the user declared path
        ReadInputFile(filePath);

        //save data for refactoring process
        refact = inputFile;

        //Prompt the user and wait
        System.out.println("Beginning refactoring process...\n\n");
        DisplaySleep();

        //refactor the entire async task file
        ImportLibrary();
        CacheAsyncTaskFunctions();
        HandleAysncFromOnCreate();
        HandleBuilder(LOADER_CALLBACK);
        HandleBuilder(LOADER_CLASS);

        //write to the new file
        try{
            PrintWriter writer = new PrintWriter("RefactoredOutput.java", "UTF-8");

            for(String in: inputFile){
                writer.print(in);
            }
            writer.close();
        }
        catch(IOException ex){
            System.out.println(ex);
        }

        //let the user know that the program has finished running
        System.out.println("AsyncTask Refactoring Process Complete!");
    }

}
