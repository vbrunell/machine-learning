// Google Machine Learning Prediction API
// Original code by Yaniv Inbar
// Modified by Victor Brunell

package com.google.api.services.prediction;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Insert;
import com.google.api.services.prediction.model.Insert2;
import com.google.api.services.prediction.model.Output;
import com.google.api.services.storage.StorageScopes;

import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

public class PredictionSample {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "HelloPrediction";

  /** Specify the Cloud Storage location of the training data. */
  static final String STORAGE_DATA_LOCATION = "your_bucket/your_data.txt";
  static final String MODEL_ID = "your_model_id";

  /**
   * Specify your Google Developers Console project ID, your service account's email address, and
   * the name of the P12 file you copied to src/main/resources/.
   */
  static final String PROJECT_ID = "your_project_id";
  static final String SERVICE_ACCT_EMAIL = "your_service_acct_email";
  static final String SERVICE_ACCT_KEYFILE = "your_keyfile";

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Authorizes the installed application to access user's protected data. */
  private static GoogleCredential authorize() throws Exception {
    return new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCT_EMAIL)
        .setServiceAccountPrivateKeyFromP12File(new File(
            PredictionSample.class.getResource("/"+SERVICE_ACCT_KEYFILE).getFile()))
        .setServiceAccountScopes(Arrays.asList(PredictionScopes.PREDICTION,
                                               StorageScopes.DEVSTORAGE_READ_ONLY))
        .build();
  }

  private static void run() throws Exception {
    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    // authorization
    GoogleCredential credential = authorize();
    Prediction prediction = new Prediction.Builder(
        httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    train(prediction);

    // These are some example predictions using the Conan text
    // You can use whatever you want as long as you train your nn
    // with the appropriate data
    predict(prediction, "gray traveling could recover faster board quivered");
    predict(prediction, "Tanasul will said much that they it Pallantides");
    predict(prediction, "Xaltotun west crumpled but must thought");
  }

  private static void train(Prediction prediction) throws IOException {
    Insert trainingData = new Insert();
    trainingData.setId(MODEL_ID);
    trainingData.setStorageDataLocation(STORAGE_DATA_LOCATION);
    prediction.trainedmodels().insert(PROJECT_ID, trainingData).execute();
    System.out.println("Training started.");
    System.out.print("Waiting for training to complete");
    System.out.flush();

    int triesCounter = 0;
    Insert2 trainingModel;
    while (triesCounter < 100) {
      // NOTE: if model not found, it will throw an HttpResponseException with a 404 error
      try {
        HttpResponse response = prediction.trainedmodels().get(PROJECT_ID, MODEL_ID).executeUnparsed();
        if (response.getStatusCode() == 200) {
          trainingModel = response.parseAs(Insert2.class);
          String trainingStatus = trainingModel.getTrainingStatus();
          if (trainingStatus.equals("DONE")) {
            System.out.println();
            System.out.println("Training completed.");
            System.out.println(trainingModel.getModelInfo());
            return;
          }
        }
        response.ignore();
      } catch (HttpResponseException e) {
      }

      try {
        // 5 seconds times the tries counter
        Thread.sleep(5000 * (triesCounter + 1));
      } catch (InterruptedException e) {
        break;
      }
      System.out.print(".");
      System.out.flush();
      triesCounter++;
    }
    error("ERROR: training not completed.");
  }

  private static void error(String errorMessage) {
    System.err.println();
    System.err.println(errorMessage);
    System.exit(1);
  }

  private static void predict(Prediction prediction, String text) throws IOException {
    Input input = new Input();
    InputInput inputInput = new InputInput();
    inputInput.setCsvInstance(Collections.<Object>singletonList(text));
    input.setInput(inputInput);
    Output output = prediction.trainedmodels().predict(PROJECT_ID, MODEL_ID, input).execute();

    // Here we show what you asked to predict
    // and the code that was predicted by the model
    System.out.println("Text: " + text);
	 String mCode = output.getOutputLabel();
    System.out.println("Predicted code: " + replaceNumbers(mCode));
  }

// -------------- Number Conversion --------------a
//
// The Google Predict API will automatically use a regression model
// if your data contains an integer column.
// To avoid this, the numbers have been converted to words for training.
// When querying the predictive model, the number must be converted back
// from English to an integer.
//
// If you are not using numerical data with a categorical model
// simply do not convert your integers to English and do not
// call replaceNumbers() to convert the English back to an integer value

   public static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
   public static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
   public static final String[] TEENS = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen",
                                          "sixteen", "seventeen", "eighteen", "nineteen"};
   public static final String[] MAGNITUDES = {"hundred", "thousand", "million", "point"};
   public static final String[] ZERO = {"zero", "oh"};

   public static String replaceNumbers (String input) {
   	 input = input.replaceAll(",","");
   	 input = input.replaceAll("\\band\\b","");
   	 input = input.replaceAll("-"," ");
       //System.out.println("\n" + input + "\n");
       String result = "";
       String[] decimal = input.split(MAGNITUDES[3]);
       String[] millions = decimal[0].split(MAGNITUDES[2]);

       for (int i = 0; i < millions.length; i++) {
           String[] thousands = millions[i].split(MAGNITUDES[1]);

           for (int j = 0; j < thousands.length; j++) {
               int[] triplet = {0, 0, 0};
               StringTokenizer set = new StringTokenizer(thousands[j]);

               if (set.countTokens() == 1) { //If there is only one token given in triplet
                   String uno = set.nextToken();
                   triplet[0] = 0;
                   for (int k = 0; k < DIGITS.length; k++) {
                       if (uno.equals(DIGITS[k])) {
                           triplet[1] = 0;
                           triplet[2] = k + 1;
                       }
                       if (uno.equals(TENS[k])) {
                           triplet[1] = k + 1;
                           triplet[2] = 0;
                       }
                   }
               }


               else if (set.countTokens() == 2) {  //If there are two tokens given in triplet
                   String uno = set.nextToken();
                   String dos = set.nextToken();
                   if (dos.equals(MAGNITUDES[0])) {  //If one of the two tokens is "hundred"
                       for (int k = 0; k < DIGITS.length; k++) {
                           if (uno.equals(DIGITS[k])) {
                               triplet[0] = k + 1;
                               triplet[1] = 0;
                               triplet[2] = 0;
                           }
                       }
                   }
                   else {
                       triplet[0] = 0;
                       for (int k = 0; k < DIGITS.length; k++) {
                           if (uno.equals(TENS[k])) {
                               triplet[1] = k + 1;
                           }
                           if (dos.equals(DIGITS[k])) {
                               triplet[2] = k + 1;
                           }
                       }
                   }
               }

               else if (set.countTokens() == 3) {  //If there are three tokens given in triplet
                   String uno = set.nextToken();
                   String dos = set.nextToken();
                   String tres = set.nextToken();
                   for (int k = 0; k < DIGITS.length; k++) {
                       if (uno.equals(DIGITS[k])) {
                           triplet[0] = k + 1;
                       }
                       if (tres.equals(DIGITS[k])) {
                           triplet[1] = 0;
                           triplet[2] = k + 1;
                       }
                       if (tres.equals(TENS[k])) {
                           triplet[1] = k + 1;
                           triplet[2] = 0;
                       }
                   }
               }

               else if (set.countTokens() == 4) {  //If there are four tokens given in triplet
                   String uno = set.nextToken();
                   String dos = set.nextToken();
                   String tres = set.nextToken();
                   String cuatro = set.nextToken();
                   for (int k = 0; k < DIGITS.length; k++) {
                       if (uno.equals(DIGITS[k])) {
                           triplet[0] = k + 1;
                       }
                       if (cuatro.equals(DIGITS[k])) {
                           triplet[2] = k + 1;
                       }
                       if (tres.equals(TENS[k])) {
                           triplet[1] = k + 1;
                       }
                   }
               }
               else {
                   triplet[0] = 0;
                   triplet[1] = 0;
                   triplet[2] = 0;
               }

               result = result + Integer.toString(triplet[0]) + Integer.toString(triplet[1]) + Integer.toString(triplet[2]);
           }
       }

       if (decimal.length > 1) {  //The number is a decimal
           StringTokenizer decimalDigits = new StringTokenizer(decimal[1]);
           result = result + ".";
           System.out.println(decimalDigits.countTokens() + " decimal digits");
           while (decimalDigits.hasMoreTokens()) {
               String w = decimalDigits.nextToken();
               System.out.println(w);

               if (w.equals(ZERO[0]) || w.equals(ZERO[1])) {
                   result = result + "0";
               }
               for (int j = 0; j < DIGITS.length; j++) {
                   if (w.equals(DIGITS[j])) {
                       result = result + Integer.toString(j + 1);
                   }
               }

           }
       }

       return result;
   }

// --------------- End: Number Conversion --------------

  // Try to get some predictions
  public static void main(String[] args) {
    try {
      run();
      // success!
      return;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
}
