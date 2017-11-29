import java.util.Scanner;

public class AsyncTaskRefactorMain {


    /*
        This program transforms AsyncTask to AsyncTaskLoaders.
        Written by: Tobin Hwang
    */
    public static void main(String[] args) {

        //Create an instance of the main transformation process
        AsyncTaskRefactorProcessor process = new AsyncTaskRefactorProcessor();

        /*
            Wait for the user to enter the location of the file. User needs to enter in the current location of the
            file as shown as an example String filePath =
            "/Users/toby/Dropbox/Grad School/CS597/AsyncTaskRefactor/AsyncTaskRefactor/AsyncTaskRefactorMain.java";
        */

        //Prompt the user to enter the file path
        System.out.println("Enter the FilePath: ");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.nextLine();

        //Run the transformation
        process.Run(filePath);

    }

}
