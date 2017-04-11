# AysncTaskRefactor

BACKGROUND:

Async Tasks is a great Android mechanisms for performing quick background tasks, however there is a caveat.  During configuration changes Async Tasks keep running even after the activity is destroyed.  This refactoring tool takes in a file written with Async Tasks and converts it to Async Task Loaders.

The main philosophy of Async Task Loaders is almost exactly the same as Async Tasks without the out the implicit memory leak.  Async Task Loader uses a static inner class that has a weak reference to the activity.  Upon destroying the activity, the reference is also destroyed.

Within Async Tasks there are four signature functions:

1. onPreExecute()
2. onProgressUpdate()
3. doInBackground()
4. onPostExecute

This tool converts each of these functions the following with the exception of onProgressUpdate():

1. onPreExecute() -> onStartLoading()
2. doInBackground() -> loadInBackground()
3. onPostExecute() -> deliverResults()

With onProgressUpdate(), Async Task calls publishProgress(string) in doInBackground.  Async Task Loader does not have an equivalent function.  This was handled by using a handler within the static class.  Even though handlers typically hold an implicit reference to the activity, it is memory leak safe within the static inner class.

This refactoring tool create a function called publishProgress() that will mimic onProgressUpdate() within the static inner class.


HOW TO USE THIS CODE:

(1) Navigate to the main() function located AsyncTaskRefactorMain.java

(2) The first line in the main() function informs the user to enter the path of the file that needs to be refactored

(3) The java code will print "done" once it has finished refactoring

(4) The output file will be in the same directory of the program named RefactoredOutput.java
