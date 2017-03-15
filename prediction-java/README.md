
This code is derived from the Google Prediction API code sample provided by Google.  I've added the ability to create categorical models even when using integer values in CSV formatted training data.  

- To do this, use the replaceNumbers() function in PredictionSample.java, passing it the English version of the number from the training data.  
- To generate training data with integers converted to English, please look at the datagen directory in this repo.  The `inflect` library is used in `datagen/data-generator.py` to achieve this.

## To use this sample:

### Register your application

-   Enable the Prediction API in the [Google Developers Console]
    (https://console.developers.google.com/projectselector/apis/api/prediction/overview).
-   Create a service account from the [Permissions]
    (https://console.developers.google.com/permissions/serviceaccounts) page.
    When you create the service account, select **Furnish a new private key**
    and download the service account's private key in P12 format. Later on, after
    you check out the sample project, you will copy this downloaded file (e.g.
    `MyProject-123456.p12`) to the `src/main/resources/` directory, and then
    edit `PROJECT_ID`, `SERVICE_ACCT_EMAIL`, and `SERVICE_ACCT_KEYFILE` in
    `PredictionSample.java`.
-   [Activate Google Storage]
    (http://code.google.com/apis/storage/docs/signup.html), upload the [training
    data](http://code.google.com/apis/predict/docs/language_id.txt) required by
    the sample to Google Storage, and then edit `OBJECT_PATH` in
    `PredictionSample.java` to point to the training data. Otherwise, you will
    get a 400 error "Training data file not found".

### Check out and run the sample

**Prerequisites:** install [Java](http://java.com), [Git](https://git-scm.com/),
and [Maven](http://maven.apache.org/download.html). You might need to set your
`JAVA_HOME`.

    cd [someDirectory]
    git clone https://github.com/vbrunell/machine-learning.git
    cd machine-learning/prediction-java
    cp ~/Downloads/MyProject-12345abcd.p12 src/main/resources/
    [editor] src/main/java/com/google/api/services/prediction/PredictionSample.java
    mvn compile
    mvn -q exec:java

To enable logging of HTTP requests and responses (highly recommended when
developing), take a look at [`logging.properties`](logging.properties).
