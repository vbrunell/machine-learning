# Machine Learning and the Google API

These are some tools to facilitate working with the [Google Cloud Prediction API](https://cloud.google.com/prediction/docs/).

- `datagen` contains a Python script for generating CSV files to be used as training data.  Currently, it generates number/text pairs for use in categorical models.  If you'd like to use a regression model with integer column values, simply do not convert the integers to English in the script using `inflect`.

- `prediction-java` contains java code to build the predictive model using the training data you create and save in the bucket associated with your Google Cloud project, and it will query the model with queries you provide.  These can be added by modifying the PredictionSample.java file.
